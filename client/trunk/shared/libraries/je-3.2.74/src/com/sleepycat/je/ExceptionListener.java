/*-
 * See the file LICENSE for redistribution information.
 *
 * Copyright (c) 2002,2008 Oracle.  All rights reserved.
 *
 * $Id: ExceptionListener.java,v 1.3.2.2 2008/01/07 15:14:08 cwl Exp $
 */

package com.sleepycat.je;

public interface ExceptionListener {
    void exceptionThrown(ExceptionEvent event);
}

