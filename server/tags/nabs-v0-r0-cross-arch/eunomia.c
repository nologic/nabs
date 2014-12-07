#include <fftw3.h>
#include <errno.h>

#include "eunomia.h"
#include "list.h"
#include "flowtable.h"
#include "flow_processor.h"
#include "netmisc.h"
#include "featureset.h"
#include "packet_capture.h"

#define NETFILTER 	"((tcp or udp) and (len > 128))"
#define EU_LOG "eunomia.log"

static int keep_going = 1;
const static unsigned int max_nr_attr = 7;
const static char* model_file = "fcc.model";

#ifdef EU_STATS
static	pthread_mutex_t update_mutex;
static	struct eu_stats stats;
#endif

int main(int argc, char **argv)
{
	struct producer_config pc;
	struct consumer_config cc;
	struct flow_table flowtable;
	pthread_t tid[4];
	int i;

	if (argc != 2) {
		fprintf(stderr, "Usage: %s <network_interface>\n", argv[0]);
		fprintf(stderr, "For example: %s eth0\n", argv[0]);
		fprintf(stderr, "\t see %s for results\n", EU_LOG);
		return ERROR;
	}

	if(setup_packet_capture(&pc, argv[1], NETFILTER) != 0){
		fprintf(stderr, "%s: error setting up packet capture interface!\n", argv[0]);
		return ERROR;
	}

	if(listen_for_requests(&cc, SERVER_PORT) != 0){
		fprintf(stderr, "%s: error listening for client requests!\n", argv[0]);
		return ERROR;
	}

	if ((cc.model = svm_load_model(model_file)) == 0) {
		fprintf(stderr, "could not open model file %s\n", model_file);
		return ERROR;
	}

	signal(SIGINT, siginthandler);
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
	cc.keep_going= &keep_going; pc.keep_going= &keep_going;
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
	if (pthread_create(&tid[0], NULL, accept_connections_for_flows, (void *)&cc) != 0) {
		fprintf(stderr, "%s: could not spawn thread\n", argv[0]);
		return ERROR;
	}

	if (pthread_create(&tid[1], NULL, produce_flowtable, (void *)&pc) != 0) {
		fprintf(stderr, "could not spawn thread\n");
		return ERROR;
	}

	consume_flowtable(&cc);

	fprintf(stderr, "waiting for threads to quit....\n");
	/* done with everything */
	for(i=0; i < 2; ++i)
		if (pthread_join(tid[i], NULL) != 0)
			fprintf(stderr, "%s: error waiting for thread %ld\n", argv[0], tid[i]);

	svm_destroy_model(cc.model);
	flowtable_finit(&flowtable);

	print_pcap_stats(stdout, &pc.pcap);
	close_pcap_interface(&pc.pcap);

	return 0;
}

int setup_packet_capture(struct producer_config *pc, const char* device, const char* filter){

	if(init_pcap_interface(&pc->pcap, device) != 0){
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

int listen_for_requests(struct consumer_config *cc, int port){
	
	if(init_connection_pair(&cc->clients, 5) != 0)
		return -1;
	if(listen_for_connections(&(cc->clients.local), port, 5) != 0)
		return -1;

	fprintf(stderr, "Eunomia server now listening...\n");
	return 0;
}

void siginthandler(int n)
{
	signal(SIGINT, siginthandler);
	fprintf(stderr, "caught SIGINT and shutting things down now!\n");
	keep_going = 0;
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
