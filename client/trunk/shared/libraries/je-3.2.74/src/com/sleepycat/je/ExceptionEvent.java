/*-
 * See the file LICENSE for redistribution information.
 *
 * Copyright (c) 2002,2008 Oracle.  All rights reserved.
 *
 * $Id: ExceptionEvent.java,v 1.4.2.2 2008/01/07 15:14:08 cwl Exp $
 */

package com.sleepycat.je;

public class ExceptionEvent {
    private Exception exception;
    private String threadName;

    public ExceptionEvent(Exception exception, String threadName) {
	this.exception = exception;
	this.threadName = threadName;
    }

    public ExceptionEvent(Exception exception) {
	this.exception = exception;
	this.threadName = Thread.currentThread().toString();
    }

    public Exception getException() {
	return exception;
    }

    public String getThreadName() {
	return threadName;
    }
}

