#ifndef INCLUDED_FLOW_RECORD_H
#define INCLUDED_FLOW_RECORD_H

#include <stdint.h>
#include "packet_capture.h"

struct time_stamp{
	uint32_t tv_sec;
	uint32_t tv_usec;
};

#define MAX_CONTENT_TYPE	8
#define MAX_HISTOGRAM_INDEX 10
static const unsigned short histo_bucket_bound[MAX_HISTOGRAM_INDEX]= 
	{64, 128, 192, 256, 384, 512, 768, 1024, 1280, 1536};

/* 128 bytes */
struct flow_record {
	unsigned char flowid[FLOWID_LEN];
	unsigned char toa;	/* Type of Application */
	unsigned char mjv;	/* Major version */
	unsigned char mnv;	/* Minor version */

	struct time_stamp start_time;
	struct time_stamp end_time;

	unsigned short toc_kb[MAX_CONTENT_TYPE];	/* content type mutliples of PAYLOAD_BUFFER_LENGTH */

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
	struct time_stamp syn_time;
	struct time_stamp synack_time;
	struct time_stamp fin_time;
	struct time_stamp finack_time;
};

#endif
