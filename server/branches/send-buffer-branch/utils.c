#include <stdlib.h>
#include <stdio.h>
#include <sys/types.h>
#include <unistd.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <pwd.h>

#include "utils.h"

int u_sleep(unsigned int sec, unsigned int usec)
{
	struct timeval t;

	t.tv_sec = sec;
	t.tv_usec = usec;
	if (select(0, (fd_set *) 0, (fd_set *) 0, (fd_set *) 0, &t) < 0)
		return -1;
	return 0;
}

void make_daemon(const char *log_file)
{
	pid_t did;

	fprintf(stdout, "Initializing daemon...");

	if (getppid() != 1) {
		did = fork();

		if (did > 0)
			exit(0);

		if (did < 0)
			fprintf(stderr, "could not fork() to make daemon");

		setsid();
	}

	/* redirect std/in/out/err to logfile */
	close(0);
	close(1);
	close(2);

	(log_file) ? open(log_file, O_RDWR) : open("/dev/null", O_RDWR);

	dup(0);
	dup(0);

	return;
}

int set_proper_privs(const char *user)
{
	struct passwd *pw = getpwnam(user);

	if (pw == NULL) {
		fprintf(stderr,
			"set_proper_privs: could not find proper user \"%s\"!\n",
			user);
		exit(-1);
	}

	if (setregid(pw->pw_gid, pw->pw_gid) == -1 ||
	    setreuid(pw->pw_uid, pw->pw_uid) == -1) {
		fprintf(stderr, "attempt to drop privileges to \"%s\" failed",
			user);
		return -1;
	}

	fprintf(stdout,
		"set_proper_privs: privileges lowered to that of user %s",
		user);
	return 0;
}
