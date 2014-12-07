/*
 * See the file LICENSE for redistribution information.
 *
 * Copyright (c) 2002,2008 Oracle.  All rights reserved.
 *
 * $Id: LockNotGrantedException.java,v 1.6.2.3 2008/01/07 15:14:08 cwl Exp $
 */

package com.sleepycat.je;

/**
 * Javadoc for this public class is generated
 * via the doc templates in the doc_src directory.
 */
public class LockNotGrantedException extends DeadlockException {

    /*
     * LockNotGrantedException extends DeadlockException in order to
     * support the approach that all application need only handle
     * DeadlockException. The idea is that we don't want an
     * application to fail because a new type of exception is thrown
     * when an operation is changed to non-blocking.
     *
     * Applications that care about LockNotGrantedExceptions can
     * add another catch block to handle it, but otherwise they
     * can be handled the same way as deadlocks.
     * See SR [#10672]
     */

    public LockNotGrantedException() {
	super();
    }

    public LockNotGrantedException(Throwable t) {
        super(t);
    }

    public LockNotGrantedException(String message) {
	super(message);
    }

    public LockNotGrantedException(String message, Throwable t) {
        super(message, t);
    }
}
