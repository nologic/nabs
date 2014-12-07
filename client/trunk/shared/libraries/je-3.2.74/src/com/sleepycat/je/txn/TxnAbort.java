/*-
 * See the file LICENSE for redistribution information.
 *
 * Copyright (c) 2002,2008 Oracle.  All rights reserved.
 *
 * $Id: TxnAbort.java,v 1.20.2.3 2008/01/07 15:14:17 cwl Exp $
 */

package com.sleepycat.je.txn;


/**
 * This class writes out a transaction commit or transaction end record.
 */
public class TxnAbort extends TxnEnd {
    public TxnAbort(long id, long lastLsn) {
        super(id, lastLsn);
    }

    /**
     * For constructing from the log.
     */
    public TxnAbort() {
    }

    /*
     * Log support
     */

    protected String getTagName() {
        return "TxnAbort";
    }
}
