#ifndef INCLUDED_FLOW_STATS_OPS_H
#define INCLUDED_FLOW_STATS_OPS_H

#include "eunomia_config.h"

static inline void buffer_packet_payload(struct flow_content *fc, struct eu_packet *ep){
	unsigned int n;

	if (fc->bytes == PAYLOAD_BUFFER_LENGTH)
		return;

	n = (PAYLOAD_BUFFER_LENGTH - fc->bytes);
	if (n > ep->payload_len) {
		memcpy((fc->payload + fc->bytes), ep->payload, ep->payload_len);
		fc->bytes += ep->payload_len;
	} else {
		memcpy((fc->payload + fc->bytes), ep->payload, n);
		fc->bytes += n;
	}
}

static inline void update_tcp_flags(struct flow_content *fc, struct eu_packet *ep){
	if((ep->tcpptr->th_flags) & TH_ACK)
		++(fc->fr.tcp_acks);
	
	if((ep->tcpptr->th_flags) & TH_PUSH)
		++(fc->fr.tcp_push);
	
	if((ep->tcpptr->th_flags) & TH_SYN)
		++(fc->fr.tcp_syns);
	
	if((ep->tcpptr->th_flags) & TH_FIN)
		++(fc->fr.tcp_fins);
	
	if((ep->tcpptr->th_flags) & TH_RST)
		++(fc->fr.tcp_rsts);
	
	if((ep->tcpptr->th_flags) & TH_URG)
		++(fc->fr.tcp_urgs);
}

static inline void update_synack_times(struct flow_content *fc, struct eu_packet *ep){
	if(fc->flags & FL_SYN_DONE)
		return;
		
	if((ep->tcpptr->th_flags) & TH_SYN){
			memcpy(&(fc->fr.syn_time), &(ep->header->ts), sizeof(struct timeval));
	}else if((ep->tcpptr->th_flags) & TH_ACK){
			memcpy(&(fc->fr.synack_time), &(ep->header->ts), sizeof(struct timeval));
			fc->flags= FL_SYN_DONE;
	}
}

static inline void update_histogram(struct flow_content *fc, struct eu_packet *ep){
	register unsigned int i;

	for(i=0; ((i < MAX_HISTOGRAM_INDEX) && (ep->header->len > histo_bucket_bound[i])); ++i);

	if(i < MAX_HISTOGRAM_INDEX)
		++(fc->fr.histogram[i]);
	else
		++(fc->fr.over_sized_packet);
}
#endif
