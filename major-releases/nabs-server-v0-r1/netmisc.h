#ifndef __NETMISC_H
#define __NETMISC_H

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

/* as in tcp.h */
#  define TH_FIN	0x01
#  define TH_SYN	0x02
#  define TH_RST	0x04
#  define TH_PUSH	0x08
#  define TH_ACK	0x10
#  define TH_URG	0x20

#define IPADDR_BUFFER_SIZE 16

#define DATALINK_PAYLOAD 1
#define DATAGRAM_PAYLOAD 2
#define SESSION_PAYLOAD  3

/* given the packet, return the source, destination IP address */
u_char *get_srcip(const u_char *);
u_char *get_dstip(const u_char *);
void get_ips(const u_char *, char *, char *);
int get_dstport(const u_char *);
int get_srcport(const u_char *);
void get_quads(const u_char *, char *, char *, int *, int *);
u_char *payload(const u_char *, int);
int payload_len(const u_char *, int, int);
unsigned short int get_tcpflags(const u_char *);
#endif
