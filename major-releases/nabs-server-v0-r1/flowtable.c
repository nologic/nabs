#include "flowtable.h"

#if USE_MD5
uint32_t hash(const unsigned char *stuff, unsigned int len)
{
	unsigned char md[MD5_DIGEST_LENGTH];

	MD5(stuff, len, md);
	return (*((uint32_t *) md));
}
#else
/* hash function from http://www.azillionmonkeys.com/qed/hash.html */

#undef get16bits
#if (defined(__GNUC__) && defined(__i386__)) || defined(__WATCOMC__) \
	  || defined(_MSC_VER) || defined (__BORLANDC__) || defined (__TURBOC__)
#define get16bits(d) (*((const uint16_t *) (d)))
#endif

#if !defined (get16bits)
#define get16bits(d) ((((const uint8_t *)(d))[1] << UINT32_C(8))\
		                      +((const uint8_t *)(d))[0])
#endif

uint32_t hash(const unsigned char *data, unsigned int len)
{
	uint32_t hash = 0, tmp;
	int rem;

	if (len <= 0 || data == NULL)
		return 0;

	rem = len & 3;
	len >>= 2;

	/* Main loop */
	for (; len > 0; len--) {
		hash += get16bits(data);
		tmp = (get16bits(data + 2) << 11) ^ hash;
		hash = (hash << 16) ^ tmp;
		data += 2 * sizeof(uint16_t);
		hash += hash >> 11;
	}

	/* Handle end cases */
	switch (rem) {
	case 3:
		hash += get16bits(data);
		hash ^= hash << 16;
		hash ^= data[sizeof(uint16_t)] << 18;
		hash += hash >> 11;
		break;
	case 2:
		hash += get16bits(data);
		hash ^= hash << 11;
		hash += hash >> 17;
		break;
	case 1:
		hash += *data;
		hash ^= hash << 10;
		hash += hash >> 1;
	}

	/* Force "avalanching" of final 127 bits */
	hash ^= hash << 3;
	hash += hash >> 5;
	hash ^= hash << 2;
	hash += hash >> 15;
	hash ^= hash << 10;

	return hash;
}
#endif

/* initialize the list_heads in table */
int flowtable_init(struct flow_table *table, unsigned int buckets)
{
	unsigned int i;

	table->buckets = buckets;

	if ((table->entries =
	     (struct list_head_safe **)malloc(sizeof(struct list_head_safe *) *
					      buckets)) == NULL) {
		fprintf(stderr, "flowable_init: could not allocate memory for buckets\n");
		return -1;
	}

	for (i = 0; i < buckets; ++i) {
		if ((table->entries[i] = (struct list_head_safe *)
		     malloc(sizeof(struct list_head_safe))) == NULL) {
			fprintf(stderr, "flowtable_init: could not allocate memory for flows!\n");
			return -1;
		}
		INIT_LIST_HEAD_SAFE((table->entries[i]));
	}

	INIT_LIST_HEAD_SAFE(&(table->freelist));

	table->free=0; table->busy=0;

	return 0;
}

int flowtable_add_flow(struct flow_table *ft, unsigned char *payload,
		       unsigned int len, const unsigned char flowid[],
		       unsigned int idlen)
{
	unsigned int i, found, n;
	struct list_head *pos;
	struct flow_content *tmp;

	i = (hash(flowid, idlen) % ft->buckets);

	/* see if the flow already exist */
	found = 0;
	safe_list_lock((ft->entries[i]));
	list_for_each(pos, &(ft->entries[i]->list)) {
		tmp = list_entry(pos, struct flow_content, list);

		if (memcmp(tmp->flowid, flowid, idlen) == 0) {
			found = 1;
			break;
		}
	}

	if (found) {
		if (tmp->bytes == MAX_PAYLOAD) {
			safe_list_unlock((ft->entries[i]));
			return FULL_FLOW;
		}

		n = (MAX_PAYLOAD - tmp->bytes);
		if (n > len) {
			memcpy((tmp->payload + tmp->bytes), payload, len);
			tmp->bytes += len;
		} else {
			memcpy((tmp->payload + tmp->bytes), payload, n);
			tmp->bytes += n;
		}
		safe_list_unlock((ft->entries[i]));
		return OLD_FLOW;
	}
	safe_list_unlock((ft->entries[i]));

	/* empty bucket or collision in either case insert a new flowcontent */

	/* anything on freelist? */
	safe_list_lock(&(ft->freelist));
	if (list_empty(&(ft->freelist.list))) {
		safe_list_unlock(&(ft->freelist));

		if ((tmp =
		     (struct flow_content *)malloc(sizeof(struct flow_content)))
		    == NULL) {
			fprintf(stderr, "flowtable_add_flow: could not allocate memory for flow record!\n");
			fprintf(stderr, "free items in flow table= %d busy items in flow table= %d\n", ft->free, ft->busy);
			return ERROR;
		}
		memset(tmp, 0, sizeof(struct flow_content));

		if ((tmp->payload =
		     (unsigned char *)malloc(MAX_PAYLOAD)) == NULL) {
			fprintf(stderr, "flowtable_add_flow: could not allocate memory for payload!\n");
			fprintf(stderr, "free items in flow table= %d busy items in flow table= %d\n", ft->free, ft->busy);
			return ERROR;
		}
		memset(tmp->payload, 0, MAX_PAYLOAD);

		INIT_LIST_HEAD(&(tmp->list));
		memcpy(tmp->flowid, flowid, idlen);
		if (len > MAX_PAYLOAD) {
			memcpy(tmp->payload, payload, MAX_PAYLOAD);
			tmp->bytes = MAX_PAYLOAD;
		} else {
			memcpy(tmp->payload, payload, len);
			tmp->bytes = len;
		}

		tmp->itime = time(NULL);
		safe_list_lock((ft->entries[i]));
		list_add(&(tmp->list), &(ft->entries[i]->list));
		++(ft->busy);
		safe_list_unlock((ft->entries[i]));
		return NEW_FLOW;
	}

	/* use the memory left in freelist */
	pos = ft->freelist.list.next;
	list_del_init(pos);
	--(ft->free);
	safe_list_unlock(&(ft->freelist));

	tmp = list_entry(pos, struct flow_content, list);
	memset(tmp->payload, 0, MAX_PAYLOAD);
	memset(tmp->flowid, 0, FLOWID_LEN);
	memcpy(tmp->flowid, flowid, idlen);

	if (len > MAX_PAYLOAD) {
		memcpy(tmp->payload, payload, MAX_PAYLOAD);
		tmp->bytes = MAX_PAYLOAD;
	} else {
		memcpy(tmp->payload, payload, len);
		tmp->bytes = len;
	}

	tmp->itime = time(NULL);
	safe_list_lock((ft->entries[i]));
	list_add(&(tmp->list), &(ft->entries[i]->list));
	++(ft->busy);
	safe_list_unlock((ft->entries[i]));
	return NEW_FLOW;
}

void flowtable_del_flow(struct flow_table *table, struct list_head *item)
{
	safe_list_lock(&(table->freelist));
	list_move(item, &(table->freelist.list));
	++(table->free);
	--(table->busy);
	safe_list_unlock(&(table->freelist));
}

void flowtable_add_to_freelist(struct flow_table *table, struct list_head *item)
{
	safe_list_lock(&(table->freelist));
	list_add(item, &(table->freelist.list));
	++(table->free);
	--(table->busy);
	safe_list_unlock(&(table->freelist));
}

void flowtable_finit(struct flow_table *ft)
{

	unsigned int i;
	struct list_head *p, *q;
	struct flow_content *tmp;

	/* don't wait for locks to be released in this function.
	 * let the user figure out that, otherwise an unclean 
	 * exit may hang the program
	 */
	if(!safe_list_trylock(&(ft->freelist))){
		list_for_each_safe(p, q, &(ft->freelist.list)) {
			tmp = list_entry(p, struct flow_content, list);

			list_del(p);
			free(tmp->payload);
			free(tmp);
		}
		safe_list_unlock(&(ft->freelist));
	}

	for (i = 0; i < ft->buckets; ++i) {
		if(!safe_list_trylock((ft->entries[i]))){
			list_for_each_safe(p, q, &(ft->entries[i]->list)) {
				tmp = list_entry(p, struct flow_content, list);

				list_del(p);
				free(tmp->payload);
				free(tmp);
			}
			safe_list_unlock((ft->entries[i]));
		}
	}

	free(ft->entries);
}
