/*-
 * See the file LICENSE for redistribution information.
 *
 * Copyright (c) 2002,2008 Oracle.  All rights reserved.
 *
 * $Id: PutMode.java,v 1.6.2.2 2008/01/07 15:14:09 cwl Exp $
 */

package com.sleepycat.je.dbi;

/**
 * Internal class used to distinguish which variety of putXXX() that
 * Cursor.putInternal() should use.
 */
public class PutMode {
    public static final PutMode NODUP =       new PutMode();
    public static final PutMode CURRENT =     new PutMode();
    public static final PutMode OVERWRITE =   new PutMode();
    public static final PutMode NOOVERWRITE = new PutMode();
}
