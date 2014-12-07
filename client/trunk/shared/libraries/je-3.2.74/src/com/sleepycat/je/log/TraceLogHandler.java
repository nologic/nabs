/*-
 * See the file LICENSE for redistribution information.
 *
 * Copyright (c) 2002,2008 Oracle.  All rights reserved.
 *
 * $Id: TraceLogHandler.java,v 1.31.2.3 2008/01/07 15:14:13 cwl Exp $
 */

package com.sleepycat.je.log;

import java.util.logging.Handler;
import java.util.logging.LogRecord;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.dbi.EnvironmentImpl;
import com.sleepycat.je.utilint.Tracer;

/**
 * Handler for java.util.logging. Takes logging records and publishes them into
 * the database log.
 */
public class TraceLogHandler extends Handler {

    private EnvironmentImpl env;

    public TraceLogHandler(EnvironmentImpl env) {
        this.env = env;
    }

    public void close() {
    }

    public void flush() {
    }

    public void publish(LogRecord l) {
        if (!env.isReadOnly() &&
	    !env.mayNotWrite()) {
            try {
                Tracer trace = new Tracer(l.getMessage());
                trace.log(env.getLogManager());
            } catch (DatabaseException e) {
		throw new IllegalStateException(e);
            }
        }
    }
}
