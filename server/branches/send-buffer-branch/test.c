#include <stdlib.h>
#include <string.h>
#include <stdint.h>		/* Replace with "stdint.h" if appropriate */
#include <pthread.h>
#include <stdio.h>
#include <sys/time.h>

#define FLOWID_LEN	13
#define MAX_CONTENT_TYPE	8
#define MAX_HISTOGRAM_INDEX 10
static unsigned short histo_bucket_bound[MAX_HISTOGRAM_INDEX]= 
	{64, 128, 192, 256, 384, 512, 768, 1024, 1280, 1536};
/* 128 bytes */
struct flow_record {
	unsigned char flowid[FLOWID_LEN];
	unsigned char toa;	/* Type of Application */
	unsigned char mjv;	/* Major version */
	unsigned char mnv;	/* Minor version */

	struct timeval start_time;
	struct timeval end_time;

	unsigned short toc_kb[MAX_CONTENT_TYPE];	/* content type mutliples of MAX_PAYLOAD */

	unsigned int total_bytes;
	unsigned int total_packets;

	unsigned short max_packet_size;
	unsigned short min_packet_size;

	unsigned short tcp_syns;
	unsigned short tcp_acks;
	unsigned short tcp_fins;
	unsigned short tcp_rsts;
	unsigned short tcp_urgs;
	unsigned short tcp_push;

	unsigned char min_ttl;
	unsigned char max_ttl;
	unsigned short over_sized_packet; /* bigger than that biggest histo bucket boundary */

	unsigned short histogram[MAX_HISTOGRAM_INDEX];

	/* to measure slowdown */
	struct timeval syn_time;
	struct timeval synack_time;
	struct timeval fin_time;
	struct timeval finack_time;
};
#define FLOWID_LEN 13
#define CONTENT_TYPES 8

struct send_buffer {
	char *start_ptr;
	char *end_ptr;
	pthread_mutex_t lock;
};

int main(int argc, char **argv)
{
	fprintf(stderr, "sizeof send buffer %d\n", sizeof(struct send_buffer));
	fprintf(stderr, "sizeof pthread_mutex_t %d\n", sizeof(pthread_mutex_t));

	fprintf(stderr, "sizeof (unsigned char) %d\n", sizeof(unsigned char));
	fprintf(stderr, "sizeof (unsigned short) %d\n", sizeof(unsigned short));
	fprintf(stderr, "sizeof (unsigned int) %d\n", sizeof(unsigned int));
	fprintf(stderr, "sizeof (unsigned long) %d\n", sizeof(unsigned long));
	fprintf(stderr, "sizeof struct timeval %d\n", sizeof(struct timeval));
	fprintf(stderr, "sizeof histo_index  %d\n", sizeof(histo_bucket_bound));
	fprintf(stderr, "sizeof flow record %d\n", sizeof(struct flow_record));
	return 0;
}
