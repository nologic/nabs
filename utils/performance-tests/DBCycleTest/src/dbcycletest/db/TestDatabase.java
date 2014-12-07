package dbcycletest.db;

import com.sleepycat.collections.StoredKeySet;
import com.sleepycat.collections.StoredMap;
import com.sleepycat.collections.StoredValueSet;
import com.sleepycat.db.Cursor;
import com.sleepycat.db.CursorConfig;
import com.sleepycat.db.Database;
import com.sleepycat.db.DatabaseConfig;
import com.sleepycat.db.DatabaseEntry;
import com.sleepycat.db.DatabaseException;
import com.sleepycat.db.DatabaseType;
import com.sleepycat.db.Environment;
import com.sleepycat.db.EnvironmentConfig;
import com.sleepycat.db.OperationStatus;
import com.sleepycat.db.SecondaryConfig;
import com.sleepycat.db.SecondaryCursor;
import com.sleepycat.db.SecondaryDatabase;
import com.sleepycat.db.internal.Db;
import dbcycletest.FlowID;
import dbcycletest.db.bindings.FlowIDTimeKeyBinding;
import dbcycletest.db.bindings.FlowIDEntityBinding;
import dbcycletest.db.bindings.FlowIDFlowIDKeyBinding;
import dbcycletest.db.bindings.FlowIDHostKeyBinding;
import dbcycletest.db.creators.FlowIDEndTimeKeyCreator;
import dbcycletest.db.creators.FlowIDExistTimeKeyCreator;
import dbcycletest.db.creators.FlowIDFlowIDMultiKeyCreator;
import dbcycletest.db.creators.FlowIDHostKeyCreator;
import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Field;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 *
 * @author Justin Stallard
 */
public class TestDatabase {
    private static final long ENV_CACHE_SIZE = 128*1024*1024;
    private static final long PRI_CACHE_SIZE = 20*1024;//*1024;
    private static final long SEC_CACHE_SIZE = 64*1024*1024;
    private int DB_COUNT;
    
    private ReentrantReadWriteLock rwLock;
    
    private Environment env;
    private boolean sharedEnvironment;;
    private Database db;
    private File primaryFile;
    private DatabaseType primaryType;
    private SecondaryDatabase flowIDsByFlowID;
    private File flowIDsByFlowIDFile;
    private SecondaryDatabase flowIDsByHost;
    private File flowIDsByHostFile;
    private SecondaryDatabase flowIDsByEndTime;
    private File flowIDsByEndTimeFile;
    private SecondaryDatabase flowIDsByExistTime;
    private File flowIDsByExistTimeFile;
    private boolean openState;
    private boolean useEnvironment;
    private boolean useSecondaries;
    private DatabaseEntry key;
    private DatabaseEntry data;
    private FlowIDEntityBinding flowIDBinding;
    private int index;
    private String path;
    
    public TestDatabase(String path, int index) {
        openState = false;
        useEnvironment = false;
        useSecondaries = true;
        flowIDBinding = new FlowIDEntityBinding();
        primaryType = DatabaseType.QUEUE;
        key = new DatabaseEntry();
        data = new DatabaseEntry();
        flowIDsByFlowID = null;
        flowIDsByHost = null;
        flowIDsByEndTime = null;
        flowIDsByExistTime = null;
        this.index = index;
        this.path = path;
        
        rwLock = new ReentrantReadWriteLock();
        sharedEnvironment = false;
        
        DB_COUNT = 1;
    }
    
    public boolean setUseEnvironment(boolean useEnvironment) {
        if (openState) {
            return false;
        }
        this.useEnvironment = useEnvironment;
        return true;
    }
    
    public boolean setEnvironment(Environment env) {
        if (!setUseEnvironment(true)) {
            return false;
        }
        
        this.env = env;
        sharedEnvironment = true;
        return true;
    }
    
    public boolean setUseSecondaries(boolean useSecondaries) {
        if (openState) {
            return false;
        }
        
        this.useSecondaries = useSecondaries;
        DB_COUNT = 5;
        return true;
    }
    
    public boolean setPrimaryType(DatabaseType primaryType) {
        if (openState) {
            return false;
        }
        
        this.primaryType = primaryType;
        return true;
    }
    
    public boolean open(boolean readOnly) {
        try {
            File dbPath = new File(path);
            if (!dbPath.exists()) {
                dbPath.mkdir();
            }
            if (useEnvironment) {
                if (env == null) {
                    //System.err.println("Opening environment...");
                    //path += "/writeEnv";
                    openEnvironment(readOnly);
                } else {
                    //System.err.println("TestDatabase: Opening DB in existing environment.");
                }
            }// else if (!readOnly) {
                //path += "/writeEnv";
            //}
            
            openDatabase(readOnly);
            //System.err.println("DB cache size: " + db.getConfig().getCacheSize() + " bytes");
            
            if (useSecondaries) {
                openSecondaries(readOnly);
                /*
                if (flowIDsByFlowID != null) {
                    System.err.println("FlowIDsByFlowID cache size: " + flowIDsByFlowID.getConfig().getCacheSize() + " bytes");
                }
                if (flowIDsByHost != null) {
                    System.err.println("FlowIDsByHost cache size: " + flowIDsByHost.getConfig().getCacheSize() + " bytes");
                }
                if (flowIDsByEndTime != null) {
                    System.err.println("FlowIDsByEndTime cache size: " + flowIDsByEndTime.getConfig().getCacheSize() + " bytes");
                }
                if (flowIDsByExistTime != null) {
                    System.err.println("FlowIDsByExistTime cache size: " + flowIDsByExistTime.getConfig().getCacheSize() + " bytes");
                }
                */
            }

            openState = true;
            
            /*
            if (useEnvironment) {
                System.err.println("TestDatabase: Database opened successfully in environment.");
            }
            */
            
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            
            return false;
        }
    }
    
    public boolean close() {
        try {
            if (openState == false) {
                return true;
            }
            if (useSecondaries) {
                closeSecondaries();
            }
            closeDatabase();
            if (!sharedEnvironment && useEnvironment) {
                closeEnvironment();
            } else if (sharedEnvironment) {
                env = null;
            }
            
            openState = false;
            
            return true;
        } catch (DatabaseException ex) {
            ex.printStackTrace();
            
            return false;
        }
    }
    
    public boolean tryReadLock() {
        return (rwLock.readLock().tryLock());
    }
    
    public void releaseReadLock() {
        rwLock.readLock().unlock();
    }
    
    public void getWriteLock() {
        rwLock.writeLock().lock();
    }
    
    public boolean remove() {
        String flowIDsByFlowIDFileName = null;// = flowIDsByFlowID.getDatabaseFile();
        String flowIDsByHostFileName = null;// = flowIDsByHost.getDatabaseFile();
        String flowIDsByEndTimeFileName = null;// = flowIDsByEndTime.getDatabaseFile();
        String flowIDsByExistTimeFileName = null;// = flowIDsByExistTime.getDatabaseFile();
        String primaryFileName;// = db.getDatabaseFile();
        File environmentPath = null;
        EnvironmentConfig envConfig = null;
        try {
            if (useSecondaries) {
                flowIDsByFlowIDFileName = flowIDsByFlowID.getDatabaseFile();
                flowIDsByHostFileName = flowIDsByHost.getDatabaseFile();
                flowIDsByEndTimeFileName = flowIDsByEndTime.getDatabaseFile();
                flowIDsByExistTimeFileName = flowIDsByExistTime.getDatabaseFile();
            }
            primaryFileName = db.getDatabaseFile();
            if (useEnvironment) {
                environmentPath = env.getHome();
                envConfig = env.getConfig();
            }
        } catch (DatabaseException e) {
            e.printStackTrace();
            return false;
        }
        if (!close()) {
            // unable to close for some reason...
            return false;
        }
        try {
            if (useSecondaries) {
                SecondaryDatabase.remove(flowIDsByFlowIDFileName, null, null);
                SecondaryDatabase.remove(flowIDsByHostFileName, null, null);
                SecondaryDatabase.remove(flowIDsByEndTimeFileName, null, null);
                SecondaryDatabase.remove(flowIDsByExistTimeFileName, null, null);
            }
            Database.remove(primaryFileName, null, null);
            if (useEnvironment) {
                Environment.remove(environmentPath, false, envConfig);
            }
            
            return true;
        } catch (DatabaseException ex1) {
            ex1.printStackTrace();
            return false;
        } catch (FileNotFoundException ex2) {
            ex2.printStackTrace();
            return false;
        }
    }
    
    public boolean moveTo(String destPath) {
        if (openState) {
            return false;
        }
        
        // move db files
        primaryFile.renameTo(new File(destPath + File.separator + primaryFile.getName()));
        flowIDsByFlowIDFile.renameTo(new File(destPath + File.separator + flowIDsByFlowIDFile.getName()));
        flowIDsByHostFile.renameTo(new File(destPath + File.separator + flowIDsByHostFile.getName()));
        flowIDsByEndTimeFile.renameTo(new File(destPath + File.separator + flowIDsByEndTimeFile.getName()));
        flowIDsByExistTimeFile.renameTo(new File(destPath + File.separator + flowIDsByExistTimeFile.getName()));
        
        // FIXME delete environment dir, if there is one
        
        return true;
    }
    
    public boolean isOpen() {
        return openState;
    }
    
    private void openEnvironment(boolean readOnly) throws DatabaseException, FileNotFoundException {
        EnvironmentConfig envConfig = new EnvironmentConfig();
        envConfig.setAllowCreate(!readOnly);
        envConfig.setCacheSize(ENV_CACHE_SIZE);
        envConfig.setInitializeCache(true);
        envConfig.setTransactional(false);
        envConfig.setInitializeLogging(false);
        File envFile = new File(makeEnvName());
        envFile.mkdir();
        env = new Environment(envFile, envConfig);
    }
    
    private void openDatabase(boolean readOnly) throws DatabaseException, FileNotFoundException {
        DatabaseConfig dbConfig = new DatabaseConfig();
        dbConfig.setAllowCreate(!readOnly);
        dbConfig.setReadOnly(readOnly);
        dbConfig.setTransactional(false);
        dbConfig.setType(primaryType);
        if (primaryType == DatabaseType.QUEUE || primaryType == DatabaseType.RECNO) {
            dbConfig.setRecordLength(200);
        }
        if (useEnvironment) {
            db = env.openDatabase(null, makeFileName("test_database.db"), null, dbConfig);
        } else {
            dbConfig.setCacheSize(PRI_CACHE_SIZE);

            //System.err.println("Opening Primary. Path: " + path + File.separator + makeFileName("test_database.db"));
            db = new Database(path + File.separator + makeFileName("test_database.db"), null, dbConfig);
        }
        primaryFile = new File(db.getDatabaseFile());
    }
    
    private void openSecondaries(boolean readOnly) throws DatabaseException, FileNotFoundException {
        openSecondaryFlowID(readOnly);
        openSecondaryHost(readOnly);
        openSecondaryEndTime(readOnly);
        openSecondaryExistTime(readOnly);
    }
    
    private void openSecondaryFlowID(boolean readOnly) throws DatabaseException, FileNotFoundException {
        SecondaryConfig secConfig = new SecondaryConfig();
        secConfig.setAllowCreate(!readOnly);
        secConfig.setAllowPopulate(true);
        secConfig.setSortedDuplicates(true);
        secConfig.setTransactional(false);
        secConfig.setType(DatabaseType.HASH);
        secConfig.setMultiKeyCreator(new FlowIDFlowIDMultiKeyCreator());
        
        if (useEnvironment) {
            flowIDsByFlowID = env.openSecondaryDatabase(null, makeFileName("sec_flowid.db"), null, db, secConfig);
        } else {
            secConfig.setCacheSize(SEC_CACHE_SIZE);
            flowIDsByFlowID = new SecondaryDatabase(path + File.separator + makeFileName("sec_flowid.db"), null, db, secConfig);
            try {
                // Go Ninja, Mike - 1, BDB - 0.
                Field f = Database.class.getDeclaredField("db");
                f.setAccessible(true);
                Db dd = (Db) f.get(flowIDsByFlowID);
                dd.get_env().wrapper = db.getEnvironment();
                f.setAccessible(false);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        flowIDsByFlowIDFile = new File(flowIDsByFlowID.getDatabaseFile());
    }
    
    private void openSecondaryHost(boolean readOnly) throws DatabaseException, FileNotFoundException {
        SecondaryConfig secConfig = new SecondaryConfig();
        secConfig.setAllowCreate(!readOnly);
        secConfig.setAllowPopulate(true);
        secConfig.setSortedDuplicates(true);
        secConfig.setTransactional(false);
        secConfig.setType(DatabaseType.HASH);
        secConfig.setMultiKeyCreator(new FlowIDHostKeyCreator());
        
        if (useEnvironment) {
            flowIDsByHost = env.openSecondaryDatabase(null, makeFileName("sec_host.db"), null, db, secConfig);
        }
        else {
            secConfig.setCacheSize(SEC_CACHE_SIZE);
            flowIDsByHost = new SecondaryDatabase(path + File.separator + makeFileName("sec_host.db"), null, db, secConfig);
            try {
                // Go Ninja, Mike - 1, BDB - 0.
                Field f = Database.class.getDeclaredField("db");
                f.setAccessible(true);
                Db dd = (Db) f.get(flowIDsByHost);
                dd.get_env().wrapper = db.getEnvironment();
                f.setAccessible(false);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        flowIDsByHostFile = new File(flowIDsByHost.getDatabaseFile());
    }
    
    private void openSecondaryEndTime(boolean readOnly) throws DatabaseException, FileNotFoundException {
        SecondaryConfig secConfig = new SecondaryConfig();
        secConfig.setAllowCreate(!readOnly);
        secConfig.setAllowPopulate(true);
        secConfig.setSortedDuplicates(true);
        secConfig.setTransactional(false);
        secConfig.setType(DatabaseType.BTREE);
        secConfig.setKeyCreator(new FlowIDEndTimeKeyCreator());
        
        if (useEnvironment) {
            flowIDsByEndTime = env.openSecondaryDatabase(null, makeFileName("sec_endtime.db"), null, db, secConfig);
        } else {
            secConfig.setCacheSize(SEC_CACHE_SIZE);
            flowIDsByEndTime = new SecondaryDatabase(path + File.separator + makeFileName("sec_endtime.db"), null, db, secConfig);
            try {
                // Go Ninja, Mike - 1, BDB - 0.
                Field f = Database.class.getDeclaredField("db");
                f.setAccessible(true);
                Db dd = (Db) f.get(flowIDsByEndTime);
                dd.get_env().wrapper = db.getEnvironment();
                f.setAccessible(false);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        flowIDsByEndTimeFile = new File(flowIDsByEndTime.getDatabaseFile());
    }
    
    private void openSecondaryExistTime(boolean readOnly) throws DatabaseException, FileNotFoundException {
        SecondaryConfig secConfig = new SecondaryConfig();
        secConfig.setAllowCreate(!readOnly);
        secConfig.setAllowPopulate(true);
        secConfig.setSortedDuplicates(true);
        secConfig.setTransactional(false);
        secConfig.setType(DatabaseType.BTREE);
        secConfig.setMultiKeyCreator(new FlowIDExistTimeKeyCreator());
        
        if (useEnvironment) {
            flowIDsByExistTime = env.openSecondaryDatabase(null, makeFileName("sec_existtime.db"), null, db, secConfig);
        } else {
            secConfig.setCacheSize(SEC_CACHE_SIZE);
            flowIDsByExistTime = new SecondaryDatabase(path + File.separator + makeFileName("sec_existtime.db"), null, db, secConfig);
            try {
                // Go Ninja, Mike - 1, BDB - 0.
                Field f = Database.class.getDeclaredField("db");
                f.setAccessible(true);
                Db dd = (Db) f.get(flowIDsByExistTime);
                dd.get_env().wrapper = db.getEnvironment();
                f.setAccessible(false);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        flowIDsByExistTimeFile = new File(flowIDsByExistTime.getDatabaseFile());
    }
    
    private void closeSecondaries() throws DatabaseException {
        db.sync();
        if (flowIDsByFlowID != null) {
            flowIDsByFlowID.close();
        }
        if (flowIDsByHost != null) {
            flowIDsByHost.close();
        }
        if (flowIDsByEndTime != null) {
            flowIDsByEndTime.close();
        }
        if (flowIDsByExistTime != null) {
            flowIDsByExistTime.close();
        }
    }
    
    private void closeDatabase() throws DatabaseException {
        db.close();
    }
    
    private void closeEnvironment() throws DatabaseException {
        env.close();
    }
    
    public boolean add(FlowID id) {
        try {
            flowIDBinding.objectToKey(id, key);
            flowIDBinding.objectToData(id, data);
            if (key == null) {
                System.err.println("TestDatabase.add(): key is null.");
            }
            if (data == null) {
                System.err.println("TestDatabase.add(): data is null.");
            }
            if (primaryType == DatabaseType.QUEUE || primaryType == DatabaseType.RECNO) {
                if (db.append(null, key, data) != OperationStatus.SUCCESS) {
                    return false;
                }
            } else if (primaryType == DatabaseType.BTREE || primaryType == DatabaseType.HASH) {
                if (db.put(null, key, data) != OperationStatus.SUCCESS) {
                    return false;
                }
            } else {
                return false;
            }
            
            return true;
        } catch (DatabaseException ex) {
            ex.printStackTrace();
            
            return false;
        }
    }
    
    private String makeEnvName() {
        StringBuilder sb = new StringBuilder();
        long tmp = Math.round(Math.floor(Math.log10(index)));
        if (tmp < 0) {
            tmp = 0;
        }
        sb.append(path);
        sb.append('/');
        for (long i = 0; i < (8 - tmp); ++i) {
            sb.append('0');
        }

        sb.append(index);
        
        return sb.toString();
    }
    
    private String makeFileName(String name) {
        StringBuilder sb = new StringBuilder();
        long tmp = Math.round(Math.floor(Math.log10(index)));
        if (tmp < 0) {
            tmp = 0;
        }
        
        for (long i = 0; i < (8 - tmp); ++i) {
            sb.append('0');
        }

        sb.append(index);
        sb.append('_');
        sb.append(name);
        
        return sb.toString();
    }
    
    public void setIndex(int index) {
        this.index = index;
    }
    
    public Cursor getPrimaryCursor(boolean readOnly) {
        try {
            CursorConfig cConfig = new CursorConfig();
            cConfig.setWriteCursor(!readOnly);
            return db.openCursor(null, cConfig);
        } catch (DatabaseException ex) {
            ex.printStackTrace();
            return null;
        }
    }
    
    public SecondaryCursor getFlowIDsByFlowIDCursor() {
        try {
            CursorConfig cConfig = new CursorConfig();
            cConfig.setWriteCursor(false);
            return flowIDsByFlowID.openSecondaryCursor(null, cConfig);
        } catch (DatabaseException ex) {
            ex.printStackTrace();
            return null;
        }
    }
    
    public SecondaryCursor getFlowIDsByHostCursor() {
        try {
            CursorConfig cConfig = new CursorConfig();
            cConfig.setWriteCursor(false);
            return flowIDsByHost.openSecondaryCursor(null, cConfig);
        } catch (DatabaseException ex) {
            ex.printStackTrace();
            return null;
        }
    }
    
    public SecondaryCursor getFlowIDsByEndTimeCursor() {
        try {
            CursorConfig cConfig = new CursorConfig();
            cConfig.setWriteCursor(false);
            return flowIDsByEndTime.openSecondaryCursor(null, cConfig);
        } catch (DatabaseException ex) {
            ex.printStackTrace();
            return null;
        }
    }
    
    public SecondaryCursor getFlowIDsByExistTimeCursor() {
        try {
            CursorConfig cConfig = new CursorConfig();
            cConfig.setWriteCursor(false);
            return flowIDsByExistTime.openSecondaryCursor(null, cConfig);
        } catch (DatabaseException ex) {
            ex.printStackTrace();
            return null;
        }
    }
    
    public StoredValueSet getPrimaryValueSet(boolean readOnly) {
        return new StoredValueSet(db, flowIDBinding, readOnly);
    }
    
    public StoredMap getFlowIDsByFlowIDMap() {
        return new StoredMap(flowIDsByFlowID, new FlowIDFlowIDKeyBinding(), flowIDBinding, false);
    }
    
    public StoredKeySet getFlowIDsByFlowIDKeySet() {
        return new StoredKeySet(flowIDsByFlowID, new FlowIDFlowIDKeyBinding(), false);
    }
    
    public StoredMap getFlowIDsByHostMap() {
        return new StoredMap(flowIDsByHost, new FlowIDHostKeyBinding(), flowIDBinding, false);
    }
    
    public StoredMap getFlowIDsByEndTimeMap() {
        return new StoredMap(flowIDsByEndTime, new FlowIDTimeKeyBinding(), flowIDBinding, false);
    }
    
    public StoredMap getFlowIDsByExistTimeMap() {
        return new StoredMap(flowIDsByExistTime, new FlowIDTimeKeyBinding(), flowIDBinding, false);
    }

    public DatabaseType getPrimaryType() {
        return primaryType;
    }
}