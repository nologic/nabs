#ifndef PACKET_CAPTURE_H
#define PACKET_CAPTURE_H

#include <pcap.h>

struct pcap_config{
	pcap_t *descr;
	char* dev;
	char errbuf[PCAP_ERRBUF_SIZE];
	bpf_u_int32 mask, net;
	struct bpf_program filter;
};

int init_pcap_interface(struct pcap_config *);
int open_pcap_interface(struct pcap_config *);
int close_pcap_interface(struct pcap_config *);
int set_packet_filter(struct pcap_config *, char*);
void print_pcap_stats(FILE* fp, struct pcap_config *p);

#endif
