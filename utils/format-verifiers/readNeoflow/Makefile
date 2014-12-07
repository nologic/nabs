CXX?= g++
CFLAGS?= -O3
CFLAGS+= -W -Wall

readNeoflow: readNeoflow.cpp Makefile
	$(CXX) $(CFLAGS) -o readNeoflow readNeoflow.cpp

clean:
	rm -f readNeoflow *.core
