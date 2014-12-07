#ifndef __LOSSY_H
#define __LOSSY_H

#include <math.h>
#include <safe_list.h>
#include <flowtable.h>

#define LC_ENTRIES 32		/* number of buckets in the entries[] hash table */

/* top-k flowids maintained by lossy-counting 
 * See "Approximate Frequency Counts over Data Streams, Gurmeet Singh Manku and Rajeev Motwani"
 * for details of analysis and theory behind it
 */
struct lossy_entry {
	struct list_head list;
	unsigned char flowid[FLOWID_LEN];
	unsigned int frequency;
	unsigned int delta;
};

struct lossy_table {
	double error;
	double support;
	unsigned int bwidth;	/* elements per bucket */
	unsigned int bcurrent;	/* current bucket id */
	unsigned int ecurrent;	/* elements in bucket bcurrent */
	unsigned long int total;	/* total elements seen so far */

	unsigned int size;	/* limit the number of lossy_entries */
	unsigned int buckets;	/* number of buckets in entries[] */
	struct list_head_safe **entries;
};

int init_lossy_table(struct lossy_table *, unsigned int, double, double);
int update_lossy_table(struct lossy_table *, unsigned int,
		       const unsigned char[], unsigned int, unsigned int);
void finit_lossy_table(struct lossy_table *);
void delete_lossy_entry(struct lossy_table *, unsigned char[], unsigned int);
void get_lossy_entries(const struct lossy_table *, struct list_head *);
unsigned int count_lossy_entries(const struct lossy_table *);
unsigned int find_lossy_entry(const struct list_head *, const unsigned char[],
			      unsigned int);
#endif
