/*
 * See the file LICENSE for redistribution information.
 *
 * Copyright (c) 2002,2008 Oracle.  All rights reserved.
 *
 * $Id: DeadlockException.java,v 1.13.2.3 2008/01/07 15:14:08 cwl Exp $
 */

package com.sleepycat.je;

/**
 * Javadoc for this public class is generated
 * via the doc templates in the doc_src directory.
 */
public class DeadlockException extends DatabaseException {

    private long[] ownerTxnIds;
    private long[] waiterTxnIds;

    public DeadlockException() {
	super();
    }

    public DeadlockException(Throwable t) {
        super(t);
    }

    public DeadlockException(String message) {
	super(message);
    }

    public DeadlockException(String message, Throwable t) {
        super(message, t);
    }

    public void setOwnerTxnIds(long[] ownerTxnIds) {
	this.ownerTxnIds = ownerTxnIds;
    }

    public long[] getOwnerTxnIds() {
	return ownerTxnIds;
    }

    public void setWaiterTxnIds(long[] waiterTxnIds) {
	this.waiterTxnIds = waiterTxnIds;
    }

    public long[] getWaiterTxnIds() {
	return waiterTxnIds;
    }
}
