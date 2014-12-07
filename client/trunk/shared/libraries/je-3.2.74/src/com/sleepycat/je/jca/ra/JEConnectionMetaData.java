/*-
 * See the file LICENSE for redistribution information.
 *
 * Copyright (c) 2002,2008 Oracle.  All rights reserved.
 *
 * $Id: JEConnectionMetaData.java,v 1.5.2.2 2008/01/07 15:14:11 cwl Exp $
 */

package com.sleepycat.je.jca.ra;

import javax.resource.ResourceException;
import javax.resource.spi.ManagedConnectionMetaData;

public class JEConnectionMetaData
    implements ManagedConnectionMetaData {

    public JEConnectionMetaData() {
    }

    public String getEISProductName()
        throws ResourceException {

        return "Berkeley DB Java Edition JCA";
    }

    public String getEISProductVersion()
        throws ResourceException {

        return "2.0";
    }

    public int getMaxConnections()
        throws ResourceException {

	/* Make a je.* parameter? */
	return 100;
    }

    public String getUserName()
        throws ResourceException {

    	return null;
    }
}
