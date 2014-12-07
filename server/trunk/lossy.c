#include <lossy.h>

int init_lossy_table(struct lossy_table *lct, unsigned int buckets,
		     double error, double support)
{
	unsigned int i = 0;

	memset(lct, 0, sizeof(struct lossy_table));

	lct->error = error;
	lct->support = support;
	lct->bwidth = (unsigned int)ceil((1 / error));
	lct->bcurrent = 1;
	lct->buckets = buckets;

	if ((lct->entries =
	     (struct list_head_safe **)malloc(sizeof(struct list_head_safe *) *
					      buckets)) == NULL) {
		fprintf(stderr, "could not allocate memory!\n");
		return -1;
	}

	for (i = 0; (i < buckets); ++i) {
		if ((lct->entries[i] = (struct list_head_safe *)
		     malloc(sizeof(struct list_head_safe))) == NULL) {
			fprintf(stderr, "could not allocate memory!\n");
			return -1;
		}
		INIT_LIST_HEAD_SAFE((lct->entries[i]));
	}

	return 0;
}

/* update_lossy_table():
 * @lct: lossy counting table to be updated
 * @key: key of eid
 * @eid: element id
 * @len: length of eid
 * @n:	frequency of eid
 * returns (-1) on errors, 1 on a new bucket boundary, 0 otherwise
 */
int update_lossy_table(struct lossy_table *lct, unsigned int key,
		       const unsigned char eid[], unsigned int len,
		       unsigned int n)
{
	unsigned int i, found, mykey;
	unsigned int buckets = lct->buckets;
	struct list_head *pos, *q;
	struct lossy_entry *tmp;

	mykey = (key % buckets);

	/* see if the flow already exist */
	found = 0;
	list_for_each(pos, &(lct->entries[mykey]->list)) {
		tmp = list_entry(pos, struct lossy_entry, list);

		if (memcmp(tmp->flowid, eid, len) == 0) {
			found = 1;
			break;
		}
	}

	/* eid already in the table, so simply increment the frequency */
	if (found) {
		tmp->frequency += n;
	} else {
		/* create a new element */
		if ((tmp =
		     (struct lossy_entry *)malloc(sizeof(struct lossy_entry)))
		    == NULL) {
			fprintf(stderr,
				"update_lctable(): could not allocate memory!\n");
			return -1;
		}
		memset(tmp, 0, sizeof(struct lossy_entry));
		INIT_LIST_HEAD(&(tmp->list));
		memcpy(tmp->flowid, eid, len);
		tmp->frequency = n;
		tmp->delta = (lct->bcurrent - 1);

		list_add(&(tmp->list), &(lct->entries[mykey]->list));
	}

	lct->ecurrent += n;
	lct->total += n;

	/* if bucket boundary then prune items */
	if (lct->ecurrent >= lct->bwidth) {
		++(lct->bcurrent);

		/* adjust for overflow from bucket */
		lct->ecurrent -= lct->bwidth;

		for (i = 0; (i < buckets); ++i) {
			list_for_each_safe(pos, q, &(lct->entries[i]->list)) {
				tmp = list_entry(pos, struct lossy_entry, list);

				//fprintf(stderr, "[delete?] [");
				//print_flowid(stderr, tmp->flowid, len);
				//fprintf(stderr, "] [(%d+%d=%d) <= (%d)]", tmp->frequency, 
				//tmp->delta, (tmp->frequency+tmp->delta), lct->bcurrent);
				if ((tmp->frequency + tmp->delta) <=
				    lct->bcurrent) {
					//fprintf(stderr, "[deleted] ");
					//print_flowid(stderr, tmp->flowid, FLOWID_LEN);
					//fprintf(stderr, "\n");
					list_del(pos);
					free(tmp);
				}
				//fprintf(stderr, "\n");
			}
		}
		return 1;
	}
	return 0;
}

void finit_lossy_table(struct lossy_table *lct)
{
	unsigned int i;
	struct list_head *p, *q;
	struct lossy_entry *tmp;

	for (i = 0; (i < lct->buckets); ++i) {
		list_for_each_safe(p, q, &(lct->entries[i]->list)) {
			tmp = list_entry(p, struct lossy_entry, list);

			list_del(p);
			free(tmp);
		}
	}
}

void delete_lossy_entry(struct lossy_table *lct, unsigned char flowid[],
			unsigned int len)
{
	unsigned int key;
	struct list_head *p, *q;
	struct lossy_entry *tmp;

	key = hash(flowid, len) % lct->buckets;

	list_for_each_safe(p, q, &(lct->entries[key]->list)) {
		tmp = list_entry(p, struct lossy_entry, list);

		if (memcmp(tmp->flowid, flowid, len) == 0) {
			list_del(p);
			free(tmp);
			return;
		}
	}
}

/* get_lossy_entries()
 * @lc_table: table to extract elements from
 * @head: list to return extracted elements
 */
void get_lossy_entries(const struct lossy_table *lct, struct list_head *head)
{
	register unsigned int i;
	struct lossy_entry *tmp, *l;
	struct list_head *pos;

	//fprintf(stderr, "allow list\n");
	//fprintf(stderr, "----------\n");
	INIT_LIST_HEAD(head);
	for (i = 0; i < lct->buckets; ++i) {
		list_for_each(pos, &(lct->entries[i]->list)) {
			tmp = list_entry(pos, struct lossy_entry, list);

			if (tmp->frequency >
			    ((lct->support - lct->error) * lct->total)) {

				if ((l = (struct lossy_entry *)
				     malloc(sizeof(struct lossy_entry))) ==
				    NULL) {
					fprintf(stderr,
						"extract_elements(): could not allocate memory!\n");
					return;
				}
				memcpy(l, tmp, sizeof(struct lossy_entry));
				INIT_LIST_HEAD(&(l->list));
				list_add(&(l->list), head);
				//fprintf(stderr, "[a] ");
				//print_flowid(stderr, tmp->flowid, 8);
				//fprintf(stderr, "\n");
			}
		}
	}
	//fprintf(stderr, "----------\n");

	return;
}

unsigned int count_lossy_entries(const struct lossy_table *lct)
{

	register unsigned int i, count;
	register struct list_head *pos;

	for (i = 0, count = 0; i < lct->buckets; ++i) {
		list_for_each(pos, &(lct->entries[i]->list)) {
			++count;
		}
	}
	return count;
}

/* find_lossy_entry():
 * @head: list head of allow list
 * @flowid: flowid to be checked
 * @len: length of flowid
 * return 1, if flowid is found in head, 0 otherwise
 */
unsigned int find_lossy_entry(const struct list_head *head,
			      const unsigned char flowid[], unsigned int len)
{
	register struct list_head *pos;
	struct lossy_entry *tmp;

	list_for_each(pos, head) {
		tmp = list_entry(pos, struct lossy_entry, list);

		if (memcmp(tmp->flowid, flowid, len) == 0)
			return 1;
	}

	return 0;
}
