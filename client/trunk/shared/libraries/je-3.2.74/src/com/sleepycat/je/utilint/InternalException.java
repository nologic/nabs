/*-
 * See the file LICENSE for redistribution information.
 *
 * Copyright (c) 2002,2008 Oracle.  All rights reserved.
 *
 * $Id: InternalException.java,v 1.14.2.3 2008/01/07 15:14:18 cwl Exp $
 */

package com.sleepycat.je.utilint;

import com.sleepycat.je.DatabaseException;

/**
 * Some internal inconsistency exception.
 */
public class InternalException extends DatabaseException {

    public InternalException() {
	super();
    }

    public InternalException(String message) {
	super(message);
    }
}
