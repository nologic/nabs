/*-
 * See the file LICENSE for redistribution information.
 *
 * Copyright (c) 2002,2008 Oracle.  All rights reserved.
 *
 * $Id: JEConnectionImpl.java,v 1.1.2.3 2008/01/07 15:14:11 cwl Exp $
 */

package com.sleepycat.je.jca.ra;

import javax.resource.ResourceException;

import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.SecondaryConfig;
import com.sleepycat.je.SecondaryDatabase;
import com.sleepycat.je.Transaction;

/**
 * A JEConnection provides access to JE services. See
 * &lt;JEHOME&gt;/examples/jca/HOWTO-**.txt and
 * &lt;JEHOME&gt;/examples/jca/simple/SimpleBean.java for more information on
 * how to build the resource adaptor and use a JEConnection.
 */
public class JEConnectionImpl implements JEConnection {

    private JEManagedConnection mc;
    private JELocalTransaction txn;

    public JEConnectionImpl(JEManagedConnection mc) {
        this.mc = mc;
    }

    public void setManagedConnection(JEManagedConnection mc,
				     JELocalTransaction lt) {
	this.mc = mc;
	if (txn == null) {
	    txn = lt;
	}
    }

    public JELocalTransaction getLocalTransaction() {
	return txn;
    }

    public void setLocalTransaction(JELocalTransaction txn) {
	this.txn = txn;
    }

    public Environment getEnvironment()
	throws ResourceException {

	return mc.getEnvironment();
    }

    public Database openDatabase(String name, DatabaseConfig config)
	throws DatabaseException {

	return mc.openDatabase(name, config);
    }

    public SecondaryDatabase openSecondaryDatabase(String name,
						   Database primaryDatabase,
						   SecondaryConfig config)
	throws DatabaseException {

	return mc.openSecondaryDatabase(name, primaryDatabase, config);
    }

    public void removeDatabase(String databaseName)
	throws DatabaseException {

	mc.removeDatabase(databaseName);
    }

    public long truncateDatabase(String databaseName, boolean returnCount)
	throws DatabaseException {

	return mc.truncateDatabase(databaseName, returnCount);
    }

    public Transaction getTransaction()
	throws ResourceException {

	if (txn == null) {
	    return null;
	}

	try {
	    return txn.getTransaction();
	} catch (DatabaseException DE) {
	    ResourceException ret = new ResourceException(DE.toString());
	    ret.initCause(DE);
	    throw ret;
	}
    }

    public void close()
	throws JEException {

	mc.close();
    }
}
