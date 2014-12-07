/*-
 * See the file LICENSE for redistribution information.
 *
 * Copyright (c) 2002,2008 Oracle.  All rights reserved.
 *
 * $Id: ForeignKeyDeleteAction.java,v 1.6.2.2 2008/01/07 15:14:08 cwl Exp $
 */

package com.sleepycat.je;

/**
 * Javadoc for this public class is generated via
 * the doc templates in the doc_src directory.
 */
public class ForeignKeyDeleteAction {

    private String name;

    private ForeignKeyDeleteAction(String name) {
	this.name = name;
    }

    /**
     * Javadoc for this public class is generated via
     * the doc templates in the doc_src directory.
     */
    public final static ForeignKeyDeleteAction ABORT =
                    new ForeignKeyDeleteAction("ABORT");

    /**
     * Javadoc for this public class is generated via
     * the doc templates in the doc_src directory.
     */
    public final static ForeignKeyDeleteAction CASCADE =
                    new ForeignKeyDeleteAction("CASCADE");

    /**
     * Javadoc for this public class is generated via
     * the doc templates in the doc_src directory.
     */
    public final static ForeignKeyDeleteAction NULLIFY =
                    new ForeignKeyDeleteAction("NULLIFY");

    public String toString() {
	return "ForeignKeyDeleteAction." + name;
    }
}
