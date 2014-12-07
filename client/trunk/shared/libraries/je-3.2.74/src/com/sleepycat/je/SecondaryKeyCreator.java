/*-
 * See the file LICENSE for redistribution information.
 *
 * Copyright (c) 2002,2008 Oracle.  All rights reserved.
 *
 * $Id: SecondaryKeyCreator.java,v 1.10.2.2 2008/01/07 15:14:08 cwl Exp $
 */

package com.sleepycat.je;

/**
 * Javadoc for this public method is generated via
 * the doc templates in the doc_src directory.
 */
public interface SecondaryKeyCreator {

    /**
     * Javadoc for this public method is generated via
     * the doc templates in the doc_src directory.
     */
    public boolean createSecondaryKey(SecondaryDatabase secondary,
				      DatabaseEntry key,
				      DatabaseEntry data,
				      DatabaseEntry result)
	throws DatabaseException;
}
