/*-
 * See the file LICENSE for redistribution information.
 *
 * Copyright (c) 2002,2007 Oracle.  All rights reserved.
 *
 * $Id: GetMode.java,v 1.7.2.2 2007/08/06 16:00:06 cwl Exp $
 */

package com.sleepycat.je.dbi;

/**
 * Internal class used to distinguish which variety of getXXX() that
 * Cursor.retrieveNext should use.
 */
public class GetMode {
    private String name;
    private boolean forward;

    private GetMode(String name, boolean forward) {
        this.name = name;
        this.forward = forward;
    }

    public static final GetMode NEXT =       new GetMode("NEXT", true);
    public static final GetMode PREV =       new GetMode("PREV", false);
    public static final GetMode NEXT_DUP =   new GetMode("NEXT_DUP", true);
    public static final GetMode PREV_DUP =   new GetMode("PREV_DUP", false);
    public static final GetMode NEXT_NODUP = new GetMode("NEXT_NODUP", true);
    public static final GetMode PREV_NODUP = new GetMode("PREV_NODUP", false);

    public final boolean isForward() {
        return forward;
    }

    public String toString() {
        return name;
    }
}
