#include <stdio.h>
#include <ctype.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <glob.h>
#include <sys/stat.h>
#include <sys/types.h>
#include <fcntl.h>

#include "svm.h"
#include <fftw3.h>
#include <featureset.h>

#define DAT_SIZE 16384
const static unsigned int max_nr_attr = 7;
inline void init_svmnode(const featureset *, struct svm_node *);

struct svmscale {
	double min;
	double max;
};

struct svmscale fscale[] = {
	{0.000007, 0.015558},
	{0.000007, 0.946698},
	{0.0, 0.000488},
	{0.115077, 113845000.0},
	{0.001154, 0.695863},
	{0.005887, 28702.6}
};

int main(int argc, char **argv)
{

	struct svm_model *model;
	struct svm_node *x;
	int fd;
	unsigned int i, j;
	double v;
	unsigned int size = DAT_SIZE;
	unsigned char *mydata;
	featureset f;
	glob_t gbuf;

	if (argc != 3) {
		fprintf(stderr, "usage: %s <modelfile> <inputdir>\n", argv[0]);
		return 1;
	}

	if ((model = svm_load_model(argv[1])) == 0) {
		fprintf(stderr, "could not open model file %s\n", argv[1]);
		return 1;
	}

	glob(argv[2], 0, NULL, &gbuf);

	if ((mydata = (unsigned char *)malloc(size)) == NULL) {
		fprintf(stderr, "mydata could not be accomodated!\n");
		return 1;
	}

	init_featureset(&f, size);
	x = (struct svm_node *)malloc(max_nr_attr * sizeof(struct svm_node));

	fprintf(stdout,
		"file\ttype\tmean\tvariance\tentropy\tpower\tfmean\tfvariance\n");

	for (i = 0; (i < gbuf.gl_pathc); ++i) {

		if ((fd = open(gbuf.gl_pathv[i], O_RDONLY)) == -1) {
			fprintf(stderr, "could not open input file %s\n",
				gbuf.gl_pathv[i]);
			return 1;
		}

		read(fd, mydata, size);
		compute_features(mydata, size, &f);
		normalize_features(&f, size);
		init_svmnode(&f, x);
		v = svm_predict(model, x);

		fprintf(stdout, "%s\t", gbuf.gl_pathv[i]);
		fprintf(stdout, "%g\t", v);
		for (j = 0; j < max_nr_attr; ++j)
			fprintf(stdout, "%g\t", x[j].value);
		fprintf(stdout, "\n");

		close(fd);
	}

	finit_featureset(&f);
	svm_destroy_model(model);
	free(mydata);
	free(x);
	return 0;
}

inline void init_svmnode(const featureset * f, struct svm_node *x)
{

	x[0].index = 1;
	x[0].value =
	    (-1) +
	    ((2) * (f->mean - fscale[0].min) / (fscale[0].max - fscale[0].min));
	x[1].index = 2;
	x[1].value =
	    (-1) +
	    ((2) * (f->variance - fscale[1].min) /
	     (fscale[1].max - fscale[1].min));
	x[2].index = 3;
	x[2].value =
	    (-1) +
	    ((2) * (f->entropy - fscale[2].min) /
	     (fscale[2].max - fscale[2].min));
	x[3].index = 4;
	x[3].value =
	    (-1) +
	    ((2) * (f->power - fscale[3].min) /
	     (fscale[3].max - fscale[3].min));
	x[4].index = 5;
	x[4].value =
	    (-1) +
	    ((2) * (f->fmean - fscale[4].min) /
	     (fscale[4].max - fscale[4].min));
	x[5].index = 6;
	x[5].value =
	    (-1) +
	    ((2) * (f->fvariance - fscale[5].min) /
	     (fscale[5].max - fscale[5].min));
	x[6].index = -1;
	x[6].value = 0.0;
}
