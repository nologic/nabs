/*-
 * See the file LICENSE for redistribution information.
 *
 * Copyright (c) 2002,2007 Oracle.  All rights reserved.
 *
 * $Id: JEConnectionFactoryImpl.java,v 1.8.2.1 2007/02/01 14:49:45 cwl Exp $
 */

package com.sleepycat.je.jca.ra;

import java.io.File;

import javax.naming.NamingException;
import javax.naming.Reference;
import javax.resource.ResourceException;
import javax.resource.spi.ConnectionManager;
import javax.resource.spi.ManagedConnectionFactory;

import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.je.TransactionConfig;

public class JEConnectionFactoryImpl implements JEConnectionFactory {

    /* 
     * These are not transient because SJSAS seems to need to serialize
     * them when leaving them in JNDI.
     */
    private /* transient */ ConnectionManager manager;
    private /* transient */ ManagedConnectionFactory factory;
    private Reference reference;

    JEConnectionFactoryImpl(ConnectionManager manager,
			    ManagedConnectionFactory factory) {
	this.manager = manager;
	this.factory = factory;
    }

    public JEConnection getConnection(String jeRootDir,
				      EnvironmentConfig envConfig)
	throws JEException {

	return getConnection(jeRootDir, envConfig, null);
    }


    public JEConnection getConnection(String jeRootDir,
				      EnvironmentConfig envConfig,
				      TransactionConfig transConfig)
	throws JEException {

	JEConnection dc = null;
 	JERequestInfo jeInfo =
 	    new JERequestInfo(new File(jeRootDir), envConfig, transConfig);
	try {
	    dc = (JEConnection) manager.allocateConnection(factory, jeInfo);
	} catch (ResourceException e) {
	    throw new JEException("Unable to get Connection: " + e);
	}

	return dc;
    }

    public void setReference(Reference reference) {
	this.reference = reference;
    }

    public Reference getReference()
	throws NamingException {

	return reference;
    }
}
