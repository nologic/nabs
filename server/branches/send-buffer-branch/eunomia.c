#include <fftw3.h>
#include <errno.h>

#include "eunomia.h"
#include "list.h"
#include "flowtable.h"
#include "flow_processor.h"
#include "featureset.h"
#include "packet_capture.h"

#define NETFILTER 	"(tcp or udp)"
#define SEND_BUFFER_SIZE 	1280
#define SEND_BUFFERS		8

static int keep_going = 1;
static const unsigned int max_nr_attr = 7;

int main(int argc, char **argv)
{
	struct producer_config pc;
	struct consumer_config cc;
	struct flow_table flowtable;
	pthread_t tid[4];

	if (argc != 2) {
		fprintf(stderr, "usage: %s <model-file>\n", argv[0]);
		return ERROR;
	}

	if (setup_packet_capture(&pc, NETFILTER) != 0) {
		fprintf(stderr,
			"%s: error setting up packet capture interface!\n",
			argv[0]);
		return ERROR;
	}

	if (listen_for_requests(&cc, SERVER_PORT) != 0) {
		fprintf(stderr, "%s: error listening for client requests!\n",
			argv[0]);
		return ERROR;
	}

	if ((cc.model = svm_load_model(argv[1])) == 0) {
		fprintf(stderr, "could not open model file %s\n", argv[1]);
		return ERROR;
	}

	/* everything is sent in network byte order */
	cc.n_max_payload = htonl(MAX_PAYLOAD);

	if (init_send_buffers(&cc.sb, sizeof(struct flow_record), 10, SEND_BUFFERS) != 0) {
		fprintf(stderr, "could not initialize send buffers!\n");
		svm_destroy_model(cc.model);
		close_pcap_interface(&pc.pcap);
		return 1;
	}
	/* initialize tables and stuff */
	flowtable_init(&flowtable, FLOW_ENTRIES, &cc.sb);
	cc.keep_going = &keep_going;
	pc.keep_going = &keep_going;
	cc.flowtable = &flowtable;
	pc.flowtable = &flowtable;

	if ((cc.x =
	     (struct svm_node *)malloc(max_nr_attr *
				       sizeof(struct svm_node))) == NULL) {
		fprintf(stderr, "could not allocate memory for svm_node!\n");
		flowtable_finit(&flowtable);
		svm_destroy_model(cc.model);
		close_pcap_interface(&pc.pcap);
		return 1;
	}


	signal(SIGINT, siginthandler);
	signal(SIGPIPE, SIG_IGN);

	/* let the games begin! */
	if (pthread_create(&tid[0], NULL, produce_flowtable, (void *)&pc) != 0) {
		fprintf(stderr, "could not spawn thread\n");
		return ERROR;
	}

	if (pthread_create(&tid[1], NULL, consume_flowtable, (void *)&cc) != 0) {
		fprintf(stderr, "could not spawn thread\n");
		return ERROR;
	}

	if (pthread_create
	    (&tid[2], NULL, accept_connections_for_flows, (void *)&cc) != 0) {
		fprintf(stderr, "%s: could not spawn thread\n", argv[0]);
		return ERROR;
	}
#if 0
	if (pthread_create(&tid[3], NULL, write_to_clients, (void *)&cc) != 0) {
		fprintf(stderr, "could not spawn thread\n");
		return ERROR;
	}
	/* done with everything */
	for (i = 0; i < 4; ++i)
		if (pthread_join(tid[i], NULL) != 0)
			fprintf(stderr, "%s: error waiting for thread %ld\n",
				argv[0], tid[i]);
#endif
	write_to_clients((void *)&cc);
	svm_destroy_model(cc.model);
	flowtable_finit(&flowtable);

	print_pcap_stats(stdout, &pc.pcap);
	close_pcap_interface(&pc.pcap);

	return 0;
}

int setup_packet_capture(struct producer_config *pc, char *filter)
{

	if (init_pcap_interface(&pc->pcap) != 0) {
		fprintf(stderr,
			"error initializing packet capture interface!\n");
		return -1;
	}

	if (open_pcap_interface(&pc->pcap) != 0) {
		fprintf(stderr, "error opening packet capture interface!\n");
		return -1;
	}

	(void)setgid(getgid());
	(void)setuid(getuid());

	if (set_packet_filter(&pc->pcap, filter) != 0) {
		fprintf(stderr, "error setting packet filter!\n");
		return -1;
	}

	return 0;
}

int listen_for_requests(struct consumer_config *cc, int port)
{

	if (init_connection_pair(&cc->clients, 5) != 0)
		return -1;
	if (listen_for_connections(&(cc->clients.local), port, 5) != 0)
		return -1;

	fprintf(stderr, "Eunomia server now listening...\n");
	return 0;
}

void siginthandler(int n)
{
	signal(SIGINT, siginthandler);
	fprintf(stderr, "(%d) caught SIGINT and shutting things down now!\n", n);
	keep_going = 0;
}

void *write_to_clients(void *c)
{
	struct consumer_config *cc = (struct consumer_config *)c;

	fprintf(stderr, "in write_to_clients()\n");
	while (*cc->keep_going)
		flush_send_buffers(&cc->sb, &cc->clients);

	return NULL;
}
