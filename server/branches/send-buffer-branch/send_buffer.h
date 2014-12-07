#ifndef SEND_BUFFER_H
#define SEND_BUFFER_H

#include <pthread.h>
#include "connections.h"
#include "bitstring.h"

#define FLUSH_TIMEOUT 50000	/* usecs */
#define MAX_CHUNKS_IN_A_BUFFER 32 /* corresponds to the bitmap below */

struct send_buffer {
	void *start_ptr;
	void *end_ptr;
	/* bitmap: semantics as follows...
	 * every chunk between start_ptr and end_ptr with unset bit is not ready for flush
	 * every chunk between start_ptr and end_ptr with set bit is ready for flush
	 * everything after end_ptr is not relevant because it is unallocated space
	 */
	bitstr_t bit_decl(bitmap, MAX_CHUNKS_IN_A_BUFFER);
	pthread_mutex_t lock;
};

struct sb_config {

	unsigned int buffer_size;
	unsigned int chunk_size;
	unsigned int chunks_in_a_buffer;

	struct send_buffer *buffers;

	int n_buffers;
	int current_buffer;
	int prev_buffer;
	int skips;

	pthread_mutex_t lock;
};

int init_send_buffers(struct sb_config *, unsigned int, unsigned int, unsigned int);
int get_chunk_from_buffer(struct sb_config *, void **, unsigned int, struct send_buffer **);
void mark_chunk_flushable(struct sb_config *, struct send_buffer *, void *);
void flush_send_buffers(struct sb_config *, struct connection_pair *);

#endif
