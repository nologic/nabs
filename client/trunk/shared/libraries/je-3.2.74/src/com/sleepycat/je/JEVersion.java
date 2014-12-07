/*-
 * See the file LICENSE for redistribution information.
 *
 * Copyright (c) 2002,2008 Oracle.  All rights reserved.
 *
 * $Id: JEVersion.java,v 1.93.2.46 2008/02/26 03:08:58 linda Exp $
 */

package com.sleepycat.je;

/**
 * Javadoc for this public class is generated
 * via the doc templates in the doc_src directory.
 */
public class JEVersion {

    /**
     * Javadoc for this public instance is generated via
     * the doc templates in the doc_src directory.
     */
    public static final JEVersion CURRENT_VERSION =
        new JEVersion(3, 2, 74, null);

    private int majorNum;
    private int minorNum;
    private int patchNum;
    private String name;

    private JEVersion(int majorNum, int minorNum, int patchNum, String name) {
        this.majorNum = majorNum;
        this.minorNum = minorNum;
        this.patchNum = patchNum;
        this.name = name;
    }

    /**
     * Javadoc for this public method is generated via
     * the doc templates in the doc_src directory.
     */
    public String toString() {
        return getVersionString();
    }

    /**
     * Javadoc for this public method is generated via
     * the doc templates in the doc_src directory.
     */
    public int getMajor() {
        return majorNum;
    }

    /**
     * Javadoc for this public method is generated via
     * the doc templates in the doc_src directory.
     */
    public int getMinor() {
        return minorNum;
    }

    /**
     * Javadoc for this public method is generated via
     * the doc templates in the doc_src directory.
     */
    public int getPatch() {
        return patchNum;
    }

    /**
     * Javadoc for this public method is generated via
     * the doc templates in the doc_src directory.
     */
    public String getNumericVersionString() {
        StringBuffer version = new StringBuffer();
        version.append(majorNum).append(".");
        version.append(minorNum).append(".");
        version.append(patchNum);
        return version.toString();
    }

    /**
     * Javadoc for this public method is generated via
     * the doc templates in the doc_src directory.
     */
    public String getVersionString() {
        StringBuffer version = new StringBuffer();
        version.append(majorNum).append(".");
        version.append(minorNum).append(".");
        version.append(patchNum);
	if (name != null) {
	    version.append(" (");
	    version.append(name).append(")");
	}
        return version.toString();
    }
}
