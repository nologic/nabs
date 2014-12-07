#ifndef INCLUDED_FLOWTABLE_H
#define INCLUDED_FLOWTABLE_H

#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <stdint.h>
#include <pthread.h>

#include "eunomia_config.h"
#include "safe_list.h"
#include "packet_capture.h"
#include "flow_record.h"
#include "hash_function.h"

struct flow_content {
	struct list_head list;
	struct flow_record fr;
	unsigned char flags;
	char __padding[3];
#define FL_RESET		0x00
#define FL_SYN_DONE		0x01
#define FL_FIN_DONE		0x02
	unsigned int bytes;
	unsigned char payload[PAYLOAD_BUFFER_LENGTH];
};

struct flow_table {
	pthread_mutex_t lock;
	long newones;
	long updates;
	long dropped;

	unsigned int buckets;
	struct list_head_safe freelist;
	struct list_head_safe *entries;
};

int flowtable_init(struct flow_table *, unsigned int, unsigned int);
int flowtable_add_flow(struct flow_table *, struct eu_packet *);
struct flow_content *flowtable_get_freeslot(struct flow_table *);
void flowtable_finit(struct flow_table *);

/*** flow stats related functions ***/
void init_flow_content_stats(struct flow_content *, struct eu_packet *);
void update_flow_content_stats(struct flow_content *, struct eu_packet *);


/* static inline functions */
static inline void flowtable_lock_bucket(struct flow_table *ft, unsigned int bucket){
	safe_list_lock(&(ft->entries[bucket]));
}

static inline void flowtable_unlock_bucket(struct flow_table *ft, unsigned int bucket){
	safe_list_unlock(&(ft->entries[bucket]));
}

static inline void flowtable_lock_freelist(struct flow_table *ft){
	safe_list_lock(&(ft->freelist));
}

static inline void flowtable_unlock_freelist(struct flow_table *ft){
	safe_list_unlock(&(ft->freelist));
}

static inline uint32_t flowtable_compute_hashcode(struct flow_table *ft, const unsigned char* key, unsigned int len){
	return (hash(key, len) % ft->buckets);
}

static inline void flowtable_add_flow_content(struct flow_table *ft, struct flow_content *fc, unsigned int bucket){
	safe_list_lock(&(ft->entries[bucket]));
	list_add(&(fc->list), &(ft->entries[bucket].list));
	safe_list_unlock(&(ft->entries[bucket]));
}

static inline void flowtable_add_to_freelist(struct flow_table *ft, struct list_head *item){
	safe_list_lock(&(ft->freelist));
	list_add(item, &(ft->freelist.list));
	safe_list_unlock(&(ft->freelist));
}

#endif
