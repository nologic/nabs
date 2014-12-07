#include <stdio.h>
#include <stdlib.h>
#include <fftw3.h>

int main(int argc, char **argv)
{

	unsigned char test[] = { 10, 9, 8, 7, 6, 5, 4, 3 };
	double fftin[8];
	fftw_complex *fftout;
	fftw_plan plan;
	unsigned int i, size = 8, fftout_size = 5;

	for (i = 0; i < 8; ++i)
		fftin[i] = (double)test[i];

	fprintf(stderr, "FFTin (before plan): ");
	for (i = 0; i < size; ++i)
		fprintf(stderr, "[%f]\t", fftin[i]);
	fprintf(stderr, "\n");

	fftout = fftw_malloc(fftout_size * sizeof(fftw_complex));
	plan = fftw_plan_dft_r2c_1d(size, fftin, fftout, FFTW_ESTIMATE);

	fprintf(stderr, "FFTin (after plan): ");
	for (i = 0; i < size; ++i)
		fprintf(stderr, "[%f]\t", fftin[i]);
	fprintf(stderr, "\n");

	fftw_execute(plan);

	fprintf(stderr, "FFTin (after execute): ");
	for (i = 0; i < size; ++i)
		fprintf(stderr, "[%f]\t", fftin[i]);
	fprintf(stderr, "\n");

	fprintf(stderr, "FFTout: ");
	for (i = 0; i < fftout_size; ++i)
		fprintf(stderr, "[%f, %f]\t", fftout[i][0], fftout[i][1]);
	fprintf(stderr, "\n");

	fftw_destroy_plan(plan);
	fftw_free(fftout);

	return 0;
}
