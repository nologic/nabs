#include <netmisc.h>

/* given the packet, return the source IP address 
   returned address is a pointer to statically allocated
   storage which will be overwritten by subsequent calls
*/
u_char *get_srcip(const u_char * packet)
{

	struct ether_header *eptr;
	struct ip *iphdr;
	u_int offset;
	static u_char srcip[IPADDR_BUFFER_SIZE];

	eptr = (struct ether_header *)packet;

	/* is it an IP packet */
	if (ntohs(eptr->ether_type) == ETHERTYPE_IP) {
		iphdr = (struct ip *)(packet + sizeof(struct ether_header));
		offset = ntohs(iphdr->ip_off);

		if ((offset & 0x1fff) == 0) {	/* we use only the first fragment */
			memset(srcip, 0, IPADDR_BUFFER_SIZE);
			strncpy(srcip, inet_ntoa(iphdr->ip_src),
				IPADDR_BUFFER_SIZE);
			return (srcip);
		}
	}
	/* not an IP packet */
	return NULL;
}

/* given the packet, return the dest IP address 
   returned address is a pointer to statically allocated
   storage which will be overwritten by subsequent calls
*/
u_char *get_dstip(const u_char * packet)
{

	struct ether_header *eptr;
	struct ip *iphdr;
	u_int offset;
	static u_char dstip[IPADDR_BUFFER_SIZE];

	eptr = (struct ether_header *)packet;

	/* is it an IP packet */
	if (ntohs(eptr->ether_type) == ETHERTYPE_IP) {
		iphdr = (struct ip *)(packet + sizeof(struct ether_header));
		offset = ntohs(iphdr->ip_off);

		if ((offset & 0x1fff) == 0) {	/* we use only the first fragment */
			memset(dstip, 0, IPADDR_BUFFER_SIZE);
			strncpy(dstip, inet_ntoa(iphdr->ip_dst),
				IPADDR_BUFFER_SIZE);
			return (dstip);
		}
	}
	/* not an IP packet */
	return NULL;
}

/* get both addreses at once given couple of pointer */
void get_ips(const u_char * packet, char *src_ip, char *dst_ip)
{

	struct ether_header *eptr;
	struct ip *iphdr;

	eptr = (struct ether_header *)packet;

	if (ntohs(eptr->ether_type) == ETHERTYPE_IP) {
		iphdr = (struct ip *)(packet + sizeof(struct ether_header));
		if (src_ip != NULL)
			memset(src_ip, 0, IPADDR_BUFFER_SIZE), strncpy(src_ip,
								       inet_ntoa
								       (iphdr->
									ip_src),
								       IPADDR_BUFFER_SIZE);
		if (dst_ip != NULL)
			memset(dst_ip, 0, IPADDR_BUFFER_SIZE), strncpy(dst_ip,
								       inet_ntoa
								       (iphdr->
									ip_dst),
								       IPADDR_BUFFER_SIZE);
	}
}

/* given a packet, return the source port number */
int get_srcport(const u_char * packet)
{

	struct ether_header *eptr;
	struct ip *ipptr;
	struct tcphdr *tcpptr;
	struct udphdr *udpptr;
	u_int offset;

	eptr = (struct ether_header *)packet;

	if (ntohs(eptr->ether_type) == ETHERTYPE_IP) {
		ipptr = (struct ip *)(packet + sizeof(struct ether_header));
		offset = ntohs(ipptr->ip_off);

		/* need to check the more frag bit here */
		/* headers will only be in the first frag */
		if ((offset & 0x1FFF) == 0) {
			if (ipptr->ip_p == IPPROTO_TCP) {	/* TCP */
				tcpptr =
				    (struct tcphdr *)(packet +
						      sizeof(struct
							     ether_header) +
						      sizeof(struct ip));
				return (ntohs(tcpptr->source));
			} else if (ipptr->ip_p == IPPROTO_UDP) {	/* UDP */
				udpptr =
				    (struct udphdr *)(packet +
						      sizeof(struct
							     ether_header) +
						      sizeof(struct ip));
				return (ntohs(udpptr->source));
			}
		}		/* offset & 0x1FFF */
	}
	return -1;
}

/* given a packet, return the destination port */
int get_dstport(const u_char * packet)
{

	struct ether_header *eptr;
	struct ip *ipptr;
	struct tcphdr *tcpptr;
	struct udphdr *udpptr;
	u_int offset;

	eptr = (struct ether_header *)packet;

	if (ntohs(eptr->ether_type) == ETHERTYPE_IP) {
		ipptr = (struct ip *)(packet + sizeof(struct ether_header));
		offset = ntohs(ipptr->ip_off);

		/* need to check the more frag bit here */
		/* headers will only be in the first frag */
		if ((offset & 0x1FFF) == 0) {
			if (ipptr->ip_p == IPPROTO_TCP) {	/* TCP */
				tcpptr =
				    (struct tcphdr *)(packet +
						      sizeof(struct
							     ether_header) +
						      sizeof(struct ip));
				return (ntohs(tcpptr->dest));
			} else if (ipptr->ip_p == IPPROTO_UDP) {	/* UDP */
				udpptr =
				    (struct udphdr *)(packet +
						      sizeof(struct
							     ether_header) +
						      sizeof(struct ip));
				return (ntohs(udpptr->dest));
			}
		}		/* offset & 0x1FFF */
	}
	return -1;
}

/* given a packet return all four fields of any TCP/UDP packet */
void get_quads(const u_char * packet, char *src_ip, char *dst_ip, int *src_port,
	       int *dst_port)
{

	struct ether_header *eptr;
	struct ip *ipptr;
	struct tcphdr *tcpptr;
	struct udphdr *udpptr;
	u_int offset;

	if (packet == NULL)
		return;

	eptr = (struct ether_header *)packet;

	/* if ip */
	if (ntohs(eptr->ether_type) == ETHERTYPE_IP) {
		ipptr = (struct ip *)(packet + sizeof(struct ether_header));
		if (src_ip != NULL)
			memset(src_ip, 0, IPADDR_BUFFER_SIZE), strncpy(src_ip,
								       inet_ntoa
								       (ipptr->
									ip_src),
								       IPADDR_BUFFER_SIZE);
		if (dst_ip != NULL)
			memset(dst_ip, 0, IPADDR_BUFFER_SIZE), strncpy(dst_ip,
								       inet_ntoa
								       (ipptr->
									ip_dst),
								       IPADDR_BUFFER_SIZE);

		offset = ntohs(ipptr->ip_off);
		if ((offset & 0x1FFF) == 0) {	/* first frag bug: also check MF */
			switch (ipptr->ip_p) {
			case IPPROTO_TCP:
				tcpptr =
				    (struct tcphdr *)(packet +
						      sizeof(struct
							     ether_header) +
						      sizeof(struct ip));
				if (src_port != NULL)
					*src_port = ntohs(tcpptr->source);
				if (dst_port != NULL)
					*dst_port = ntohs(tcpptr->dest);
				break;

			case IPPROTO_UDP:
				udpptr =
				    (struct udphdr *)(packet +
						      sizeof(struct
							     ether_header) +
						      sizeof(struct ip));
				if (src_port != NULL)
					*src_port = ntohs(udpptr->source);
				if (dst_port != NULL)
					*dst_port = ntohs(udpptr->dest);
				break;

			default:
				*src_port = -1, *dst_port = -1;
				break;
			}	/* switch */
		}		/* (offset & 0x1FF... */
	}
	/* ntohs(eptr.. */
}

/* returns a pointer to the payload */
u_char *payload(u_char * packet, int which)
{

	struct ether_header *eptr;
	struct ip *ipptr;
	u_int offset;

	if (packet == NULL)
		return NULL;

	/* which payload */
	switch (which) {
	case DATALINK_PAYLOAD:
		return (packet + sizeof(struct ether_header));
		break;

	case DATAGRAM_PAYLOAD:
		eptr = (struct ether_header *)packet;

	      datagram:
		switch (ntohs(eptr->ether_type)) {
		case ETHERTYPE_IP:
			return (packet + sizeof(struct ether_header) +
				sizeof(struct ip));
			break;

		case ETHERTYPE_ARP:
			return (packet + sizeof(struct ether_header) + 16);
			break;

		default:
			return NULL;
			break;
		}		/* switch(ntohs(eptr... */
		break;

	case SESSION_PAYLOAD:
		eptr = (struct ether_header *)packet;
		switch (ntohs(eptr->ether_type)) {
		case ETHERTYPE_IP:
			ipptr =
			    (struct ip *)(packet + sizeof(struct ether_header));
			offset = ntohs(ipptr->ip_off);
			if ((offset & 0x1FFF) == 0) {	/* first frag bug: also check MF some os reset offset to 0x0 */
				switch (ipptr->ip_p) {
				case IPPROTO_TCP:
					return (packet +
						sizeof(struct ether_header) +
						sizeof(struct ip) +
						sizeof(struct tcphdr));
					break;
				case IPPROTO_UDP:
					return (packet +
						sizeof(struct ether_header) +
						sizeof(struct ip) +
						sizeof(struct udphdr));
					break;
				}
			} else	/* fragment so session payload 
				   is payload of datagram */
				goto datagram;

			return NULL;
			break;

		default:
			return NULL;
			break;
		}		/* switch(ntohs(eptr... */
		break;

	default:
		return NULL;
		break;
	}

}

/* returns the size of the packet payloadi, -1 on error
   packet: raw packet
   len: length of the packet
   which: which network layer
*/
int payload_len(u_char * packet, int len, int which)
{

	struct ether_header *eptr;
	struct ip *ipptr;
	u_int offset;

	if (packet == NULL)
		return -1;

	/* which payload */
	switch (which) {
	case DATALINK_PAYLOAD:
		return (len - sizeof(struct ether_header));
		break;

	case DATAGRAM_PAYLOAD:
		eptr = (struct ether_header *)packet;

	      datagram:
		switch (ntohs(eptr->ether_type)) {
		case ETHERTYPE_IP:
			return (len -
				(sizeof(struct ether_header) +
				 sizeof(struct ip)));
			break;

		case ETHERTYPE_ARP:
			return (len - (sizeof(struct ether_header) + 16));
			break;

		default:
			return -1;
			break;
		}		/* switch(ntohs(eptr... */
		break;

	case SESSION_PAYLOAD:
		eptr = (struct ether_header *)packet;
		switch (ntohs(eptr->ether_type)) {
		case ETHERTYPE_IP:
			ipptr =
			    (struct ip *)(packet + sizeof(struct ether_header));
			offset = ntohs(ipptr->ip_off);
			if ((offset & 0x1FFF) == 0) {	/* first frag bug: also check MF */
				switch (ipptr->ip_p) {
				case IPPROTO_TCP:
					return (len -
						(sizeof(struct ether_header) +
						 sizeof(struct ip) +
						 sizeof(struct tcphdr)));
					break;
				case IPPROTO_UDP:
					return (len -
						(sizeof(struct ether_header) +
						 sizeof(struct ip) +
						 sizeof(struct udphdr)));
					break;
				}
			} else	/* fragment: similar to the size of DATAGRAM_PAYLOAD */
				goto datagram;

			return -1;
			break;

		default:
			return -1;
			break;
		}		/* switch(ntohs(eptr... */
		break;

	default:
		return -1;
		break;
	}
}

/* given a tcp packet it returns which flags are set */
unsigned short int get_tcpflags(const u_char * packet)
{

	unsigned short int flags;
	struct ether_header *eptr;
	struct ip *ipptr;
	struct tcphdr *tcpptr;
	u_int offset;

	eptr = (struct ether_header *)packet;

	/* if ip */
	if (ntohs(eptr->ether_type) == ETHERTYPE_IP) {
		ipptr = (struct ip *)(packet + sizeof(struct ether_header));

		/* see if the packet is tcp */
		if (ipptr->ip_p == IPPROTO_TCP) {
			offset = ntohs(ipptr->ip_off);
			if ((offset & 0x1FFF) == 0) {	/* first frag bug: also check MF */
				tcpptr =
				    (struct tcphdr *)(packet +
						      sizeof(struct
							     ether_header) +
						      sizeof(struct ip));

				/* check the flags and inclusive or them */
				flags = 0;
				if (tcpptr->syn)
					flags |= TH_SYN;
				if (tcpptr->fin)
					flags |= TH_FIN;
				if (tcpptr->rst)
					flags |= TH_RST;
				//if(tcpptr->ack) flags|=TH_ACK;
				return flags;
			}
		}
	}
	return 0;
}
