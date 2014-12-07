#include <unistd.h>

#include "flow_processor.h"
#include "netmisc.h"
#include "featureset.h"

/* capture packets and insert them into the flowtable
 */
void *produce_flowtable(void *p)
{
	u_char *packet, *payloadat;
	struct pcap_pkthdr hdr;
	struct eu_packet ep;
	int len;
	struct producer_config *f= (struct producer_config *)p;

	while (*(f->keep_going)
	       && (packet = (u_char *) pcap_next(f->pcap.descr, &hdr)) != NULL) {

		if (get_flowid(packet, hdr.caplen, &ep) == -1)
			continue;
		if ((len =
		     payload_len(packet, hdr.caplen, SESSION_PAYLOAD)) == -1)
			continue;
		ep.len= len;
		if ((payloadat = payload(packet, SESSION_PAYLOAD)) == NULL)
			continue;
		ep.payload= payloadat;

		schedule_flow(f, &ep);
	}

	return NULL;
}


int schedule_flow(struct producer_config *f, struct eu_packet *p){

	int n;

		n = flowtable_add_flow(f->flowtable, p->payload, p->len, p->flowid,
				       FLOWID_LEN);
#ifdef EU_STATS
		pthread_mutex_lock(f->update_mutex);
		switch (n) {
		case OLD_FLOW:
			++f->stats->oldflows;
			break;
		case NEW_FLOW:
			++f->stats->newflows;
			++f->stats->resident;
			break;
		case FULL_FLOW:
			++f->stats->fullflows;
			break;
		case NO_FLOW:
			++f->stats->noflows;
			break;
		default:
			++f->stats->errors;
			break;
		}
		pthread_mutex_unlock(f->update_mutex);
#endif

		return 0;
}

/* go thru the flowtable and if a flow has MAX_PAYLOAD
 * data then run it by the classifier. sleep for a while
 * if we didn't process any flows.
 */
void *consume_flowtable(void *c)
{
	register unsigned int i;
	struct list_head *p, *q;
	LIST_HEAD(rlist);
	struct flow_content *tmp;
	time_t now;
	struct consumer_config *cc= (struct consumer_config *)c;
	struct flow_table* flowtable= cc->flowtable;
	featureset fs;
#ifdef EU_STATS
	struct eu_stats *stats= cc->stats;
#endif

	init_featureset(&fs, MAX_PAYLOAD);

	while (*(cc->keep_going)) {
		for (i = 0; i < FLOW_ENTRIES; ++i) {
			safe_list_lock((flowtable->entries[i]));
			if (!list_empty(&(flowtable->entries[i]->list))) {
				list_for_each_safe(p, q,
						   &(flowtable->entries[i]->
						     list)) {
					tmp =
					    list_entry(p, struct flow_content,
						       list);

					now = time(NULL);

					if (tmp->bytes == MAX_PAYLOAD) {	/* create a list of ready flows */
						list_move(p, &rlist);
					} else if ((now - tmp->itime) > FLOW_TIMEOUT) {	/* timeout reached, cleanup */
						list_del_init(p);
						flowtable_add_to_freelist
						    (flowtable, p);
#ifdef EU_STATS
						pthread_mutex_lock
						    (cc->update_mutex);
						++stats->removed;
						--stats->resident;
						pthread_mutex_unlock
						    (cc->update_mutex);
#endif
					}
				}
			}
			safe_list_unlock((flowtable->entries[i]));

			/* now process ready list */
			list_for_each_safe(p, q, &rlist) {
				tmp = list_entry(p, struct flow_content, list);

				list_del_init(p);
				classify(tmp, &fs, cc);
				flowtable_add_to_freelist(flowtable, p);
#ifdef EU_STATS
				pthread_mutex_lock(cc->update_mutex);
				++stats->classified;
				--stats->resident;
				pthread_mutex_unlock(cc->update_mutex);
#endif
			}

		}		/* for(i = 0, ... */

		/* go to sleep and wake up when there is something in flowtable.
		 * not really needed, because this is the bottleneck!
		 */
	}

	finit_featureset(&fs);
	
	return NULL;
}

int classify(const struct flow_content *f, featureset *fs, struct consumer_config *cc)
{

#ifdef EU_STATS
	struct timeval start, end;
#endif
	double v;
	int i;
	unsigned char type;
	time_t n_itime;
	struct connection *clients= cc->clients.remote;

#ifdef EU_STATS
	gettimeofday(&start, NULL);
#endif

	compute_features(f->payload, f->bytes, fs);
	normalize_features(fs, f->bytes);
	init_svmnode(fs, cc->x);

	v = svm_predict(cc->model, cc->x);
	
#ifdef EU_STATS
	gettimeofday(&end, NULL);

	pthread_mutex_lock(cc->update_mutex);
	cc->stats->lapsed_time +=
	    ((end.tv_sec - start.tv_sec) * 1000000) + (end.tv_usec -
						       start.tv_usec);
	pthread_mutex_unlock(cc->update_mutex);
#endif

	type = (unsigned char)v;
	/* SVM type range is [1,8] Mike wants it to be [0,7] for his client
	 * to work properly. He is a lazy bastard!
	 */
	--type;
	/* convert to network byte order */
	n_itime = htonl(f->itime);
	for (i = 0; i < cc->clients.n_remote; i++) {
		if (!clients[i].socketfd)
			continue;

		/* wtf? */
		if(write(clients[i].socketfd, &n_itime, sizeof(f->itime)) < 0){
			close(clients[i].socketfd);
			fclose(clients[i].socketfp);
			memset(&clients[i], 0, sizeof(struct connection));
			continue;
		}

		if(write(clients[i].socketfd, &cc->n_max_payload,
		      sizeof(cc->n_max_payload)) < 0){
			close(clients[i].socketfd);
			fclose(clients[i].socketfp);
			memset(&clients[i], 0, sizeof(struct connection));
			continue;
		}

		if(write(clients[i].socketfd, f->flowid, FLOWID_LEN) < 0){
			close(clients[i].socketfd);
			fclose(clients[i].socketfp);
			memset(&clients[i], 0, sizeof(struct connection));
			continue;
		}

		if(write(clients[i].socketfd, &type, sizeof(type))< 0){
			close(clients[i].socketfd);
			fclose(clients[i].socketfp);
			memset(&clients[i], 0, sizeof(struct connection));
			continue;
		}

	}

	return 0;
}

void* accept_connections_for_flows(void *c){
	struct consumer_config *cc= (struct consumer_config *)c;
	int i;

	while(*(cc->keep_going)){
		i=accept_connections(&(cc->clients));

		if(i == 1){
			fprintf(stderr, "already serving maximum clients!\n");
			sleep(10);
			continue;
		}
	
		if(i == -1)
			fprintf(stderr, "error accepting connections!\n");
	}

	return NULL;
}

int get_flowid(const unsigned char *packet, unsigned int len,
	      struct eu_packet *ep)
{
	struct ether_header *eptr;
	struct ip *ipptr;
	struct tcphdr *tcpptr;
	struct udphdr *udpptr;
	u_int offset;

	if (packet == NULL)
		return -1;

	eptr = (struct ether_header *)packet;
	ep->len = len;

	if (ntohs(eptr->ether_type) == ETHERTYPE_IP) {
		ipptr = (struct ip *)(packet + sizeof(struct ether_header));
		ep->srcip.s_addr = ipptr->ip_src.s_addr;
		ep->dstip.s_addr = ipptr->ip_dst.s_addr;

		offset = ntohs(ipptr->ip_off);
		if ((offset & 0x1FFF) == 0) {	/* first frag bug: also check MF */
			switch (ipptr->ip_p) {
			case IPPROTO_TCP:
				tcpptr =
				    (struct tcphdr *)(packet +
						      sizeof(struct
							     ether_header) +
						      sizeof(struct ip));
				ep->srcport = tcpptr->source;
				ep->dstport = tcpptr->dest;

				memcpy(ep->flowid, &(ep->srcip.s_addr), 4);
				memcpy(ep->flowid + 4, &(ep->dstip.s_addr), 4);
				memcpy(ep->flowid + 8, &(ep->srcport), 2);
				memcpy(ep->flowid + 10, &(ep->dstport), 2);
				ep->key = hash(ep->flowid, FLOWID_LEN);
				return 0;
				break;

			case IPPROTO_UDP:
				udpptr =
				    (struct udphdr *)(packet +
						      sizeof(struct
							     ether_header) +
						      sizeof(struct ip));
				ep->srcport = udpptr->source;
				ep->dstport = udpptr->dest;

				memcpy(ep->flowid, &(ep->srcip.s_addr), 4);
				memcpy(ep->flowid + 4, &(ep->dstip.s_addr), 4);
				memcpy(ep->flowid + 8, &(ep->srcport), 2);
				memcpy(ep->flowid + 10, &(ep->dstport), 2);
				ep->key = hash(ep->flowid, FLOWID_LEN);
				return 0;
				break;

			default:
				ep->srcport = 0;
				ep->dstport = 0;
				memset(ep->flowid, 0, FLOWID_LEN);
				ep->key = 0;
				return -1;
				break;
			}	/* switch */
		}		/* (offset & 0x1FF... */
	}
	/* ntohs(eptr.. */
	return -1;
}

void print_flowid(FILE * fp, const unsigned char flowid[], unsigned int len)
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
