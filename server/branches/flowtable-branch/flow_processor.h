#ifndef FLOW_PROCESSOR_H
#define FLOW_PROCESSOR_H

#include <stdlib.h>
#include <string.h>
#include <pcap.h>
#include <sys/socket.h>
#include <netinet/in_systm.h>
#include <netinet/in.h>
#include <netinet/ip.h>
#include <netinet/tcp.h>
#include <netinet/udp.h>
#include <arpa/inet.h>
#include <net/ethernet.h>

#include "packet_capture.h"
#include "flow_table.h"
#include "featureset.h"
#include "connections.h"

struct producer_config {
	int *keep_going;
	struct pcap_config pcap;
	struct flow_table *flowtable;
};

struct consumer_config {
	int *keep_going;
	struct flow_table *flowtable;
	struct connection_pair clients;
	//struct sb_config sb;

	struct svm_model *model;
	struct svm_node *x;
	unsigned int n_max_payload;
};

int schedule_flow(struct producer_config *, struct eu_packet *);
void *produce_flowtable(void *);
void *consume_flowtable(void *);
void classify(struct flow_content *, struct payload_featureset *, struct consumer_config *);
void *accept_connections_for_flows(void *);
void pcap_callback(u_char *arg, const struct pcap_pkthdr *hdr, const u_char *pkt);
void flush_flow_record(struct flow_record *, struct connection_pair *);

#endif
