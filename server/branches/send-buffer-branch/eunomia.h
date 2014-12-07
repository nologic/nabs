#ifndef _EUNOMIA_H
#define _EUNOMIA_H

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <signal.h>
#include <errno.h>
#include <pcap.h>
#include <sys/socket.h>
#include <sys/types.h>
#include <arpa/inet.h>
#include <netinet/in.h>
#include <netinet/if_ether.h>
#include <netinet/ip.h>
#include <netinet/tcp.h>
#include <time.h>
#include <unistd.h>

#include "list.h"
#include "safe_list.h"
#include "flowtable.h"
#include "flow_processor.h"

#define STAT_TIME		60	/* seconds */

#ifndef SERVER_PORT
#define SERVER_PORT		1986
#endif

struct eu_config {

	char *netfilter;
	unsigned int throttle;	/* to throttle or not to throttle? */
	//struct lossy_table th_table;
	//struct lossy_table lf_table;

};

int setup_packet_capture(struct producer_config *, char *);
int listen_for_requests(struct consumer_config *, int);
void siginthandler(int);
void *write_to_clients(void *);

#endif
