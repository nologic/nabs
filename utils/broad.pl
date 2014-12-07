#!/usr/bin/perl -w

use strict;
use Socket;
use POSIX ":sys_wait_h";

$SIG{CHLD} = \&REAPER;

my @files = split(/\n/, `cat broad.spc`);
my $waitedpid;

foreach my $file (@files) {
	listener(split(/ /, $file));
}

sub listener {
	my $port = shift;
	my $dest = shift;

	if(fork()) {
        	return;
        }

	print "Starting on $port\n";
	my $socket = start_server($port);
	my $client;
	my $paddr;

     	for ($waitedpid = 0; ($paddr = accept($client, $socket)) || $waitedpid; $waitedpid = 0) {
 		next if $waitedpid and not $paddr;

		do_fd($client, open_dest($dest));
	}

	exit(0);
}

sub open_dest {
	my $dest = shift;
	my $fd;

	if($dest =~ m/:/) {
		(my $ip, my $port) = split(/:/, $dest);

		socket($fd, PF_INET, SOCK_STREAM, getprotobyname('tcp')) or warn "socket: $!";
		connect($fd, sockaddr_in($port, inet_aton($ip))) or warn "connect: $!";

		return $fd;
	} else {
		open($fd, "<$dest");

		return $fd;
	}
}

sub start_server {
	my $port = shift;
	my $socket;

	socket($socket, PF_INET, SOCK_STREAM, getprotobyname('tcp')) or warn "socket: $!";
	setsockopt($socket, SOL_SOCKET, SO_REUSEADDR, 1) or die "setsock: $!";
	setsockopt($socket, SOL_SOCKET, SO_KEEPALIVE, 1) or die "setsock: $!";
	bind($socket, sockaddr_in($port, INADDR_ANY)) or die "bind: $!";
	listen($socket, SOMAXCONN) or die "listen: $!";

	return $socket;
}

sub do_fd {
	my $client = shift;
	my $dest = shift;
	my $buff;

	if(fork()) {
		return;
	}

	binmode($client);
	binmode($dest);

	while( sysread($dest, $buff, 1) and syswrite($client, $buff, 1) ) {
	}

	binmode($client);
	binmode($dest);

	shutdown($client, 2);
	shutdown($dest, 2);

	close($client);
	close($dest);

	exit(0);

}

sub REAPER {
    $waitedpid = wait;
    $SIG{CHLD} = \&REAPER;  # loathe sysV
}
