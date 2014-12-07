#include <string.h>
#include <stdlib.h>
#include <arpa/inet.h>
#include <unistd.h>

#include "connections.h"

inline void init_connection(struct connection *c)
{

	memset(c, 0, sizeof(struct connection));
	c->sin.sin_family = AF_INET;
	c->sin.sin_addr.s_addr = htonl(INADDR_ANY);
}

inline int open_connection_socket(struct connection *c)
{
	c->socketfd = socket(AF_INET, SOCK_STREAM, 0);
	return 0;
}

inline int set_connection_fp(struct connection *c)
{
	c->socketfp = fdopen(c->socketfd, "r+");
	return 0;
}

inline int set_connection_port(struct connection *c, int port)
{
	c->port = port;
	c->sin.sin_port = htons((unsigned short) port);

	return 0;
}

int init_connection_pair(struct connection_pair *c, int x)
{
	int i;
	void *p;

	init_connection(&c->local);

	if((c->remote = (void *) malloc(sizeof(struct connection) * x)) == NULL)
		return -1;

	for(i = 0, p = c->remote; i < x; ++i)
		init_connection((p + (i * sizeof(struct connection))));

	c->n_remote= x;
	return 0;
}

int listen_for_connections(struct connection *server, int port, int tries)
{

	int i;

	if(port > 0)
		set_connection_port(server, port);

	if((server->socketfd = socket(AF_INET, SOCK_STREAM, 0)) == -1){
		fprintf(stderr, "could not create a socket!\n");
		return -1;
	}

	i = 1;
  	if(setsockopt(server->socketfd, SOL_SOCKET, SO_REUSEADDR, &i, sizeof(i)) == -1){
        	fprintf(stderr, "WARNING: Could not set socket for reuse!\n");
		fprintf(stderr, "WARNING: This may affect restarting the sensor when there are active TCP connections to it!\n");
	}

	memset(&server->sin, 0, sizeof(server->sin));
	server->sin.sin_family = AF_INET;
	server->sin.sin_addr.s_addr = htonl(INADDR_ANY);
	server->sin.sin_port = htons((unsigned short) server->port);

	for(i = 0; i < tries; i++) {
		if(!bind
		   (server->socketfd, (struct sockaddr *) &(server->sin),
			sizeof(server->sin)))
			break;
		fprintf(stderr, "Could not bind to port %d. ", server->port);
		if(i < tries)
			fprintf(stderr, "Will try %d more times...\n", tries-i);
		sleep(1);
	}

	if(i == 5) {
		fprintf(stderr, "Could not bind to port %d. Giving up!\n",
				server->port);
		return -1;
	}

	if(listen(server->socketfd, 50) < 0) {
		fprintf(stderr, "Error: listen()\n");
		close(server->socketfd);
		return -1;
	}

	return 0;
}

int accept_connections(struct connection_pair *p)
{
	int i;
	int len;

	len = sizeof(struct sockaddr);

	for(i = 0; i < p->n_remote; i++)
		if(p->remote[i].socketfd == 0)
			break;				//available

	if(i == p->n_remote)
		return 1;


	p->remote[i].socketfd =
		accept(p->local.socketfd,
			   (struct sockaddr *) &(p->remote[i].sin), &len);
	if(p->remote[i].socketfd < 0) {
		memset(&p->remote[i], 0, sizeof(struct connection));
		return -1;
	}

	p->remote[i].socketfp = fdopen(p->remote[i].socketfd, "r+");
	if(!p->remote[i].socketfp) {
		if(p->remote[i].socketfd)
			close(p->remote[i].socketfd);
		memset(&p->remote[i], 0, sizeof(struct connection));
		return -1;
	}

	return 0;
}
