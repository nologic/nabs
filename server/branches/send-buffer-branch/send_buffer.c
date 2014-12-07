#include <stdlib.h>
#include <string.h>
#include <unistd.h>

#include "send_buffer.h"
#include "utils.h"

static inline void init_send_buffer(struct send_buffer *s, void *ptr)
{
	memset(s, 0, sizeof(struct send_buffer));
	s->start_ptr = ptr;
	s->end_ptr = ptr;
	pthread_mutex_init(&(s->lock), NULL);
}

static inline void lock_send_buffer(struct send_buffer *s)
{
	pthread_mutex_lock(&s->lock);
}

static inline void unlock_send_buffer(struct send_buffer *s)
{
	pthread_mutex_unlock(&s->lock);
}

static inline void lock_whole_buffer(struct sb_config *sb){
	pthread_mutex_lock(&sb->lock);
}

static inline void unlock_whole_buffer(struct sb_config *sb){
	pthread_mutex_unlock(&sb->lock);
}

int init_send_buffers(struct sb_config *sb, unsigned int chunk_size, unsigned int chunks_in_a_buffer, unsigned int n_buffers)
{
	void *ptr, *p;
	unsigned int i;

	if(chunks_in_a_buffer > MAX_CHUNKS_IN_A_BUFFER){
		fprintf(stderr, "maximum chunks in a buffer is %u. given value %u is too big!\n", MAX_CHUNKS_IN_A_BUFFER, chunks_in_a_buffer);
		return -1;
	}
	
	memset(sb, 0, sizeof(struct sb_config));
	sb->buffer_size= (chunk_size * chunks_in_a_buffer);
	sb->chunk_size= chunk_size;
	sb->chunks_in_a_buffer= chunks_in_a_buffer;
	sb->n_buffers = n_buffers;

	if ((ptr = (void *)malloc((sb->buffer_size * n_buffers))) == NULL)
		return -1;

	if ((sb->buffers = (void *)malloc(sizeof(struct send_buffer) * n_buffers)) == NULL){
		fprintf(stderr, "could not allocate memory for send buffers!\n");
		return -1;
	}

	for (i = 0, p = sb->buffers; i < n_buffers; ++i)
		init_send_buffer((p + (i * sizeof(struct send_buffer))),
				 (ptr + (i * sb->buffer_size)));

	pthread_mutex_init(&(sb->lock), NULL);

	return 0;
}

int get_chunk_from_buffer(struct sb_config *sb, void **ptr, unsigned int chunk_size, struct send_buffer **s){
	unsigned int unoccupied;

	/* first try */
	lock_whole_buffer(sb);
	*s= &(sb->buffers[sb->current_buffer]);
	unlock_whole_buffer(sb);

	lock_send_buffer(*s);
	unoccupied= (sb->buffer_size - ((*s)->end_ptr - (*s)->start_ptr));

	if (unoccupied >= chunk_size) {
		(*ptr)= (*s)->end_ptr;
		(*s)->end_ptr += chunk_size;
		unlock_send_buffer(*s);
		return 0;
	}
	unlock_send_buffer(*s);

	/* move to next buffer and try */
	lock_whole_buffer(sb);
	++(sb->current_buffer);
	sb->current_buffer %= sb->n_buffers;
	*s = &(sb->buffers[sb->current_buffer]);
	unlock_whole_buffer(sb);

	lock_send_buffer(*s);
	unoccupied = (sb->buffer_size - ((*s)->end_ptr - (*s)->start_ptr));
	if (unoccupied >= chunk_size) {
		(*ptr)= (*s)->end_ptr;
		(*s)->end_ptr += chunk_size;
		unlock_send_buffer(*s);
		return 0;
	}
	unlock_send_buffer(*s);

	/* no memory left in buffer to fit chunk_size */
	*s=NULL;
	return -1;
}

void mark_chunk_flushable(struct sb_config *sbc, struct send_buffer *sb, void *ptr){
	unsigned int n;

	n= ((ptr - sb->start_ptr)/sbc->buffer_size);
	bit_set(sb->bitmap, n);
}

static inline int flush_send_buffer(struct sb_config *sb, struct send_buffer *s, struct connection_pair *cp){
	register int j, l;
	int fs, fc;
	void *ptr;

	lock_send_buffer(s);


	/* find first flushable chunk */
	bit_ffs(s->bitmap, sb->chunks_in_a_buffer, &fs);
	if(fs == -1){ /* nothing flushable */
		unlock_send_buffer(s);
		return 0;
	}

	/* find first non-flushable chunk */
	bit_ffc(s->bitmap, sb->chunks_in_a_buffer, &fc);
	if(fc == -1){ /* everything is flushable */
		l= (s->end_ptr - s->start_ptr);
		fprintf(stderr, "flushing everything (%d bytes).", l);

		for (j = 0; j < cp->n_remote; ++j) {
			if (!cp->remote[j].socketfd)
				continue;

			if (write (cp->remote[j].socketfd, s->start_ptr, l) < 0) {
					close(cp->remote[j].socketfd);
					fclose(cp->remote[j].socketfp);
					memset(&cp->remote[j], 0, sizeof(struct connection));
					continue;
			}
		}

		s->end_ptr = s->start_ptr;
		bit_nclear(s->bitmap, 0, (sb->chunks_in_a_buffer - 1));
		unlock_send_buffer(s);
		return l;
	}
	
	/* some chunks flushable, some not. flush the first available window and
	 * only that. this keeps the flush-window moving in only forward direction
	 */
	ptr= (fs == 0)? (s->start_ptr): ((s->start_ptr) + (fs * sb->chunk_size));
	l= ((fc - fs) * sb->chunk_size);
	fprintf(stderr, "flushing a range (%d bytes).", l);
	for (j = 0; j < cp->n_remote; ++j) {
		if (!cp->remote[j].socketfd)
			continue;

		if (write (cp->remote[j].socketfd, ptr, l) < 0) {
				close(cp->remote[j].socketfd);
				fclose(cp->remote[j].socketfp);
				memset(&cp->remote[j], 0, sizeof(struct connection));
				continue;
		}
	}

	/* when and only when the last chunk is flushed, reset the buffer */
	if(s->end_ptr == (ptr + l)){
		s->end_ptr= s->start_ptr;
		bit_nclear(s->bitmap, 0, (sb->chunks_in_a_buffer - 1));
	}

	unlock_send_buffer(s);
	return l;
}

void flush_send_buffers(struct sb_config *sb, struct connection_pair *cp)
{
	register int i, cb;

	lock_whole_buffer(sb);
	cb = sb->current_buffer;
	unlock_whole_buffer(sb);

	/* flush all buffers preceding current_buffer */
	if (cb > 0) {
		for (i = (cb - 1); i != 0; --i)
			flush_send_buffer(sb, &sb->buffers[i], cp);
		flush_send_buffer(sb, &sb->buffers[0], cp);
	}

	/* current_buffer may have wrapped around */
	lock_whole_buffer(sb);
	if (sb->prev_buffer > cb) {
		fprintf(stderr, "looks like buffer wrapped around. (%d, %d)", sb->prev_buffer, cb);
		for (i = sb->prev_buffer; i < sb->n_buffers; ++i)
			flush_send_buffer(sb, &sb->buffers[i], cp);
	}
	unlock_whole_buffer(sb);

	/* current_buffer may be too slow to fill */
	lock_whole_buffer(sb);
	if(sb->prev_buffer != cb){
		sb->prev_buffer = cb;
		unlock_whole_buffer(sb);
		return;
	}

	if (sb->skips == 8) {
		fprintf(stderr, "buffer %d had 8 chances to fill but did not!\n", cb);
		if(flush_send_buffer(sb, &sb->buffers[cb], cp) == 0){
			unlock_whole_buffer(sb);
			u_sleep(0, FLUSH_TIMEOUT); /* ZZZZzzzzzz */
		}else{
			(sb->skips) = 0;
			unlock_whole_buffer(sb);
		}
	} else {
		++(sb->skips);
		unlock_whole_buffer(sb);
	}
}
