CC= gcc
CPP= g++
CFLAGS= -Wall -W -Winline -g -ggdb -D_POSIX_PTHREAD_SEMANTICS
#CFLAGS= -Wall -W -D_POSIX_PTHREAD_SEMANTICS -O3 -msse3 -mfpmath=sse -momit-leaf-frame-pointer -ftracer -fomit-frame-pointer -funroll-loops -fprefetch-loop-arrays
INCLUDES= -I. -I/sw/include/ -I/usr/include/
LIBINCLUDES= -L/sw/lib/
LIBS= -lpcap -lpthread -lfftw3
OBJS= svm.o packet_capture.o flow_table.o flow_processor.o featureset.o connections.o eunomia.o
EXEC= eunomia
README= Readme

all:	$(OBJS) Makefile
	$(CPP) $(LIBINCLUDES) $(OBJS) -o $(EXEC) $(LIBS)
	@echo "Stripping $(EXEC)..."
	@strip $(EXEC)
	
eunomia.o:	eunomia.h eunomia.c Makefile
	$(CC) $(CFLAGS) $(INCLUDES) -c eunomia.c

flow_table.o:	flow_table.h flow_table.c Makefile
	$(CC) $(CFLAGS) $(INCLUDES) -c flow_table.c

flow_processor.o:	flow_processor.h flow_processor.c Makefile
	$(CC) $(CFLAGS) $(INCLUDES) -c flow_processor.c

packet_capture.o:	packet_capture.h packet_capture.c Makefile
	$(CC) $(CFLAGS) $(INCLUDES) -c packet_capture.c

featureset.o:	featureset.h featureset.c Makefile
	$(CC) $(CFLAGS) $(INCLUDES) -c featureset.c

connections.o:	connections.h connections.c Makefile
	$(CC) $(CFLAGS) $(INCLUDES) -c connections.c

svm.o:	svm.h svm.cpp Makefile
	$(CPP) $(CFLAGS) $(INCLUDES) -c svm.cpp

clean:
	@rm -rf *.o *~ $(EXEC) 

beauty:
	@echo "Beautyfing files..."
	@indent -kr -i8 -ts8 -sob -l80 -ss -ncs *.[c,h]
	@rm -f *.c~
	@rm -f *.h~

sense:
	@echo "Reading the $(README) file is the best way to get some sense..."
	@echo 
	@more $(README)
