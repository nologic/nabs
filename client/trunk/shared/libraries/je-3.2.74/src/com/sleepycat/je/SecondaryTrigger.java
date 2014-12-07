/*-
 * See the file LICENSE for redistribution information.
 *
 * Copyright (c) 2002,2008 Oracle.  All rights reserved.
 *
 * $Id: SecondaryTrigger.java,v 1.12.2.2 2008/01/07 15:14:08 cwl Exp $
 */

package com.sleepycat.je;

import com.sleepycat.je.txn.Locker;

class SecondaryTrigger implements DatabaseTrigger {

    private SecondaryDatabase secDb;

    SecondaryTrigger(SecondaryDatabase secDb) {

        this.secDb = secDb;
    }

    final SecondaryDatabase getDb() {

        return secDb;
    }

    public void triggerAdded(Database db) {
    }

    public void triggerRemoved(Database db) {

        secDb.clearPrimary();
    }

    public void databaseUpdated(Database db,
                                Locker locker,
                                DatabaseEntry priKey,
                                DatabaseEntry oldData,
                                DatabaseEntry newData)
        throws DatabaseException {

        secDb.updateSecondary(locker, null, priKey, oldData, newData);
    }
}
