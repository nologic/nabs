#include <string.h>
#include "packet_capture.h"

int init_pcap_interface(struct pcap_config *p, const char *device){

    memset(p, 0, sizeof(struct pcap_config));

    if(device == NULL){
    	if ((p->dev = pcap_lookupdev(p->errbuf)) == NULL) {
        	fprintf(stderr, "pcap_lookupdev(): %s\n", p->errbuf);
        	return -1;
    	}
    }else{
        p->dev = strdup(device);
    }

    /* get network mask options */
    if (pcap_lookupnet(p->dev, &p->net, &p->mask, p->errbuf) == -1) {
        fprintf(stderr, "pcap_lookupnet(): %s\n", p->errbuf);
        return -1;
    }

	return 0;
}

int open_pcap_interface(struct pcap_config *p){

    if ((p->descr = pcap_open_live(p->dev, BUFSIZ, 1, -1, p->errbuf)) == NULL) {
        fprintf(stderr, "pcap_open_live: %s\n", p->errbuf);
        return -1;
    }

    fprintf(stdout, "Listening on network device: %s\n", p->dev);
	return 0;
}

int close_pcap_interface(struct pcap_config *p){
	pcap_close(p->descr);
	return 0;
}

int set_packet_filter(struct pcap_config *p, const char* str){
    if (pcap_compile(p->descr, &p->filter, str, 1, p->net) == -1) {
        fprintf(stderr, "pcap_compile(): %s\n", pcap_geterr(p->descr));
        return -1;
    }

    if (pcap_setfilter(p->descr, &p->filter) == -1) {
        fprintf(stderr, "pcap_setfilter(): %s\n", pcap_geterr(p->descr));
        return -1;
    }

	return 0;
}

void print_pcap_stats(FILE *fp, struct pcap_config *p)
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
