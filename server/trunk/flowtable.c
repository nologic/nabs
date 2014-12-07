#include "flowtable.h"

#if USE_MD5
/* are you insane? */
uint32_t hash(const unsigned char *stuff, unsigned int len)
{
	unsigned char md[MD5_DIGEST_LENGTH];

	MD5(stuff, len, md);
	return (*((uint32_t *) md));
}
#else
/* SuperFastHash function (See benchmarks at http://www.azillionmonkeys.com/qed/hash.html) */
#undef get16bits
#if (defined(__GNUC__) && defined(__i386__)) || defined(__WATCOMC__) \
  || defined(_MSC_VER) || defined (__BORLANDC__) || defined (__TURBOC__)
#define get16bits(d) (*((const uint16_t *) (d)))
#endif

#if !defined (get16bits)
#define get16bits(d) ((((uint32_t)(((const uint8_t *)(d))[1])) << 8)\
                       +(uint32_t)(((const uint8_t *)(d))[0]) )
#endif

uint32_t hash (const unsigned char * data, unsigned int len) {
uint32_t h = len, tmp;
int rem;

    if (len <= 0 || data == NULL) return 0;

    rem = len & 3;
    len >>= 2;

    /* Main loop */
    for (;len > 0; len--) {
        h  += get16bits (data);
        tmp    = (get16bits (data+2) << 11) ^ h;
        h   = (h << 16) ^ tmp;
        data  += 2*sizeof (uint16_t);
        h  += h >> 11;
    }

    /* Handle end cases */
    switch (rem) {
        case 3: h += get16bits (data);
                h ^= h << 16;
                h ^= data[sizeof (uint16_t)] << 18;
                h += h >> 11;
                break;
        case 2: h += get16bits (data);
                h ^= h << 11;
                h += h >> 17;
                break;
        case 1: h += *data;
                h ^= h << 10;
                h += h >> 1;
    }

    /* Force "avalanching" of final 127 bits */
    h ^= h << 3;
    h += h >> 5;
    h ^= h << 4;
    h += h >> 17;
    h ^= h << 25;
    h += h >> 6;

    return h;
}
#endif

/* inline functions to update stats flow record given flow content, eu_packet */
static inline int calloc_flow_content(struct flow_content **fc){
	void *tmp;

	tmp= malloc((sizeof(struct flow_content) + MAX_PAYLOAD));

	if(tmp == NULL){
		fprintf(stderr, "could not allocate memory for new flow records!\n");
		return ERROR;
	}

	(*fc)= (struct flow_content *)tmp;
	(*fc)->payload= (unsigned char *) (tmp + sizeof(struct flow_content));
	return 0;
}

static inline void buffer_packet_payload(struct flow_content *fc, struct eu_packet *ep){
	unsigned int n;

	if (fc->bytes == MAX_PAYLOAD)
		return;

	n = (MAX_PAYLOAD - fc->bytes);
	if (n > ep->payload_len) {
		memcpy((fc->payload + fc->bytes), ep->payload, ep->payload_len);
		fc->bytes += ep->payload_len;
	} else {
		memcpy((fc->payload + fc->bytes), ep->payload, n);
		fc->bytes += n;
	}
}

static inline void update_tcp_flags(struct flow_content *fc, struct eu_packet *ep){
	if((ep->tcpptr->th_flags) & TH_ACK)
		++(fc->fr.tcp_acks);
	
	if((ep->tcpptr->th_flags) & TH_PUSH)
		++(fc->fr.tcp_push);
	
	if((ep->tcpptr->th_flags) & TH_SYN)
		++(fc->fr.tcp_syns);
	
	if((ep->tcpptr->th_flags) & TH_FIN)
		++(fc->fr.tcp_fins);
	
	if((ep->tcpptr->th_flags) & TH_RST)
		++(fc->fr.tcp_rsts);
	
	if((ep->tcpptr->th_flags) & TH_URG)
		++(fc->fr.tcp_urgs);
}

static inline void update_synack_times(struct flow_content *fc, struct eu_packet *ep){
	if(fc->flags & FL_SYN_DONE)
		return;
		
	if((ep->tcpptr->th_flags) & TH_SYN){
			memcpy(&(fc->fr.syn_time), &(ep->header->ts), sizeof(struct timeval));
	}else if((ep->tcpptr->th_flags) & TH_ACK){
			memcpy(&(fc->fr.synack_time), &(ep->header->ts), sizeof(struct timeval));
			fc->flags= FL_SYN_DONE;
	}
}

static inline void update_histogram(struct flow_content *fc, struct eu_packet *ep){
	register unsigned int i;

	for(i=0; ((i < MAX_HISTOGRAM_INDEX) && (ep->header->len > histo_bucket_bound[i])); ++i);

	if(i < MAX_HISTOGRAM_INDEX)
		++(fc->fr.histogram[i]);
	else
		++(fc->fr.over_sized_packet);
}

static inline void init_flow_content_stats(struct flow_content *fc, struct eu_packet *ep){
	INIT_LIST_HEAD(&(fc->list));
	pthread_mutex_init(&fc->lock, NULL);
	fc->status = FS_RESET;
	fc->flags = FL_RESET;
	fc->bytes= 0;

	memset(&(fc->fr), 0, sizeof(struct flow_record));
	
	memcpy(fc->fr.flowid, ep->flowid, FLOWID_LEN);
	memcpy(&(fc->fr.start_time), &(ep->header->ts), sizeof(struct timeval));
	memcpy(&(fc->fr.end_time), &(ep->header->ts), sizeof(struct timeval));

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
}

static inline void update_flow_content_stats(struct flow_content *fc, struct eu_packet *ep){
	memcpy(&(fc->fr.end_time), &(ep->header->ts), sizeof(struct timeval));

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

/* initialize the list_heads in table */
int flowtable_init(struct flow_table *table, unsigned int buckets){
	unsigned int i;
	void* tmp;

	table->buckets = buckets;
	i= (sizeof(struct list_head_safe*) * buckets);

	tmp= malloc( i+ (sizeof(struct list_head_safe ) * buckets) );

	table->entries= (struct list_head_safe **)tmp;
	tmp= tmp + i;

	for(i=0; i < buckets; ++i){
		table->entries[i] = (struct list_head_safe *) (tmp + (i * sizeof(struct list_head_safe)));
		INIT_LIST_HEAD_SAFE((table->entries[i]));
	}

	INIT_LIST_HEAD_SAFE(&(table->freelist));

	return 0;
}

int flowtable_add_flow(struct flow_table *ft, struct eu_packet *e)
{
	unsigned int i, found;
	struct list_head *pos;
	struct flow_content *tmp;

	i = (hash(e->flowid, FLOWID_LEN) % ft->buckets);

	/* see if the flow already exist */
	found = 0;
	safe_list_lock((ft->entries[i]));
	list_for_each(pos, &(ft->entries[i]->list)) {
		tmp = list_entry(pos, struct flow_content, list);

		if (memcmp(tmp->fr.flowid, e->flowid, FLOWID_LEN) == 0) {
			found = 1;
			break;
		}
	}

	if (found) {
		buffer_packet_payload(tmp, e);
		update_flow_content_stats(tmp, e);
		safe_list_unlock((ft->entries[i]));
		return OLD_FLOW;
	}
	safe_list_unlock((ft->entries[i]));

	/* empty bucket or collision in either case insert a new flowcontent */

	/* anything on freelist? */
	safe_list_lock(&(ft->freelist));
	if (list_empty(&(ft->freelist.list))) {
		safe_list_unlock(&(ft->freelist));

		if(calloc_flow_content(&tmp) == ERROR)
			return ERROR;

		init_flow_content_stats(tmp, e);
		buffer_packet_payload(tmp, e);
		
		safe_list_lock((ft->entries[i]));
		list_add(&(tmp->list), &(ft->entries[i]->list));
		safe_list_unlock((ft->entries[i]));

		return NEW_FLOW;
	}

	/* use the memory left in freelist */
	pos = ft->freelist.list.next;
	list_del_init(pos);
	safe_list_unlock(&(ft->freelist));

	tmp = list_entry(pos, struct flow_content, list);
	init_flow_content_stats(tmp, e);
	buffer_packet_payload(tmp, e);

	safe_list_lock((ft->entries[i]));
	list_add(&(tmp->list), &(ft->entries[i]->list));
	safe_list_unlock((ft->entries[i]));

	return NEW_FLOW;
}

void flowtable_del_flow(struct flow_table *table, struct list_head *item)
{
	safe_list_lock(&(table->freelist));
	list_move(item, &(table->freelist.list));
	safe_list_unlock(&(table->freelist));
}

void flowtable_add_to_freelist(struct flow_table *table, struct list_head *item)
{
	safe_list_lock(&(table->freelist));
	list_add(item, &(table->freelist.list));
	safe_list_unlock(&(table->freelist));
}

void flowtable_finit(struct flow_table *ft)
{

	unsigned int i;
	struct list_head *p, *q;
	struct flow_content *tmp;

	safe_list_lock(&(ft->freelist));
	list_for_each_safe(p, q, &(ft->freelist.list)) {
		tmp = list_entry(p, struct flow_content, list);

		list_del(p);
		free(tmp);
	}
	safe_list_unlock(&(ft->freelist));

	for (i = 0; i < ft->buckets; ++i) {
		safe_list_lock((ft->entries[i]));
		list_for_each_safe(p, q, &(ft->entries[i]->list)) {
			tmp = list_entry(p, struct flow_content, list);
			list_del(p);
			free(tmp);
		}
		safe_list_unlock((ft->entries[i]));
	}

	free(ft->entries);
}
