#ifndef INCLUDED_EUNOMIA_CONFIG_H
#define INCLUDED_EUNOMIA_CONFIG_H

#define _REENTRANT
#include <pthread.h>

#ifndef STATUS_OK
#define STATUS_OK	 0
#endif

#ifndef STATUS_WARN
#define STATUS_WARN	 1
#endif

#ifndef STATUS_ERROR
#define STATUS_ERROR 2
#endif


#define SENSOR_LISTEN_PORT 		1986
#define SENSOR_NETFILTER		"(tcp or udp)"
#define PAYLOAD_BUFFER_LENGTH 	16384	
#define FLOWTABLE_BUCKETS		4096
#define FLOWTABLE_RECORDS		65536
#define FLOW_ACTIVE_TIMEOUT		120
#define FLOW_INACTIVE_TIMEOUT	30

#endif

