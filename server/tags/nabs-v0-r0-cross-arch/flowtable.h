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

#include <safe_list.h>

#define MAX_PAYLOAD   16384
//#define MAX_PAYLOAD 	32768
#define FLOW_ENTRIES   	32
#define FLOW_TIMEOUT	60	/* seconds */
#define FLOW_EPOCH 		3
#define FLOW_SKIP		3
#define FLOWID_LEN		12

#define OLD_FLOW	0
#define NEW_FLOW	1
#define FULL_FLOW 	2
#define NO_FLOW 	-2
#define ERROR		-1

/* keeps MAX_PAYLOAD bytes of data per flow */
struct flow_content {
	struct list_head list;
	unsigned char flowid[FLOWID_LEN];	/* srcip:dstip:srcport:dstport */
	uint32_t itime;
	unsigned int bytes;
	unsigned char *payload;
};

struct flow_entry {
	struct list_head list;
	unsigned char flowid[FLOWID_LEN];
	unsigned int epoch;
};

struct flow_table {
	struct list_head_safe **entries;
	struct list_head_safe freelist;
	unsigned int buckets;
};

uint32_t hash(const unsigned char *, unsigned int);
int flowtable_init(struct flow_table *, unsigned int);
int flowtable_add_flow(struct flow_table *, unsigned char *, unsigned int,
		       const unsigned char[], unsigned int);
void flowtable_del_flow(struct flow_table *, struct list_head *);
void flowtable_add_to_freelist(struct flow_table *, struct list_head *);
void flowtable_finit(struct flow_table *);

#endif
