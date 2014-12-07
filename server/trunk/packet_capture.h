#ifndef PACKET_CAPTURE_H
#define PACKET_CAPTURE_H

#include <stdlib.h>
#include <string.h>
#include <pcap.h>
#include <sys/socket.h>
#include <netinet/in.h>
#ifndef __FAVOR_BSD
#  define __FAVOR_BSD
#endif
#include <netinet/ip.h>
#include <netinet/tcp.h>
#include <netinet/udp.h>
#include <arpa/inet.h>
#include <net/ethernet.h>

#define FLOWID_LEN	13


/* a packet */
struct eu_packet {
	unsigned char flowid[FLOWID_LEN];	/* srcip:dstip:srcport:dstport:proto */
	const u_char *packet;
	const struct pcap_pkthdr *header;

	struct ether_header *eptr;
	struct ip			*ipptr;
	struct tcphdr		*tcpptr;
	struct udphdr		*udpptr;

	unsigned int payload_len;
	const unsigned char *payload;
};

struct pcap_config {
	pcap_t *descr;
	char *dev;
	char errbuf[PCAP_ERRBUF_SIZE];
	bpf_u_int32 mask, net;
	struct bpf_program filter;
};

int init_pcap_interface(struct pcap_config *);
int open_pcap_interface(struct pcap_config *);
int close_pcap_interface(struct pcap_config *);
int set_packet_filter(struct pcap_config *, char *);
void print_pcap_stats(FILE * fp, struct pcap_config *p);
int init_eu_packet(const unsigned char *, const struct pcap_pkthdr *, struct eu_packet *);

#endif
