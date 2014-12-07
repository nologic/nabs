/*-
 * See the file LICENSE for redistribution information.
 *
 * Copyright (c) 2002,2008 Oracle.  All rights reserved.
 *
 * $Id: SecondaryMultiKeyCreator.java,v 1.4.2.2 2008/01/07 15:14:08 cwl Exp $
 */

package com.sleepycat.je;

import java.util.Set;

/**
 * Javadoc for this public method is generated via
 * the doc templates in the doc_src directory.
 */
public interface SecondaryMultiKeyCreator {

    /**
     * Javadoc for this public method is generated via
     * the doc templates in the doc_src directory.
     */
    public void createSecondaryKeys(SecondaryDatabase secondary,
				    DatabaseEntry key,
				    DatabaseEntry data,
				    Set results)
	throws DatabaseException;
}
