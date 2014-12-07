#ifndef SEND_BUFFER_H
#define SEND_BUFFER_H

#include <pthread.h>
#include "connections.h"

#define FLUSH_TIMEOUT 50000	/* usecs */

struct send_buffer {
	char *start_ptr;
	char *end_ptr;
	pthread_mutex_t lock;
};

struct sb_config {

	int buffer_size;

	struct send_buffer *buffers;
	int n_buffers;

	int current_buffer;
	int prev_buffer;
	int skips;

	pthread_mutex_t lock;
};

int init_send_buffers(struct sb_config *, int, int);
int copy_to_send_buffer(struct sb_config *, void *, unsigned int);
void flush_send_buffers(struct sb_config *, struct connection_pair *);

#endif
