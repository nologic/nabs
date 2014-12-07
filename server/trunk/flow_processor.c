#include <unistd.h>

#include "flow_processor.h"
#include "featureset.h"
#include "flow_content_ptr_list.h"


#if 0
/* capture packets and insert them into the flowtable
 */
void *produce_flowtable(void *p)
{
	u_char *packet;
	struct pcap_pkthdr hdr;
	struct eu_packet ep;
	struct producer_config *f = (struct producer_config *)p;

	while (*(f->keep_going)
	       && (packet = (u_char *) pcap_next(f->pcap.descr, &hdr)) != NULL) {

		init_eu_packet(packet, &hdr, &ep);
		flowtable_add_flow(f->flowtable, &ep);
	}

	return NULL;
}
#endif

/* capture packets and insert them into the flowtable
 */
void *produce_flowtable(void *p)
{
	struct producer_config *f = (struct producer_config *)p;

	fprintf(stdout, "Listening for packets on interface %s\n", f->pcap.dev);
	pcap_loop(f->pcap.descr, (-1), pcap_callback, p);

	return NULL;
}

void pcap_callback(u_char *arg, const struct pcap_pkthdr *hdr, const u_char *pkt){
	struct eu_packet ep;
	struct producer_config *f = (struct producer_config *)arg;

	if(pkt == NULL)
		return;

	init_eu_packet(pkt, hdr, &ep);
	flowtable_add_flow(f->flowtable, &ep);
}


static inline void flush_flow_record(struct sb_config *sb, struct flow_record *f)
{
	copy_to_send_buffer(sb, (void *)f, sizeof(struct flow_record));
}

/* go thru the flowtable and if a flow has MAX_PAYLOAD
 * data then run it by the classifier. sleep for a while
 * if we didn't process any flows.
 */
void *consume_flowtable(void *c)
{
	register unsigned int i;
	register struct list_head *p, *q;
	struct flow_content_ptr_list fcpl; /* classification list */
	LIST_HEAD(flist); /* flush list */
	struct flow_content *tmp;
	struct flow_content_ptr *fcptr;
	time_t now;
	struct consumer_config *cc = (struct consumer_config *)c;
	struct flow_table *flowtable = cc->flowtable;
	featureset fs;

	init_featureset(&fs, MAX_PAYLOAD);
	init_flow_content_ptr_list(&fcpl);

	while (*(cc->keep_going)) {
		for (i = 0; i < FLOW_ENTRIES; ++i) {
			now = time(NULL);
			safe_list_lock((flowtable->entries[i]));
			if (!list_empty(&(flowtable->entries[i]->list))) {
				list_for_each_safe(p, q, &(flowtable->entries[i]->list)) {
					tmp = list_entry(p, struct flow_content, list);

					if (((now - tmp->fr.start_time.tv_sec) > FLOW_ACTIVE_TIMEOUT) || 
							   ((now - tmp->fr.end_time.tv_sec) > FLOW_INACTIVE_TIMEOUT) ) {
						list_move(p, &flist);
					} else if (tmp->bytes == MAX_PAYLOAD) { 
						/* create a list of flows that are ready for conent
						 * classification */
						add_flow_content_ptr_list(&fcpl, tmp);
						/* FIXME: set the status to "classifying" */
					} 
				}
			}
			safe_list_unlock((flowtable->entries[i]));

			list_for_each_safe(p, q, &(fcpl.list)) {
				fcptr= list_entry(p, struct flow_content_ptr, list);
				list_del_init(p);
				classify(fcptr->ptr, &fs, cc);
				/* FIXME: unset the status from classifying */
				delete_flow_content_ptr_list(&fcpl, fcptr);
			}

			list_for_each_safe(p, q, &flist) {
				tmp = list_entry(p, struct flow_content, list);
				list_del_init(p);
				flush_flow_record(&cc->sb, &tmp->fr);
				flowtable_add_to_freelist(flowtable, p);
			}
		}		/* for(i = 0, ... */

		/* go to sleep and wake up when there is something in flowtable.
		 * not really needed, because this is the bottleneck!
		 */
	}

	finit_featureset(&fs);
	finit_flow_content_ptr_list(&fcpl);

	return NULL;
}

int classify(struct flow_content *f, featureset * fs,
	     struct consumer_config *cc)
{

	unsigned char type;

	compute_features(f->payload, f->bytes, fs);
	normalize_features(fs, f->bytes);
	init_svmnode(fs, cc->x);

	type = (unsigned char)svm_predict(cc->model, cc->x);
	f->fr.toc_kb[type-1] = f->fr.toc_kb[type-1] + 1;
	f->bytes= 0; /* must set to zero to indicate we are done using buffered payload */

	return 0;
}

void *accept_connections_for_flows(void *c)
{
	struct consumer_config *cc = (struct consumer_config *)c;
	int i;

	while (*(cc->keep_going)) {
		i = accept_connections(&(cc->clients));

		if (i == 1) {
			fprintf(stderr, "already serving maximum clients!\n");
			sleep(10);
			continue;
		}

		if (i == -1)
			fprintf(stderr, "error accepting connections!\n");
	}

	return NULL;
}

void print_flowid(FILE * fp, const unsigned char flowid[])
{
	struct in_addr src, dst;
	unsigned short int srcport, dstport;

	memcpy(&src.s_addr, flowid, 4);
	memcpy(&dst.s_addr, flowid + 4, 4);
	memcpy(&srcport, flowid + 8, 2);
	memcpy(&dstport, flowid + 10, 2);

	fprintf(fp, "[%s:%u --> ", inet_ntoa(src), srcport);
	fprintf(fp, "%s:%u]", inet_ntoa(dst), dstport);
}
