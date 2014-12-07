#include <stdio.h>
#include <stdlib.h>
#include <stdint.h>

int main(int argc, char** argv){
	unsigned char flowid[13];

	fprintf(stdout, "sizeof(time_t) = %d\n", sizeof(time_t));
	fprintf(stdout, "sizeof(uint32_t) = %d\n", sizeof(uint32_t));
	fprintf(stdout, "sizeof(max_payload) = %d\n", sizeof(unsigned int));
	fprintf(stdout, "sizeof(flowid) = %d\n", sizeof(flowid));
	fprintf(stdout, "sizeof(type) = %d\n", sizeof(unsigned char));

	return 0;
}
