#ifndef INCLUDED_FLOW_CONTENT_PTR_LIST_H
#define INCLUDED_FLOW_CONTENT_PTR_LIST_H

#include "list.h"
#include "flow_table.h"

struct flow_content_ptr_list{
	struct list_head list;
	struct list_head freelist;
};

struct flow_content_ptr{
	struct list_head list;
	struct flow_content *ptr;
};


static inline void init_flow_content_ptr_list(struct flow_content_ptr_list *fcpl){
	memset(fcpl, 0, sizeof(struct flow_content_ptr_list));
	INIT_LIST_HEAD(&(fcpl->list));
	INIT_LIST_HEAD(&(fcpl->freelist));
}

static inline void add_flow_content_ptr_list(struct flow_content_ptr_list *fcpl, struct flow_content *fc){
	struct flow_content_ptr *tmp;
	struct list_head *pos;
	
	if(list_empty(&(fcpl->freelist))){
		tmp= (struct flow_content_ptr *)malloc(sizeof(struct flow_content_ptr));
		INIT_LIST_HEAD(&(tmp->list));
		tmp->ptr= fc;
		list_add(&(tmp->list), &(fcpl->list));
	}else{
		pos= fcpl->freelist.next;
		list_del_init(pos);
		tmp= list_entry(pos, struct flow_content_ptr, list);
		tmp->ptr= fc;
		list_add(&(tmp->list), &(fcpl->list));
	}
}

static inline void delete_flow_content_ptr_list(struct flow_content_ptr_list *fcpl, struct flow_content_ptr *fcp){
	list_del_init(&(fcp->list));
	list_add(&(fcp->list), &(fcpl->freelist));
}

static inline void finit_flow_content_ptr_list(struct flow_content_ptr_list *fcpl){
	struct list_head *p, *q;	
	struct flow_content_ptr *tmp;

	list_for_each_safe(p, q, &(fcpl->list)){
		tmp= list_entry(p, struct flow_content_ptr, list);
		list_del(p);
		free(tmp);
	}
	
	list_for_each_safe(p, q, &(fcpl->freelist)){
		tmp= list_entry(p, struct flow_content_ptr, list);
		list_del(p);
		free(tmp);
	}

	memset(fcpl, 0, sizeof(struct flow_content_ptr_list));
}

#endif
