#include <stdio.h>
#include <stdlib.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <unistd.h>
#include <glob.h>
#include <fftw3.h>
#include <featureset.h>

#define DAT_SIZE 16384

int main(int argc, char **argv)
{
	unsigned int size = DAT_SIZE;
	unsigned int i;
	featureset f;
	int fd;
	struct timeval start, end;
	unsigned char *mydata;
	glob_t gbuf;

	if (argc != 2) {
		fprintf(stderr, "usage: %s <dirname>\n", argv[0]);
		return 1;
	}

	if ((mydata = (unsigned char *)malloc(size)) == NULL) {
		fprintf(stderr, "mydata could not be accomodated!\n");
		return 1;
	}

	glob(argv[1], 0, NULL, &gbuf);
	init_featureset(mydata, size, &f);

	fprintf(stdout, "mean\tvariance\tentropy\tpower\tfmean\tfvariance\n");

	for (i = 0; (i < gbuf.gl_pathc); ++i) {

		fd = open(gbuf.gl_pathv[i], O_RDONLY);

		//fprintf(stdout, "%s\t", gbuf.gl_pathv[i]);
		read(fd, mydata, size);

		gettimeofday(&start, NULL);
		compute_features(mydata, size, &f);
		gettimeofday(&end, NULL);

		normalize_features(&f, size);
		fprintf(stdout, "%1.15e\t", f.mean);
		fprintf(stdout, "%1.15e\t", f.variance);
		fprintf(stdout, "%1.15e\t", f.entropy);
		fprintf(stdout, "%1.15e\t", f.power);
		fprintf(stdout, "%1.15e\t", f.fmean);
		fprintf(stdout, "%1.15e\n", f.fvariance);

		close(fd);
	}

	finit_featureset(&f);
	free(mydata);

	return 0;
}
