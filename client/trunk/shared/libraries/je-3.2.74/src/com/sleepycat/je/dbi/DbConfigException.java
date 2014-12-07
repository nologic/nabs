/*-
 * See the file LICENSE for redistribution information.
 *
 * Copyright (c) 2002,2008 Oracle.  All rights reserved.
 *
 * $Id: DbConfigException.java,v 1.14.2.2 2008/01/07 15:14:09 cwl Exp $
 */

package com.sleepycat.je.dbi;

import com.sleepycat.je.DatabaseException;

/**
 * Configuration related exceptions.
 */
public class DbConfigException extends DatabaseException {

    public DbConfigException(Throwable t) {
        super(t);
    }

    public DbConfigException(String message) {
	super(message);
    }

    public DbConfigException(String message, Throwable t) {
        super(message, t);
    }
}
