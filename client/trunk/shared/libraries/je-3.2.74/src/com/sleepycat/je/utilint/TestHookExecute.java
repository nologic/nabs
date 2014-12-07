/*-
 * See the file LICENSE for redistribution information.
 *
 * Copyright (c) 2002,2008 Oracle.  All rights reserved.
 *
 * $Id: TestHookExecute.java,v 1.4.2.2 2008/01/07 15:14:18 cwl Exp $
 */

package com.sleepycat.je.utilint;

/**
 */
public class TestHookExecute {
    public static boolean doHookIfSet(TestHook testHook) {
        if (testHook != null) {
            testHook.doHook();
        }
        return true;
    }
}
