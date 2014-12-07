#ifndef _EUNOMIA_H
#define _EUNOMIA_H

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <signal.h>
#include <errno.h>
#include <time.h>
#include <unistd.h>
#include <pcap.h>
#include <sys/socket.h>
#include <sys/types.h>
#include <arpa/inet.h>
#include <netinet/in.h>
#include <netinet/if_ether.h>
#include <netinet/ip.h>
#include <netinet/tcp.h>
#include <netinet/udp.h>

#include "packet_capture.h"
#include "flowtable.h"
#include "connections.h"
#include "featureset.h"
#include "list.h"
#include "safe_list.h"
#include "flowtable.h"
#include "lossy.h"

#define STAT_TIME		60	/* seconds */

#ifndef SERVER_PORT
#define SERVER_PORT		1986
#endif

struct eu_config {

	char *netfilter;
	unsigned int throttle;	/* to throttle or not to throttle? */
	struct lossy_table th_table;
	struct lossy_table lf_table;

};

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
	int signal;
	struct pcap_config pcap;
	struct flow_table  *flowtable;
#ifdef EU_STATS
	pthread_mutex_t *update_mutex;
	struct eu_stats *stats;
#endif
};

struct consumer_config{
	int signal;
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

void process_packet(u_char *, const struct pcap_pkthdr *, const u_char *);
int schedule_flow(struct producer_config *, struct eu_packet *);
void *produce_flowtable(void *);
void *consume_flowtable(void *);
int classify(const struct flow_content *, featureset *, struct consumer_config *);
void* accept_connections_for_flows(void *);
int get_flowid(const unsigned char*, unsigned int, struct eu_packet *);
void print_flowid(FILE *, const unsigned char *, unsigned int);
int setup_packet_capture(struct producer_config *, char*);
int listen_for_requests(struct consumer_config *, int);
void sighuphandler(int);
void siginthandler(int);
void clean_exit(int);

#ifdef HANDLE_IDLE_JOBS
int handle_idle_jobs();
#endif

#ifdef EU_STATS
void sigalarmhandler(int);
#endif

#endif
