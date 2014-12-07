/*-
 * See the file LICENSE for redistribution information.
 *
 * Copyright (c) 2002,2007 Oracle.  All rights reserved.
 *
 * $Id: Loggable.java,v 1.3.2.1 2007/02/01 14:49:47 cwl Exp $
 */

package com.sleepycat.je.log;

import java.nio.ByteBuffer;

/**
 * A class that implements Loggable knows how to read and write itself into 
 * a ByteBuffer in a format suitable for the JE log or JE replication messages.
 */
public interface Loggable {

    /* 
     * Writing to a byte buffer
     */

    /**
     * @return number of bytes used to store this object.
     */
    public int getLogSize();

    /**
     * Serialize this object into the buffer. 
     * @param logBuffer is the destination buffer
     */
    public void writeToLog(ByteBuffer logBuffer);

    /*  
     *  Reading from a byte buffer
     */

    /**
     * Initialize this object from the data in itemBuf.
     * @param itemBuf the source buffer
     */
    public void readFromLog(ByteBuffer itemBuffer, byte entryTypeVersion)
	throws LogException;

    /**
     * Write the object into the string buffer for log dumping. Each object
     * should be dumped without indentation or new lines and should be valid
     * XML.
     * @param sb destination string buffer
     * @param verbose if true, dump the full, verbose version
     */
    public void dumpLog(StringBuffer sb, boolean verbose);

    /**
     * @return the transaction id embedded within this loggable object. Objects
     * that have no transaction id should return 0.
     */
    public long getTransactionId();
}
