#ifndef FLOW_PROCESSOR_H
#define FLOW_PROCESSOR_H


#include <stdlib.h>
#include <string.h>
#include <netinet/ip.h>
#include <pcap.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <netinet/tcp.h>
#include <netinet/udp.h>
#include <arpa/inet.h>
#include <net/ethernet.h>
#include "packet_capture.h"
#include "flowtable.h"
#include "connections.h"
#include "featureset.h"

/* a packet */
struct eu_packet {
	unsigned char flowid[FLOWID_LEN];	/* srcip:dstip:srcport:dstport */
	struct in_addr srcip;
	struct in_addr dstip;
	unsigned short int srcport;
	unsigned short int dstport;
	unsigned int key;

	unsigned int len;
	unsigned char *payload;
};

/* results */
struct eu_results {
	struct list_head list;
	unsigned char flowid[FLOWID_LEN];
	unsigned int type;
};

#ifdef EU_STATS
struct eu_stats {
	unsigned int resident;
	unsigned int newflows;
	unsigned int oldflows;
	unsigned int fullflows;
	unsigned int noflows;
	unsigned int errors;
	unsigned int classified;
	unsigned int removed;

	unsigned long int lapsed_time;
};
#endif

struct producer_config{
	int *keep_going;
	struct pcap_config pcap;
	struct flow_table  *flowtable;
#ifdef EU_STATS
	pthread_mutex_t *update_mutex;
	struct eu_stats *stats;
#endif
};

struct consumer_config{
	int *keep_going;
	struct flow_table  *flowtable;
	struct connection_pair clients;

	struct svm_model *model;
	struct svm_node *x;
#ifdef EU_STATS
	pthread_mutex_t *update_mutex;
	struct eu_stats *stats;
#endif
	unsigned int n_max_payload;
};

int schedule_flow(struct producer_config *, struct eu_packet *);
void *produce_flowtable(void *);
void *consume_flowtable(void *);
int classify(const struct flow_content *, featureset *, struct consumer_config *);
void* accept_connections_for_flows(void *);
int get_flowid(const unsigned char*, unsigned int, struct eu_packet *);
void print_flowid(FILE *, const unsigned char *, unsigned int);

#endif
