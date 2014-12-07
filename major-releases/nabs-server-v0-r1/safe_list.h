#ifndef __SAFE_LIST_H
#define __SAFE_LIST_H

#include <pthread.h>
#include <error.h>
#include "list.h"

struct list_head_safe {
	struct list_head list;
	pthread_mutex_t mutex;
};

#define LIST_HEAD_SAFE_INIT(name)	{LIST_HEAD_INIT(name.list), PTHREAD_MUTEX_INITIALIZER}

#define LIST_HEAD_SAFE(name)	\
	struct list_head_safe name= LIST_HEAD_SAFE_INIT(name)

#define INIT_LIST_HEAD_SAFE(name)	\
	INIT_LIST_HEAD(&((name)->list));	\
	pthread_mutex_init(&((name)->mutex), NULL);

static inline int safe_list_lock(struct list_head_safe *l)
{
	return (pthread_mutex_lock(&(l->mutex)));
}

static inline int safe_list_unlock(struct list_head_safe *l)
{
	return (pthread_mutex_unlock(&(l->mutex)));
}

static inline int safe_list_trylock(struct list_head_safe *l)
{

	if (pthread_mutex_trylock(&(l->mutex)) != 0)
		return 1;
	return 0;
}

#endif
