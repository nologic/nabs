/*-
 * See the file LICENSE for redistribution information.
 *
 * Copyright (c) 2002,2007 Oracle.  All rights reserved.
 *
 * $Id: DumpFileReader.java,v 1.43.2.1 2007/02/01 14:49:47 cwl Exp $
 */

package com.sleepycat.je.log;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.dbi.EnvironmentImpl;
import com.sleepycat.je.utilint.DbLsn;

/**
 * The DumpFileReader prints every log entry to stdout.
 */
public abstract class DumpFileReader extends FileReader {

    /* A set of the entry type numbers that this DumpFileReader should dump. */
    private Set targetEntryTypes;

    /* A set of the txn ids that this DumpFileReader should dump. */
    protected Set targetTxnIds;

    /* If true, dump the long version of the entry. */
    protected boolean verbose;

    /**
     * Create this reader to start at a given LSN.
     */
    public DumpFileReader(EnvironmentImpl env,
			  int readBufferSize, 
			  long startLsn,
			  long finishLsn,
			  String entryTypes,
			  String txnIds,
			  boolean verbose)
	throws IOException, DatabaseException {

        super(env,
              readBufferSize,
              true, // read forward
              startLsn,
              null, // single file number
              DbLsn.NULL_LSN, // end of file lsn
              finishLsn); // finish lsn

        /* If entry types is not null, record the set of target entry types. */
        targetEntryTypes = new HashSet();
        if (entryTypes != null) {
            StringTokenizer tokenizer = new StringTokenizer(entryTypes, ",");
            while (tokenizer.hasMoreTokens()) {
                String typeString = (String) tokenizer.nextToken();
                targetEntryTypes.add(new Byte(typeString.trim()));
            }
        }
        /* If txn ids is not null, record the set of target txn ids. */
        targetTxnIds = new HashSet();
        if (txnIds != null) {
            StringTokenizer tokenizer = new StringTokenizer(txnIds, ",");
            while (tokenizer.hasMoreTokens()) {
                String txnIdString = (String) tokenizer.nextToken();
                targetTxnIds.add(new Long(txnIdString.trim()));
            }
        }
        this.verbose = verbose;
    }

    /** 
     * @return true if this reader should process this entry, or just
     * skip over it.
     */
    protected boolean isTargetEntry(byte logEntryTypeNumber,
                                    byte logEntryTypeVersion) {
	if (targetEntryTypes.size() == 0) {
	    /* We want to dump all entry types. */
	    return true;
	} else {
	    return targetEntryTypes.contains(new Byte(logEntryTypeNumber));
	}
    }

    public void summarize() {
    }
}
