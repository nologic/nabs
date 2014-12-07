/*-
 * See the file LICENSE for redistribution information.
 *
 * Copyright (c) 2002,2008 Oracle.  All rights reserved.
 *
 * $Id: DuplicateEntryException.java,v 1.13.2.2 2008/01/07 15:14:16 cwl Exp $
 */

package com.sleepycat.je.tree;

import com.sleepycat.je.DatabaseException;

/**
 * Exception to indicate that an entry is already present in a node.
 */
public class DuplicateEntryException extends DatabaseException {
    public DuplicateEntryException() {
	super();
    }

    public DuplicateEntryException(String message) {
	super(message);
    }
}
