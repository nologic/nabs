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

#include <netinet/tcp.h>
#include <time.h>
#include <unistd.h>

#include "eunomia_config.h"
#include "list.h"
#include "safe_list.h"
#include "flow_processor.h"

struct eu_config {
	char *netfilter;
	unsigned int throttle;	/* to throttle or not to throttle? */

};

int setup_packet_capture(struct producer_config *, char *);
int listen_for_requests(struct consumer_config *, int);
int listen_for_requests(struct consumer_config *, int);
void siginthandler(int);
void *write_to_clients(void *);

#endif
