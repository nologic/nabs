#include "packet_capture.h"

int init_pcap_interface(struct pcap_config *p)
{

	memset(p, 0, sizeof(struct pcap_config));

	if ((p->dev = pcap_lookupdev(p->errbuf)) == NULL) {
		fprintf(stderr, "pcap_lookupdev(): %s\n", p->errbuf);
		return -1;
	}

	/* get network mask options */
	if (pcap_lookupnet(p->dev, &p->net, &p->mask, p->errbuf) == -1) {
		fprintf(stderr, "pcap_lookupnet(): %s\n", p->errbuf);
		return -1;
	}

	return 0;
}

int open_pcap_interface(struct pcap_config *p)
{

	if ((p->descr =
	     pcap_open_live(p->dev, BUFSIZ, 1, -1, p->errbuf)) == NULL) {
		fprintf(stderr, "pcap_open_live: %s\n", p->errbuf);
		return -1;
	}

	return 0;
}

int close_pcap_interface(struct pcap_config *p)
{
	pcap_close(p->descr);
	return 0;
}

int set_packet_filter(struct pcap_config *p, char *str)
{
	if (pcap_compile(p->descr, &p->filter, str, 1, p->net) == -1) {
		fprintf(stderr, "pcap_compile(): %s\n", pcap_geterr(p->descr));
		return -1;
	}

	if (pcap_setfilter(p->descr, &p->filter) == -1) {
		fprintf(stderr, "pcap_setfilter(): %s\n",
			pcap_geterr(p->descr));
		return -1;
	}

	return 0;
}

void print_pcap_stats(FILE * fp, struct pcap_config *p)
{
	struct pcap_stat ps;

	if (pcap_stats(p->descr, &ps) < 0) {
		fprintf(stderr, "error when pcap_stat()\n");
		fprintf(fp, "error when pcap_stat()\n");
		return;
	}

	fprintf(fp, "packets recieved= %d\n", ps.ps_recv);
	fprintf(fp, "packets dropped = %d\n", ps.ps_drop);
	return;
}

int init_eu_packet(const unsigned char *packet, const struct pcap_pkthdr *hdr,
		   struct eu_packet *ep)
{
	u_int offset;
	u_int32_t iphlen;
	u_int32_t thlen; /* header length of tcp or udp */

	if (packet == NULL)
		return -1;

	memset(ep, 0, sizeof(*ep));
	ep->packet = packet;
	ep->header = hdr;

	ep->eptr = (struct ether_header *)packet;

	if (ntohs(ep->eptr->ether_type) == ETHERTYPE_IP) {
		ep->ipptr = (struct ip *)(packet + sizeof(struct ether_header));
		iphlen= ((ep->ipptr->ip_hl) << 2);

		offset = ntohs(ep->ipptr->ip_off);
		if ((offset & 0x1FFF) == 0) {	/* FIXME: need more robust checks here! (first frag bug: also check MF) */
			switch (ep->ipptr->ip_p) {
			case IPPROTO_TCP:
				ep->tcpptr = (struct tcphdr *)(packet + (sizeof(struct ether_header) + iphlen));
				thlen= ((ep->tcpptr->th_off) << 2);

				memcpy(ep->flowid, &(ep->ipptr->ip_src.s_addr), 4);
				memcpy(ep->flowid + 4, &(ep->ipptr->ip_dst.s_addr),
				       4);
				memcpy(ep->flowid + 8, &(ep->tcpptr->th_sport), 2);
				memcpy(ep->flowid + 10, &(ep->tcpptr->th_dport), 2);
				memcpy(ep->flowid + 12, &(ep->ipptr->ip_p), 1);

				ep->payload = (packet + sizeof(struct ether_header) + iphlen + thlen);
				ep->payload_len = hdr->caplen - (sizeof(struct ether_header) + iphlen + thlen);
				return 0;
				break;

			case IPPROTO_UDP:
				ep->udpptr = (struct udphdr *)(packet + sizeof(struct ether_header) + iphlen);

				memcpy(ep->flowid, &(ep->ipptr->ip_src.s_addr), 4);
				memcpy(ep->flowid + 4, &(ep->ipptr->ip_dst.s_addr), 4);
				memcpy(ep->flowid + 8, &(ep->udpptr->uh_sport), 2);
				memcpy(ep->flowid + 10, &(ep->udpptr->uh_dport), 2);
				memcpy(ep->flowid + 12, &(ep->ipptr->ip_p), 1);

				ep->payload = (packet + sizeof(struct ether_header) + iphlen + sizeof(struct udphdr));
				ep->payload_len = hdr->caplen - (sizeof(struct ether_header) + iphlen + sizeof(struct udphdr));
				return 0;
				break;

			default:
				memset(ep->flowid, 0, FLOWID_LEN);
				memcpy(ep->flowid, &(ep->ipptr->ip_src.s_addr), 4);
				memcpy(ep->flowid + 4, &(ep->ipptr->ip_dst.s_addr),
				       4);
				memcpy(ep->flowid + 12, &(ep->ipptr->ip_p), 1);

				ep->payload = (packet + sizeof(struct ether_header) + iphlen);
				ep->payload_len = hdr->caplen - (sizeof(struct ether_header) + iphlen);
				return 0;
				break;
			}	/* switch */
		}		/* (offset & 0x1FF... */
	}
	/* ntohs(eptr.. */
	return -1;
}
