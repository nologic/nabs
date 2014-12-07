#include <stdlib.h>
#include <string.h>
#include <unistd.h>

#include "send_buffer.h"
#include "utils.h"

static inline void init_send_buffer(struct send_buffer *s, char *ptr)
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

int init_send_buffers(struct sb_config *sb, int buffer_size, int n_buffers)
{
	void *ptr, *p;
	int i;

	memset(sb, 0, sizeof(struct sb_config));

	if ((ptr = (void *)malloc((buffer_size * n_buffers))) == NULL)
		return -1;

	if ((sb->buffers =
	     (void *)malloc(sizeof(struct send_buffer) * n_buffers)) == NULL)
		return -1;

	for (i = 0, p = sb->buffers; i < n_buffers; ++i)
		init_send_buffer((p + (i * sizeof(struct send_buffer))),
				 (ptr + (i * buffer_size)));

	sb->buffer_size = buffer_size;
	sb->n_buffers = n_buffers;

	pthread_mutex_init(&(sb->lock), NULL);

	return 0;
}

int copy_to_send_buffer(struct sb_config *sb, void *p, unsigned int len)
{
	struct send_buffer *tmp = &(sb->buffers[sb->current_buffer]);
	unsigned int unoccupied;

	lock_send_buffer(tmp);
	unoccupied = (sb->buffer_size - (tmp->end_ptr - tmp->start_ptr));

	if (unoccupied >= len) {
		memcpy(tmp->end_ptr, p, len);
		tmp->end_ptr += len;
		unlock_send_buffer(tmp);
		return 0;
	}
	unlock_send_buffer(tmp);

	pthread_mutex_lock(&sb->lock);
	++(sb->current_buffer);
	sb->current_buffer %= sb->n_buffers;
	pthread_mutex_unlock(&sb->lock);

	tmp = &(sb->buffers[sb->current_buffer]);

	lock_send_buffer(tmp);
	unoccupied = (sb->buffer_size - (tmp->end_ptr - tmp->start_ptr));

	if (unoccupied >= len) { /* used to be (unoccupied < len) WHY?? */
		memcpy(tmp->end_ptr, p, len);
		tmp->end_ptr += len;
		unlock_send_buffer(tmp);
		return 0;
	}
	unlock_send_buffer(tmp);

	return -1;
}

static inline int __flush_send_buffer(struct send_buffer *s, struct connection_pair *cp){
	register int j, l;

	lock_send_buffer(s);
	l= (s->end_ptr - s->start_ptr);

	if(l <= 0){
		unlock_send_buffer(s);
	   	return l;
	}

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
	unlock_send_buffer(s);
	return l;
}

void flush_send_buffers(struct sb_config *sb, struct connection_pair *cp)
{
	register int i, cb, pb;

	pthread_mutex_lock(&sb->lock);
	cb = sb->current_buffer;
	pb = sb->prev_buffer;
	pthread_mutex_unlock(&sb->lock);

	/* if buffer has made some head way, flush everything that lies before
	 * the current_buffer in reverse order.
	 */
	if (cb > 0) {
		for (i = (cb - 1); i != 0; --i)
			__flush_send_buffer(&sb->buffers[i], cp);

		__flush_send_buffer(&sb->buffers[0], cp);
	}

	/* if the current_buffer has wrapped around take care of tail end */
	if (pb > cb) {
		fprintf(stderr, "looks like buffer wrapped around. (%d, %d)", pb, cb);
		for (i = pb; i < sb->n_buffers; ++i)
			__flush_send_buffer(&sb->buffers[i], cp);
	}

	/* if the current_buffer is slow to fill up give it 8 chances before 
	 * forcing a flush on it.
	 */
	if (pb == cb) {
		if (sb->skips == 8) {
			fprintf(stderr, "buffer %d had 8 chances to fill but did not!\n", cb);
			if(__flush_send_buffer(&sb->buffers[cb], cp) <= 0){
				u_sleep(0, FLUSH_TIMEOUT);
			}else{
				sb->skips = 0;
			}
		} else {
			++(sb->skips);
		}
	}

	pthread_mutex_lock(&sb->lock);
	sb->prev_buffer = cb;
	pthread_mutex_unlock(&sb->lock);
}
