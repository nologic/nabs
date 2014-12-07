#include <fftw3.h>
#include <errno.h>

#include "eunomia.h"
#include "list.h"
#include "flowtable.h"
#include "netmisc.h"
#include "featureset.h"
#include "packet_capture.h"

#define NETFILTER 	"((tcp or udp) and (len > 128))"
#define EU_LOG "eunomia.log"

const static unsigned int max_nr_attr = 7;
static struct producer_config pc;
static struct consumer_config cc;
static struct flow_table flowtable;
static struct eu_packet ep;

#ifdef EU_STATS
static	pthread_mutex_t update_mutex;
static	struct eu_stats stats;
#endif

static int eu_exiting=0;

FILE *log_fp;

int main(int argc, char **argv)
{
	pthread_t tid[4];
	int i;

	log_fp = stderr;

	if (argc != 2) {
		fprintf(stderr, "usage: %s <model-file>\n", argv[0]);
		fprintf(stderr, "\t see %s for results\n", EU_LOG);
		return ERROR;
	}

	if(setup_packet_capture(&pc, NETFILTER) != 0){
		fprintf(stderr, "%s: error setting up packet capture interface!\n", argv[0]);
		return ERROR;
	}

	if(listen_for_requests(&cc, SERVER_PORT) != 0){
		fprintf(stderr, "%s: error listening for client requests!\n", argv[0]);
		return ERROR;
	}

	if ((cc.model = svm_load_model(argv[1])) == 0) {
		fprintf(stderr, "could not open model file %s\n", argv[1]);
		return ERROR;
	}

	cc.signal=0; pc.signal=0;
	signal(SIGINT, siginthandler);
	signal(SIGHUP, sighuphandler);
	signal(SIGPIPE, SIG_IGN);

#ifdef EU_STATS
	signal(SIGALRM, sigalarmhandler);
	alarm(STAT_TIME);
	memset(&stats, 0, sizeof(struct eu_stats));
	cc.stats= &stats; pc.stats= &stats;
	pthread_mutex_init(&(update_mutex), NULL);
	cc.update_mutex= &update_mutex; pc.update_mutex= &update_mutex;
#endif

	/* everything is sent in network byte order */
	cc.n_max_payload = htonl(MAX_PAYLOAD);

	/* initialize tables and stuff */
	flowtable_init(&flowtable, FLOW_ENTRIES);
	cc.flowtable= &flowtable; pc.flowtable= &flowtable;

	if ((cc.x =
	     (struct svm_node *)malloc(max_nr_attr *
				       sizeof(struct svm_node))) == NULL) {
		fprintf(stderr, "could not allocate memory for svm_node!\n");
		flowtable_finit(&flowtable);
		svm_destroy_model(cc.model);
		close_pcap_interface(&pc.pcap);
		return 1;
	}

	/* let the games begin! */
	if (pthread_create(&tid[0], NULL, accept_connections_for_flows, NULL) != 0) {
		fprintf(stderr, "%s: could not spawn thread\n", argv[0]);
		return ERROR;
	}

	if (pthread_create(&tid[1], NULL, produce_flowtable, NULL) != 0) {
		fprintf(stderr, "could not spawn thread\n");
		return ERROR;
	}

	consume_flowtable(&cc);

	fprintf(stderr, "waiting for threads to quit....\n");
	/* done with everything */
	for(i=0; i < 2; ++i)
		if (pthread_join(tid[i], NULL) != 0)
			fprintf(stderr, "%s: error waiting for thread %ld\n", argv[0], tid[i]);

	clean_exit(0);
	return 0;
}

int setup_packet_capture(struct producer_config *pc, char* filter){

	if(init_pcap_interface(&pc->pcap) != 0){
		fprintf(stderr, "error initializing packet capture interface!\n");
		return -1;
	}
	
	if(open_pcap_interface(&pc->pcap) != 0){
		fprintf(stderr, "error opening packet capture interface!\n");
		return -1;
	}
	
	(void)setgid(getgid());
	(void)setuid(getuid());

	if(set_packet_filter(&pc->pcap, filter) != 0){
		fprintf(stderr, "error setting packet filter!\n");
		return -1;
	}

	return 0;
}


/* capture packets and insert them into the flowtable
 */
void *produce_flowtable(void *p)
{
	int pcap_ret;

	while (1){

		pcap_ret= pcap_dispatch(pc.pcap.descr, -1, process_packet, NULL);

		/* handle errors and signals */
		if(pc.signal){
			fprintf(log_fp, "shutting down packet capture. recieved SIGHUP from user\n");
			return NULL;
		}

		if(pcap_ret == -1){
			fprintf(log_fp, "error while capturing packets: (%s)\n", pcap_geterr(pc.pcap.descr));
			return NULL;
		}else if (pcap_ret == -2){
			fprintf(log_fp, "done capturing packets.\n");
			return NULL;
#ifdef HANDLE_IDLE_JOBS
		}else if (pcap_ret == 0){
			handle_idle_jobs();
#endif
		}
	}

	return NULL;
}

void process_packet(u_char* user, const struct pcap_pkthdr *h, const u_char *packet){

	if (get_flowid(packet, h->caplen, &ep) == -1)
		return;
	
	if ((ep.len =
	     payload_len(packet, h->caplen, SESSION_PAYLOAD)) == -1)
		return;
	
	if ((ep.payload = payload(packet, SESSION_PAYLOAD)) == NULL)
		return;

	schedule_flow(&pc, &ep);

	return;
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
	struct flow_table* flowtable= cc.flowtable;
	featureset fs;
#ifdef EU_STATS
	struct eu_stats *stats= cc.stats;
#endif

	init_featureset(&fs, MAX_PAYLOAD);

	while (!(cc.signal)) {
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
						    (cc.update_mutex);
						++stats->removed;
						--stats->resident;
						pthread_mutex_unlock
						    (cc.update_mutex);
#endif
					}
				}
			}
			safe_list_unlock((flowtable->entries[i]));

			/* now process ready list */
			list_for_each_safe(p, q, &rlist) {
				tmp = list_entry(p, struct flow_content, list);

				list_del_init(p);
				classify(tmp, &fs, &cc);
				flowtable_add_to_freelist(flowtable, p);
#ifdef EU_STATS
				pthread_mutex_lock(cc.update_mutex);
				++stats->classified;
				--stats->resident;
				pthread_mutex_unlock(cc.update_mutex);
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
	int i;

	while((cc.signal == 0)){
		i=accept_connections(&(cc.clients));

		if(i == 1){
			fprintf(stderr, "already serving maximum clients!\n");
			sleep(10);
			continue;
		}
	
		if(i == -1)
			fprintf(stderr, "error accepting connections!\n");
	}
	

	fprintf(stderr, "stopped listening to client requests\n");
	
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
	
int listen_for_requests(struct consumer_config *cc, int port){
	
	if(init_connection_pair(&cc->clients, 5) != 0)
		return -1;
	if(listen_for_connections(&(cc->clients.local), port, 5) != 0)
		return -1;

	fprintf(log_fp, "Eunomia server now listening...\n");
	return 0;
}

#ifdef HANDLE_IDLE_JOBS
int handle_idle_jobs(void){
	/* do something quick  and return */
	return 0;
}
#endif

void sighuphandler(int n)
{
	fprintf(stderr, "caught SIGHUP and shutting things down now!\n");
	pc.signal= n;
	cc.signal= n;
	clean_exit(n);
}

void siginthandler(int n)
{
	fprintf(stderr, "caught SIGINT and shutting things down now!\n");
	pc.signal= n;
	cc.signal= n;
	clean_exit(n);
}

void clean_exit(int n){

	if(eu_exiting)
		return;
	eu_exiting = 1;

#ifdef HAVE_PCAP_BREAKLOOP
	pcap_breakloop(pc.pcap.descr);
#endif
	print_pcap_stats(log_fp, &pc.pcap);
	svm_destroy_model(cc.model);
	flowtable_finit(&flowtable);
	exit(0);
}

#ifdef EU_STATS
void sigalarmhandler(int n)
{

	pthread_mutex_lock(&update_mutex);

	fprintf(stderr,
		"current time= %ld\nnew flows= %u\nold flows= %u\nfullflows= %u\nno flows = %u\nerrors   = %u\nclassified= %u\ntrashed   = %u\nin memory = %u\nave. time = %g\n",
		time(NULL), stats.newflows, stats.oldflows, stats.fullflows,
		stats.noflows, stats.errors, stats.classified, stats.removed,
		stats.resident,
		(stats.classified ==
		 0) ? 0.0 : (double)((double)stats.lapsed_time /
				     (double)stats.classified));
	stats.newflows = 0;
	stats.oldflows = 0;
	stats.fullflows = 0;
	stats.errors = 0;
	stats.noflows = 0;
	stats.classified = 0;
	stats.removed = 0;
	stats.lapsed_time = 0;
	pthread_mutex_unlock(&update_mutex);

	fprintf(stderr, "\n");
	alarm(STAT_TIME);
}
#endif
