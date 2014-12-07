/*-
 * See the file LICENSE for redistribution information.
 *
 * Copyright (c) 2000,2008 Oracle.  All rights reserved.
 *
 * $Id: KeyRangeException.java,v 1.3.2.2 2008/01/07 15:14:21 cwl Exp $
 */

package com.sleepycat.util.keyrange;

/**
 * An exception thrown when a key is out of range.
 *
 * @author Mark Hayes
 */
public class KeyRangeException extends IllegalArgumentException {

    /**
     * Creates a key range exception.
     */
    public KeyRangeException(String msg) {

        super(msg);
    }
}
