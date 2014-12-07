#ifndef __FLOWTABLE_H
#define __FLOWTABLE_H

#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <stdint.h>		/* Replace with "stdint.h" if appropriate */
#include <pthread.h>
#if USE_MD5
#include <openssl/md5.h>
#endif

#include "safe_list.h"
#include "packet_capture.h"

#define MAX_CONTENT_TYPE	8
#define MAX_PAYLOAD   		16384
//#define MAX_PAYLOAD   	32768

#define FLOW_ENTRIES   		256
#define FLOW_ACTIVE_TIMEOUT		120	/* seconds */
#define FLOW_INACTIVE_TIMEOUT	30	/* seconds */
#define FLOW_EPOCH 			3
#define FLOW_SKIP			3

#define OLD_FLOW			0
#define NEW_FLOW			1
#define FULL_FLOW 			2
#define NO_FLOW 			-2
#define ERROR				-1

#define MAX_HISTOGRAM_INDEX 10
static const unsigned short histo_bucket_bound[MAX_HISTOGRAM_INDEX]= 
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


/* status of the flow record */
#define FS_RESET 		0x00
#define FS_DONE 		0x01
#define FS_CLASSIFYING 	0x02
#define FS_CLASSIFIED	0x04

#define FL_RESET		0x00
#define FL_SYN_DONE		0x01
#define FL_FIN_DONE		0x02

/* keeps MAX_PAYLOAD bytes of data per flow */
struct flow_content {
	struct list_head list;
	struct flow_record fr;
	unsigned int bytes;
	unsigned char *payload;
	unsigned char status;
	unsigned char flags;
	pthread_mutex_t lock;
};

struct flow_table {
	struct list_head_safe **entries;
	struct list_head_safe freelist;
	unsigned int buckets;
};

uint32_t hash(const unsigned char *, unsigned int);
int flowtable_init(struct flow_table *, unsigned int);
int flowtable_add_flow(struct flow_table *, struct eu_packet *);
void flowtable_del_flow(struct flow_table *, struct list_head *);
void flowtable_add_to_freelist(struct flow_table *, struct list_head *);
void flowtable_finit(struct flow_table *);

#endif
