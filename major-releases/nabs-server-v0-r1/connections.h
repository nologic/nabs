#ifndef CONNECTIONS_H
#define CONNECTIONS_H

#include <stdio.h>
#include <sys/types.h>
#include <sys/socket.h>


struct connection{
	int socketfd;
	FILE *socketfp;
	struct sockaddr_in sin;
	int port;
};

struct connection_pair{
	struct connection local;
	struct connection *remote;
	int n_remote;
};

int init_connection_pair(struct connection_pair *, int);
int listen_for_connections(struct connection *, int, int);
int accept_connections(struct connection_pair *);

#endif
