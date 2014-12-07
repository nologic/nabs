/*-
 * See the file LICENSE for redistribution information.
 *
 * Copyright (c) 2002,2008 Oracle.  All rights reserved.
 *
 * $Id: NotImplementedYetException.java,v 1.16.2.2 2008/01/07 15:14:18 cwl Exp $
 */

package com.sleepycat.je.utilint;

/**
 * Something is not yet implemented.
 */
public class NotImplementedYetException extends RuntimeException {

    public NotImplementedYetException() {
	super();
    }

    public NotImplementedYetException(String message) {
	super(message);
    }
}
