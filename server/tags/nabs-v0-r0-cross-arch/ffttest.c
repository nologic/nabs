#include <stdio.h>
#include <stdlib.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <unistd.h>
#include <fftw3.h>
#include <featureset.h>

#define DAT_SIZE 32678

int main(int argc, char **argv)
{
	unsigned int size = DAT_SIZE;
	unsigned int i, j;
	unsigned int dat[] =
	    { 8, 16, 32, 64, 128, 256, 512, 1024, 2048, 4096, 8192, 16384,
		32768
	};
	featureset f;
	int fd;
	struct timeval start, end;
	unsigned char *mydata = NULL;

	fd = open("1-mpg.dat", O_RDONLY);

	for (i = 0; i < 13; ++i) {
		size = dat[i];

		init_featureset(mydata, size, &f);
		fprintf(stderr, "fragment size= %u\n", size);

		j = 0;
		if ((mydata = (unsigned char *)malloc(size)) == NULL) {
			fprintf(stderr, "mydata could not be accomodated!\n");
			return 1;
		}
		while (read(fd, mydata, size) == size) {
			gettimeofday(&start, NULL);
			compute_features(mydata, size, &f);
			gettimeofday(&end, NULL);
			fprintf(stderr, "fragment %d\n", j);
			fprintf(stderr, "\ttime: %lds %ldmicros\n",
				(end.tv_sec - start.tv_sec),
				(end.tv_usec - start.tv_usec));

			fprintf(stderr, "\tmean= %1.15e\n", f.mean);
			fprintf(stderr, "\tvariance= %1.15e\n", f.variance);
			fprintf(stderr, "\tentropy= %1.15e\n", f.entropy);
			fprintf(stderr, "\tpower= %1.15e\n", f.power);
			fprintf(stderr, "\tfmean= %1.15e\n", f.fmean);
			fprintf(stderr, "\tfvariance= %1.15e\n\n", f.fvariance);
			++j;
			free(mydata);
			mydata = NULL;
			if ((mydata = (unsigned char *)malloc(size)) == NULL) {
				fprintf(stderr,
					"mydata could not be accomodated!\n");
				return 1;
			}
		}

		fprintf(stderr,
			"-----------------------------------------------------\n");
		free(mydata);
		lseek(fd, 0, SEEK_SET);
		finit_featureset(&f);
	}

	close(fd);
	return 0;
}
