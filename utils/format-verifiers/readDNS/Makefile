CXX?= g++
#CFLAGS?= -O3
CFLAGS= -g
CFLAGS+= -W -Wall

readDNS: readDNS.cpp Makefile
	$(CXX) $(CFLAGS) -o readDNS readDNS.cpp

clean:
	rm -f readDNS *.core
