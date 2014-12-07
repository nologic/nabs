/*-
 * See the file LICENSE for redistribution information.
 *
 * Copyright (c) 2002,2008 Oracle.  All rights reserved.
 *
 * $Id: NoRootException.java,v 1.1.2.2 2008/01/07 15:14:14 cwl Exp $
 */

package com.sleepycat.je.recovery;

import com.sleepycat.je.dbi.EnvironmentImpl;

/**
 * Recovery related exceptions
 */
public class NoRootException extends RecoveryException {

    public NoRootException(EnvironmentImpl env,
                           String message) {
	super(env, message);
    }
}
