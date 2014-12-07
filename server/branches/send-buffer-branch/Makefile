CC= gcc
CPP= g++
#CFLAGS= -Wall -ggdb
CFLAGS= -Wall -W -g -ggdb -Wpadded #-O3 -mmmx -msse2 -mfpmath=sse -momit-leaf-frame-pointer -ftracer -ffast-math -fomit-frame-pointer -funroll-loops -fprefetch-loop-arrays # -mcpu=nocona -fprofile-arcs #-fbranch-probabilities #-DUSE_MD5 #-DEU_STATS 
#CFLAGS= -Wall -ggdb -fprofile-arcs -ftest-coverage -D_FILE_OFFSET_BITS=64 -DEU_THROTTLE
INCLUDES= -I. -I/usr/include/ -I/sw/include/
LIBINCLUDES= -L/sw/lib/
LIBS= -lpcap -lpthread -lm -lfftw3 #-lssl
OBJS= utils.o send_buffer.o flowtable.o flow_processor.o packet_capture.o connections.o featureset.o eunomia.o svm.o #lossy.o
EXEC= eunomia
README= Readme

all:	$(OBJS) Makefile
	$(CPP) $(LIBINCLUDES) $(OBJS) -o $(EXEC) $(LIBS)
#	@echo "Striping $(EXEC)..."
#	@strip $(EXEC)
	
eunomia.o:	flowtable.h eunomia.h eunomia.c Makefile
	$(CC) $(CFLAGS) $(INCLUDES) -c eunomia.c

lossy.o:	lossy.h lossy.c Makefile
	$(CC) $(CFLAGS) $(INCLUDES) -c lossy.c

flowtable.o:	flowtable.h flowtable.c Makefile
	$(CC) $(CFLAGS) $(INCLUDES) -c flowtable.c

flow_processor.o:	flow_processor.h flow_processor.c Makefile
	$(CC) $(CFLAGS) $(INCLUDES) -c flow_processor.c

connections.o:	connections.h connections.c Makefile
	$(CC) $(CFLAGS) $(INCLUDES) -c connections.c

send_buffer.o:	send_buffer.h send_buffer.c Makefile
	$(CC) $(CFLAGS) $(INCLUDES) -c send_buffer.c

utils.o:	utils.h utils.c Makefile
	$(CC) $(CFLAGS) $(INCLUDES) -c utils.c

packet_capture.o:	packet_capture.h packet_capture.c Makefile
	$(CC) $(CFLAGS) $(INCLUDES) -c packet_capture.c

featureset.o:	featureset.h featureset.c Makefile
	$(CC) $(CFLAGS) $(INCLUDES) -c featureset.c

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
