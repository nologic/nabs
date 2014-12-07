/*-
 * See the file LICENSE for redistribution information.
 *
 * Copyright (c) 2002,2007 Oracle.  All rights reserved.
 *
 * $Id: CheckpointStart.java,v 1.28.2.1 2007/02/01 14:49:48 cwl Exp $
 */

package com.sleepycat.je.recovery;

import java.nio.ByteBuffer;
import java.sql.Timestamp;
import java.util.Calendar;

import com.sleepycat.je.log.LogException;
import com.sleepycat.je.log.LogUtils;
import com.sleepycat.je.log.Loggable;

/**
 * CheckpointStart creates a log entry that marks the beginning of a
 * checkpoint.
 */
public class CheckpointStart implements Loggable {

    private Timestamp startTime;
    private long id;

    /* 
     * invoker is just a way to tag each checkpoint in the log for easier log
     * based debugging. It will tell us whether the checkpoint was invoked by
     * recovery, the daemon, the api, or the cleaner.
     */
    private String invoker; 

    public CheckpointStart(long id, String invoker) {
        Calendar cal = Calendar.getInstance();
        this.startTime = new Timestamp(cal.getTime().getTime());
        this.id = id;
        if (invoker == null) {
            this.invoker = "";
        } else {
            this.invoker = invoker;
        }
    }

    /* For logging only. */
    public CheckpointStart() {
    }

    /*
     * Logging support for writing.
     */

    /**
     * @see Loggable#getLogSize
     */
    public int getLogSize() {
        return LogUtils.getTimestampLogSize() +
            LogUtils.getLongLogSize() + 
            LogUtils.getStringLogSize(invoker);
    }

    /**
     * @see Loggable#writeToLog
     */
    public void writeToLog(ByteBuffer logBuffer) {
        LogUtils.writeTimestamp(logBuffer, startTime);
        LogUtils.writeLong(logBuffer, id);
        LogUtils.writeString(logBuffer, invoker);
    }

    /**
     * @see Loggable#readFromLog
     */
    public void readFromLog(ByteBuffer logBuffer, byte entryTypeVersion)
	throws LogException {

        startTime = LogUtils.readTimestamp(logBuffer);
        id = LogUtils.readLong(logBuffer);
        invoker = LogUtils.readString(logBuffer);
    }

    /**
     * @see Loggable#dumpLog
     */
    public void dumpLog(StringBuffer sb, boolean verbose) {
        sb.append("<CkptStart invoker=\"").append(invoker);
        sb.append("\" time=\"").append(startTime);
        sb.append("\" id=\"").append(id);
        sb.append("\"/>");
    }

    /**
     * @see Loggable#getTransactionId
     */
    public long getTransactionId() {
	return 0;
    }
}
