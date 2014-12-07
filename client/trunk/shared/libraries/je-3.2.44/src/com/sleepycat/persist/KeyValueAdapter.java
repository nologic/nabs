/*-
 * See the file LICENSE for redistribution information.
 *
 * Copyright (c) 2002,2007 Oracle.  All rights reserved.
 *
 * $Id: KeyValueAdapter.java,v 1.5.2.1 2007/02/01 14:49:55 cwl Exp $
 */

package com.sleepycat.persist;

import com.sleepycat.bind.EntryBinding;
import com.sleepycat.je.DatabaseEntry;

/**
 * A ValueAdapter where the "value" is the key (the primary key in a primary
 * index or the secondary key in a secondary index).
 *
 * @author Mark Hayes
 */
class KeyValueAdapter<V> implements ValueAdapter<V> {

    private EntryBinding keyBinding;

    KeyValueAdapter(Class<V> keyClass, EntryBinding keyBinding) {
        this.keyBinding = keyBinding;
    }

    public DatabaseEntry initKey() {
        return new DatabaseEntry();
    }

    public DatabaseEntry initPKey() {
        return null;
    }

    public DatabaseEntry initData() {
        return BasicIndex.NO_RETURN_ENTRY;
    }
    
    public void clearEntries(DatabaseEntry key,
                             DatabaseEntry pkey,
                             DatabaseEntry data) {
        key.setData(null);
    }

    public V entryToValue(DatabaseEntry key,
                          DatabaseEntry pkey,
                          DatabaseEntry data) {
        return (V) keyBinding.entryToObject(key);
    }

    public void valueToData(V value, DatabaseEntry data) {
        throw new UnsupportedOperationException
            ("Cannot change the data in a key-only index");
    }
}
