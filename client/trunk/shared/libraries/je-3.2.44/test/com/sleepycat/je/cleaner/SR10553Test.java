/*-
 * See the file LICENSE for redistribution information.
 *
 * Copyright (c) 2002,2007 Oracle.  All rights reserved.
 *
 * $Id: SR10553Test.java,v 1.10.2.1 2007/02/01 14:50:06 cwl Exp $
 */

package com.sleepycat.je.cleaner;

import java.io.File;
import java.io.IOException;

import junit.framework.TestCase;

import com.sleepycat.je.CheckpointConfig;
import com.sleepycat.je.Cursor;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.DbInternal;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.je.OperationStatus;
import com.sleepycat.je.config.EnvironmentParams;
import com.sleepycat.je.log.FileManager;
import com.sleepycat.je.util.TestUtils;

public class SR10553Test extends TestCase {

    private static final String DB_NAME = "foo";

    private static final CheckpointConfig forceConfig = new CheckpointConfig();
    static {
        forceConfig.setForce(true);
    }

    private File envHome;
    private Environment env;
    private Database db;

    public SR10553Test() {
        envHome = new File(System.getProperty(TestUtils.DEST_DIR));
    }

    public void setUp()
        throws IOException, DatabaseException {

        TestUtils.removeLogFiles("Setup", envHome, false);
        TestUtils.removeFiles("Setup", envHome, FileManager.DEL_SUFFIX);
    }

    public void tearDown()
        throws IOException, DatabaseException {

        try {
            if (env != null) {
                env.close();
            }
        } catch (Throwable e) {
            System.out.println("tearDown: " + e);
        }
                
        try {
            TestUtils.removeLogFiles("tearDown", envHome, true);
            TestUtils.removeFiles("tearDown", envHome, FileManager.DEL_SUFFIX);
        } catch (Throwable e) {
            System.out.println("tearDown: " + e);
        }

        db = null;
        env = null;
        envHome = null;
    }

    /**
     * Opens the environment and database.
     */
    private void openEnv()
        throws DatabaseException {

        EnvironmentConfig config = TestUtils.initEnvConfig();
	DbInternal.disableParameterValidation(config);
        config.setAllowCreate(true);
        /* Do not run the daemons. */
        config.setConfigParam
            (EnvironmentParams.ENV_RUN_CLEANER.getName(), "false");
        config.setConfigParam
            (EnvironmentParams.ENV_RUN_EVICTOR.getName(), "false");
        config.setConfigParam
	    (EnvironmentParams.ENV_RUN_CHECKPOINTER.getName(), "false");
        config.setConfigParam
            (EnvironmentParams.ENV_RUN_INCOMPRESSOR.getName(), "false");
        /* Use a small log file size to make cleaning more frequent. */
        config.setConfigParam(EnvironmentParams.LOG_FILE_MAX.getName(),
                              Integer.toString(1024));
        /* Use a small memory size to force eviction. */
        config.setConfigParam(EnvironmentParams.MAX_MEMORY.getName(),
                              Integer.toString(1024 * 96));
        /* Don't track detail with a tiny cache size. */
        config.setConfigParam
            (EnvironmentParams.CLEANER_TRACK_DETAIL.getName(), "false");
        config.setConfigParam(EnvironmentParams.NUM_LOG_BUFFERS.getName(),
                              Integer.toString(2));
        /* Set log buffers large enough for trace messages. */
        config.setConfigParam(EnvironmentParams.LOG_MEM_SIZE.getName(),
                              Integer.toString(7000));
        
        env = new Environment(envHome, config);

        openDb();
    }

    /**
     * Opens that database.
     */
    private void openDb()
        throws DatabaseException {

        DatabaseConfig dbConfig = new DatabaseConfig();
        dbConfig.setAllowCreate(true);
        dbConfig.setSortedDuplicates(true);
        db = env.openDatabase(null, DB_NAME, dbConfig);
    }

    /**
     * Closes the environment and database.
     */
    private void closeEnv()
        throws DatabaseException {

        if (db != null) {
            db.close();
            db = null;
        }
        if (env != null) {
            env.close();
            env = null;
        }
    }

    /**
     */
    public void testSR10553()
        throws DatabaseException {

        openEnv();

        /* Put some duplicates, enough to fill a log file. */
        final int COUNT = 10;
        DatabaseEntry key = new DatabaseEntry(TestUtils.getTestArray(0));
        DatabaseEntry data = new DatabaseEntry();
        for (int i = 0; i < COUNT; i += 1) {
            data.setData(TestUtils.getTestArray(i));
            db.put(null, key, data);
        }
        Cursor cursor = db.openCursor(null, null);
        assertEquals(OperationStatus.SUCCESS,
                     cursor.getSearchKey(key, data, null));
        assertEquals(COUNT, cursor.count());
        cursor.close();

        /* Delete everything.  Do not compress. */
        db.delete(null, key);

        /* Checkpoint and clean. */
        env.checkpoint(forceConfig);
        int cleaned = env.cleanLog();
        assertTrue("cleaned=" + cleaned, cleaned > 0);

        /* Force eviction. */
        env.evictMemory();

        /* Scan all values. */
        cursor = db.openCursor(null, null);
        for (OperationStatus status = cursor.getFirst(key, data, null);
                             status == OperationStatus.SUCCESS;
                             status = cursor.getNext(key, data, null)) {
        }
        cursor.close();

        /*
         * Before the fix to 10553, while scanning over deleted records, a
         * LogFileNotFoundException would occur when faulting in a deleted
         * record, if the log file had been cleaned.  This was because the
         * cleaner was not setting knownDeleted for deleted records.
         */
        closeEnv();
    }
}
