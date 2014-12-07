#include "flow_table.h"
#include "flow_stats_operations.h"

int flowtable_init(struct flow_table *table, unsigned int buckets, unsigned int records_in_memory){
	unsigned int i;
	unsigned int allocated = 0;

	table->buckets = buckets;
	table->entries = (struct list_head_safe *)malloc(sizeof(struct list_head_safe)*buckets);
	allocated += (sizeof(struct list_head_safe*) * buckets) ; 

	for(i=0; i < buckets; ++i){
		INIT_LIST_HEAD_SAFE(&(table->entries[i]));
		allocated += sizeof(struct list_head_safe);
	}

	INIT_LIST_HEAD_SAFE(&(table->freelist));
	struct flow_content *fc;
	for(i=0; i < records_in_memory; ++i){
		fc= (struct flow_content *)malloc(sizeof(struct flow_content));
		INIT_LIST_HEAD(&(fc->list));
		flowtable_add_to_freelist(table, &(fc->list));
		allocated += sizeof(struct flow_content);
	}

	pthread_mutex_init(&table->lock, NULL);
	table->newones = 0;
	table->updates = 0;

	fprintf(stderr, "pre-allocated %.2fMB.\n", (double)allocated/(double)(1024*1024));

	return STATUS_OK;
}

int flowtable_add_flow(struct flow_table *ft, struct eu_packet *e)
{
	register unsigned int i;
	register struct list_head *pos;
	register struct flow_content *tmp=NULL;

	i = flowtable_compute_hashcode(ft, e->flowid, FLOWID_LEN);

	/* look into bucket i to see if a flow_content record exists for this flow.
	 * when such a record exist tmp will point to it. otherwise tmp will be
	 * NULL.
	 */
	flowtable_lock_bucket(ft, i);
	list_for_each(pos, &(ft->entries[i].list)) {
		tmp = list_entry(pos, struct flow_content, list);
		if (memcmp(tmp->fr.flowid, e->flowid, FLOWID_LEN) == 0) {
			break;
		}
		tmp= NULL;
	}

	if (tmp) {
		pthread_mutex_lock(&ft->lock);
		++ft->updates;
		pthread_mutex_unlock(&ft->lock);

		buffer_packet_payload(tmp, e);
		update_flow_content_stats(tmp, e);
		flowtable_unlock_bucket(ft, i);
		return STATUS_OK;
	}
	flowtable_unlock_bucket(ft, i);

	if((tmp= flowtable_get_freeslot(ft)) == NULL){
		fprintf(stderr, "not enough pre-allocated memory for flow content!!\n");
		return STATUS_ERROR;
	}

	init_flow_content_stats(tmp, e);
	buffer_packet_payload(tmp, e);
	flowtable_add_flow_content(ft, tmp, i);

	pthread_mutex_lock(&ft->lock);
	++ft->newones;
	pthread_mutex_unlock(&ft->lock);

	return STATUS_OK;
}

struct flow_content *flowtable_get_freeslot(struct flow_table *ft){
	struct list_head *pos;

	flowtable_lock_freelist(ft);
	if(list_empty(&(ft->freelist.list))){
		flowtable_unlock_freelist(ft);
		return NULL;
	}

	pos= ft->freelist.list.next;
	list_del_init(pos);
	flowtable_unlock_freelist(ft);

	return list_entry(pos, struct flow_content, list);
}

void flowtable_finit(struct flow_table *ft)
{
	unsigned int i;
	struct list_head *p, *q;
	struct flow_content *tmp;

	flowtable_lock_freelist(ft);
	list_for_each_safe(p, q, &(ft->freelist.list)) {
		tmp = list_entry(p, struct flow_content, list);
		list_del(p);
		free(tmp);
	}
	flowtable_unlock_freelist(ft);

	for (i = 0; i < ft->buckets; ++i) {
		flowtable_lock_bucket(ft, i);
		list_for_each_safe(p, q, &(ft->entries[i].list)) {
			tmp = list_entry(p, struct flow_content, list);
			list_del(p);
			free(tmp);
		}
		flowtable_unlock_bucket(ft, i);
		FINIT_LIST_HEAD_SAFE(&(ft->entries[i]));
	}

	free(ft->entries);
	pthread_mutex_destroy(&ft->lock);
}

/*** simple flow operations are defined below ***/
void init_flow_content_stats(struct flow_content *fc, struct eu_packet *ep){
	fc->bytes = 0;
	memset(&(fc->fr), 0, sizeof(struct flow_record));
	memcpy(fc->fr.flowid, ep->flowid, FLOWID_LEN);

	fc->fr.start_time.tv_sec = ep->header->ts.tv_sec;
	fc->fr.start_time.tv_usec= ep->header->ts.tv_usec;
	fc->fr.end_time.tv_sec = ep->header->ts.tv_sec;
	fc->fr.end_time.tv_usec= ep->header->ts.tv_usec;


	fc->fr.total_bytes = ep->header->len;
	fc->fr.total_packets =1;

	fc->fr.max_packet_size= ep->header->len;
	fc->fr.min_packet_size= ep->header->len;

	fc->fr.max_ttl= ep->ipptr->ip_ttl;
	fc->fr.min_ttl= ep->ipptr->ip_ttl;

	if(ep->ipptr->ip_p == IPPROTO_TCP){
		update_tcp_flags(fc, ep);
		update_synack_times(fc, ep);
	}
	update_histogram(fc, ep);
	
	INIT_LIST_HEAD(&(fc->list));
}

void update_flow_content_stats(struct flow_content *fc, struct eu_packet *ep){
	fc->fr.end_time.tv_sec = ep->header->ts.tv_sec;
	fc->fr.end_time.tv_usec= ep->header->ts.tv_usec;

	fc->fr.total_bytes += ep->header->len;
	++(fc->fr.total_packets);

	if(fc->fr.max_packet_size < ep->header->len)
		fc->fr.max_packet_size= ep->header->len;
	else if(fc->fr.min_packet_size > ep->header->len)
		fc->fr.min_packet_size= ep->header->len;

	if(fc->fr.max_ttl < ep->ipptr->ip_ttl)
		fc->fr.max_ttl= ep->ipptr->ip_ttl;
	else if(fc->fr.min_ttl > ep->ipptr->ip_ttl)
		fc->fr.min_ttl= ep->ipptr->ip_ttl;

	if(ep->ipptr->ip_p == IPPROTO_TCP){
		update_tcp_flags(fc, ep);
		update_synack_times(fc, ep);
	}
	update_histogram(fc, ep);
}
