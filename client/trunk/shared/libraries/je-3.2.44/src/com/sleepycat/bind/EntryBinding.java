/*-
 * See the file LICENSE for redistribution information.
 *
 * Copyright (c) 2000,2007 Oracle.  All rights reserved.
 *
 * $Id: EntryBinding.java,v 1.20.2.1 2007/02/01 14:49:38 cwl Exp $
 */

package com.sleepycat.bind;

import com.sleepycat.je.DatabaseEntry;

/**
 * A binding between a key or data entry and a key or data object.
 *
 * @author Mark Hayes
 */
public interface EntryBinding {

    /**
     * Converts a entry buffer into an Object.
     *
     * @param entry is the source entry buffer.
     *
     * @return the resulting Object.
     */
    Object entryToObject(DatabaseEntry entry);

    /**
     * Converts an Object into a entry buffer.
     *
     * @param object is the source Object.
     *
     * @param entry is the destination entry buffer.
     */
    void objectToEntry(Object object, DatabaseEntry entry);
}
