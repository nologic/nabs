#ifndef UTILS_H
#define UTILS_H

int u_sleep(unsigned int, unsigned int);
void make_daemon(const char *);
int set_proper_privs(const char *user);

#endif
