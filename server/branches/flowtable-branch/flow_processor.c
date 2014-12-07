#include "flow_processor.h"
#include "eunomia_config.h"
#include "flow_content_ptr_list.h"
#include <unistd.h>


/* capture packets and insert them into the flowtable
 */
void *produce_flowtable(void *p)
{
	struct producer_config *f = (struct producer_config *)p;
//	struct pcap_pkthdr pkt_header;
//	struct eu_packet ep;
//	u_char *pkt;

	fprintf(stderr, "Listening for packets on interface %s\n", f->pcap.dev);
	pcap_loop(f->pcap.descr, (-1), pcap_callback, p);

#if 0
	while(f->keep_going){
		pkt = (u_char *)pcap_next(f->pcap.descr, &pkt_header);
		if(pkt != NULL){
			if(init_eu_packet(pkt, &pkt_header, &ep) == 0)
				flowtable_add_flow(f->flowtable, &ep);
		}
	}
#endif

	return NULL;
}

void pcap_callback(u_char *arg, const struct pcap_pkthdr *hdr, const u_char *pkt){
	struct eu_packet ep;
	struct producer_config *f = (struct producer_config *)arg;

	if(pkt == NULL)
		return;

	if(*(f->keep_going) == 0){
		pcap_breakloop(f->pcap.descr);
		return;
	}

	if(init_eu_packet(pkt, hdr, &ep) != 0) 
		return;

	flowtable_add_flow(f->flowtable, &ep);
}

void *consume_flowtable(void *c)
{
	register unsigned int i;
	register struct list_head *p, *q;
	register struct flow_content *tmp;

	LIST_HEAD(flushable);
	struct flow_content_ptr_list classifiable;
	struct flow_content_ptr *fcp;
	struct consumer_config *cc = (struct consumer_config *)c;
	struct flow_table *flowtable = cc->flowtable;
	struct payload_featureset fs;
	unsigned int flushed = 0;
	unsigned int classified = 0;
	unsigned int run_captures = 0;
	time_t now = time(NULL);

	init_featureset(&fs, PAYLOAD_BUFFER_LENGTH);
	init_flow_content_ptr_list(&classifiable);
	
	fprintf(stderr, "time \t in memory \t memory used \t updates \t dropped \t classified \t flushed\n");
	while (*(cc->keep_going)) {
		for (i = 0; (i < flowtable->buckets) && (*(cc->keep_going) != 0); ++i) {
			now = time(NULL);

			flowtable_lock_bucket(flowtable, i);
			if (!list_empty(&(flowtable->entries[i].list))) {
				list_for_each_safe(p, q, &(flowtable->entries[i].list)) {
					tmp = list_entry(p, struct flow_content, list);

					if (((now - tmp->fr.end_time.tv_sec) > FLOW_INACTIVE_TIMEOUT) ||
							((now - tmp->fr.start_time.tv_sec) > FLOW_ACTIVE_TIMEOUT)) {
						list_move(p, &flushable);
					} else if (tmp->bytes == PAYLOAD_BUFFER_LENGTH) { 
						add_flow_content_ptr_list(&classifiable, tmp);
					} 
				}
			}
			flowtable_unlock_bucket(flowtable, i);

			/* flush flow records that are ready! */
			list_for_each_safe(p, q, &flushable){
				tmp = list_entry(p, struct flow_content, list);
				list_del_init(p);
				flush_flow_record(&(tmp->fr), &(cc->clients));
				flowtable_add_to_freelist(flowtable, p);

				++flushed;
				pthread_mutex_lock(&flowtable->lock);
				--(flowtable->newones);
				pthread_mutex_unlock(&flowtable->lock);
			}

			/* classify flows that have enough payload */
			list_for_each_safe(p, q, &(classifiable.list)){
				fcp = list_entry(p, struct flow_content_ptr, list);
				list_del_init(p);
				classify(fcp->ptr, &fs, cc);
				++classified;
				delete_flow_content_ptr_list(&classifiable, fcp);
			}
		}
 
		++run_captures;
		if((run_captures % 2500) == 0){
			pthread_mutex_lock(&flowtable->lock);
			fprintf(stderr, "%lu \t %lu \t %.2f \t %lu \t %lu \t %u \t %u\n", now, flowtable->newones, (flowtable->newones*16.0)/1024.0,
																 flowtable->updates, flowtable->dropped, classified, flushed);
			flowtable->updates = 0;
			flowtable->dropped = 0;
			pthread_mutex_unlock(&flowtable->lock);
			flushed = 0;
			classified = 0;
			run_captures = 0;
		}

		/* go to sleep and wake up when there is something in flowtable.
		 * not really needed, because this is the bottleneck!
		 */
	}

	finit_flow_content_ptr_list(&classifiable);
	finit_featureset(&fs);

	return NULL;
}

void classify(struct flow_content *fc, struct payload_featureset *fs, struct consumer_config *cc){
	unsigned int content_type;

	compute_features(fs, fc->payload, fc->bytes);
	normalize_features(fs, fc->bytes);
	fc->bytes = 0;

	init_svmnode(fs, cc->x);
	content_type = svm_predict(cc->model, cc->x);
	++(fc->fr.toc_kb[content_type]);
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

void flush_flow_record(struct flow_record *fr, struct connection_pair *cp){
	int i;

	for (i = 0; i < cp->n_remote; ++i) {
		if (!cp->remote[i].socketfd)
			continue;

		if (write (cp->remote[i].socketfd, fr, sizeof(struct flow_record)) < 0) {
			close(cp->remote[i].socketfd);
			fclose(cp->remote[i].socketfp);
			memset(&cp->remote[i], 0, sizeof(struct connection));
			continue;
		}
	}
}
