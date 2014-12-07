
#include "eunomia.h"
#include "list.h"
#include "flow_table.h"
#include "flow_processor.h"
#include "packet_capture.h"
#include "svm.h"
#include <errno.h>

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
		return STATUS_ERROR;
	}

	if (setup_packet_capture(&pc, SENSOR_NETFILTER) != STATUS_OK) {
		fprintf(stderr, "%s: could not setup packet capture interface!\n", argv[0]);
		return STATUS_ERROR;
	}

	if (listen_for_requests(&cc, SENSOR_LISTEN_PORT) != STATUS_OK) {
		fprintf(stderr, "%s: error listening for client requests!\n", argv[0]);
		return STATUS_ERROR;
	}

	if ((cc.model = svm_load_model(argv[1])) == 0) {
		fprintf(stderr, "%s: could not open model file %s\n", argv[0], argv[1]);
		return STATUS_ERROR;
	}

	/* everything is sent in network byte order */
	cc.n_max_payload = htonl(PAYLOAD_BUFFER_LENGTH);

	/* initialize tables and stuff */
	flowtable_init(&flowtable, FLOWTABLE_BUCKETS, FLOWTABLE_RECORDS);
	cc.keep_going = &keep_going;
	pc.keep_going = &keep_going;
	cc.flowtable = &flowtable;
	pc.flowtable = &flowtable;

	if ((cc.x = (struct svm_node *)malloc(max_nr_attr * sizeof(struct svm_node))) == NULL) {
		fprintf(stderr, "%s: could not allocate memory for classifier!\n", argv[0]);
		flowtable_finit(&flowtable);
		svm_destroy_model(cc.model);
		close_pcap_interface(&pc.pcap);
		return STATUS_ERROR;
	}

	signal(SIGINT, siginthandler);
	signal(SIGPIPE, SIG_IGN);

	/* let the games begin! */
	if (pthread_create(&tid[0], NULL, produce_flowtable, (void *)&pc) != 0) {
		fprintf(stderr, "%s: could not spawn thread!\n", argv[0]);
		return STATUS_ERROR;
	}

	if (pthread_create(&tid[1], NULL, accept_connections_for_flows, (void *)&cc) != 0) {
		fprintf(stderr, "%s: could not spawn thread\n", argv[0]);
		return STATUS_ERROR;
	}

	consume_flowtable((void *)&cc);
	flowtable_finit(&flowtable);
	print_pcap_stats(stderr, &pc.pcap);
	close_pcap_interface(&pc.pcap);

	return STATUS_OK;
}

int setup_packet_capture(struct producer_config *pc, char *filter)
{

	if (init_pcap_interface(&pc->pcap) != 0) {
		fprintf(stderr,
			"error initializing packet capture interface!\n");
		return STATUS_ERROR;
	}

	if (open_pcap_interface(&pc->pcap) != 0) {
		fprintf(stderr, "error opening packet capture interface!\n");
		return STATUS_ERROR;
	}

	(void)setgid(getgid());
	(void)setuid(getuid());

	if (set_packet_filter(&pc->pcap, filter) != 0) {
		fprintf(stderr, "error setting packet filter!\n");
		return STATUS_ERROR;
	}

	return STATUS_OK;
}

int listen_for_requests(struct consumer_config *cc, int port)
{

	if (init_connection_pair(&cc->clients, 5) != 0)
		return STATUS_ERROR;
	if (listen_for_connections(&(cc->clients.local), port, 5) != 0)
		return STATUS_ERROR;

	fprintf(stderr, "Sensor now listening for client requests...\n");
	return STATUS_OK;
}

void siginthandler(int n)
{
	signal(SIGINT, siginthandler);
	fprintf(stderr, "(%d) caught SIGINT and shutting things down now!\n", n);
	keep_going = 0;
}
