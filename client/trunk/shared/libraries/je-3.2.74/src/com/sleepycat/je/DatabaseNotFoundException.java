/*
 * See the file LICENSE for redistribution information.
 *
 * Copyright (c) 2002,2008 Oracle.  All rights reserved.
 *
 * $Id: DatabaseNotFoundException.java,v 1.6.2.2 2008/01/07 15:14:08 cwl Exp $
 */

package com.sleepycat.je;

/**
 * Javadoc for this public class is generated
 * via the doc templates in the doc_src directory.
 */
public class DatabaseNotFoundException extends DatabaseException {

    public DatabaseNotFoundException() {
	super();
    }

    public DatabaseNotFoundException(Throwable t) {
        super(t);
    }

    public DatabaseNotFoundException(String message) {
	super(message);
    }

    public DatabaseNotFoundException(String message, Throwable t) {
        super(message, t);
    }
}
