CC= gcc
CPP= g++
CFLAGS= -Wall -O3 -msse2 -march=pentium4 -mfpmath=sse -ffast-math -fomit-frame-pointer -funroll-loops -ftracer -momit-leaf-frame-pointer -DEU_STATS #-DHANDLE_IDLE_JOBS #-DUSE_MD5 #-DEU_STATS 
#CFLAGS= -Wall -ggdb -DSTATS #-fprofile-arcs -ftest-coverage -D_FILE_OFFSET_BITS=64 -DEU_THROTTLE
INCLUDES= -I. -I/usr/include/ -I/home/kulesh/prefix/include/
LDPATH= -L/home/kulesh/prefix/lib/
LIBS= -lpcap -lpthread -lm -lfftw3 #-lssl
OBJS= flowtable.o packet_capture.o connections.o featureset.o lossy.o eunomia.o netmisc.o svm.o
EXEC= eunomia
README= Readme

all:	$(OBJS) Makefile
	$(CPP) $(LDPATH) $(OBJS) -o $(EXEC) $(LIBS)
	@echo "Striping $(EXEC)..."
	@strip -s $(EXEC)
	
eunomia.o:	flowtable.h eunomia.h eunomia.c Makefile
	$(CC) $(CFLAGS) $(INCLUDES) -c eunomia.c

lossy.o:	lossy.h lossy.c Makefile
	$(CC) $(CFLAGS) $(INCLUDES) -c lossy.c

flowtable.o:	flowtable.h flowtable.c Makefile
	$(CC) $(CFLAGS) $(INCLUDES) -c flowtable.c

connections.o:	connections.h connections.c Makefile
	$(CC) $(CFLAGS) $(INCLUDES) -c connections.c

packet_capture.o:	packet_capture.h packet_capture.c Makefile
	$(CC) $(CFLAGS) $(INCLUDES) -c packet_capture.c

featureset.o:	featureset.h featureset.c Makefile
	$(CC) $(CFLAGS) $(INCLUDES) -c featureset.c

netmisc.o:	netmisc.h netmisc.c Makefile
	$(CC) $(CFLAGS) $(INCLUDES) -c netmisc.c

svm.o: svm.cpp svm.h
	$(CPP) $(CFLAGS) $(INCLUDES) -c svm.cpp

ffttest: featureset.h ffttest.c Makefile
	$(CC) $(CFLAGS) $(INCLUDES) ffttest.c -lm -lfftw3 -o ffttest
	
getfeatures: featureset.h getfeatures.c Makefile
	$(CC) $(CFLAGS) $(INCLUDES) getfeatures.c -lm -lfftw3 -o getfeatures 
	
mypredict: featureset.h mypredict.c svm.o Makefile
	$(CC) $(CFLAGS) $(INCLUDES) -c mypredict.c 
	$(CPP) $(CFLAGS) mypredict.o svm.o -lm -lfftw3 -o mypredict
	
clean:
	@rm -rf *.o *~ $(EXEC) ffttest mypredict getfeatures core.* core

beauty:
	@echo "Beautyfing files..."
	@indent -kr -i8 -ts8 -sob -l80 -ss -ncs *.[c,h]
	@rm -f *.c~
	@rm -f *.h~

sense:
	@echo "Reading the $(README) file is the best way to get some sense..."
	@echo 
	@more $(README)

love:
	@echo "Get a job!"
