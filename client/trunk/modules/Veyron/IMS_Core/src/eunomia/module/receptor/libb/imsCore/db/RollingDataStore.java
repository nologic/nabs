/*
 * RollingDataStore.java
 *
 * Created on May 10, 2008, 12:23 PM
 *
 */

package eunomia.module.receptor.libb.imsCore.db;

import com.sleepycat.bind.EntryBinding;
import com.sleepycat.collections.StoredMap;
import com.sleepycat.db.Database;
import com.sleepycat.db.DatabaseConfig;
import com.sleepycat.db.DatabaseEntry;
import com.sleepycat.db.DatabaseException;
import com.sleepycat.db.DatabaseType;
import com.sleepycat.db.Environment;
import com.sleepycat.db.EnvironmentConfig;
import com.sleepycat.db.SecondaryConfig;
import com.sleepycat.db.SecondaryDatabase;
import com.sleepycat.db.SecondaryKeyCreator;
import com.sleepycat.db.SecondaryMultiKeyCreator;
import com.sleepycat.db.internal.Db;
import com.vivic.eunomia.sys.receptor.SieveContext;
import com.vivic.eunomia.sys.util.Util;
import eunomia.config.Config;
import eunomia.module.receptor.libb.imsCore.EnvironmentEntry;
import eunomia.module.receptor.libb.imsCore.EnvironmentKey;
import eunomia.module.receptor.libb.imsCore.db.bdb.BDBStoredMap;
import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Mikhail Sosonkin
 */
public class RollingDataStore implements RollingDatabaseStore {
    private int maxDBCount;
    private String storeDir;
    private String storeName;
    
    private StoreDescriptor[] descs;
    private DatabaseType mainDBType;
    private int curDBnum;
    private DatabaseConfig writeDbConfig;
    private Environment readingEnv;
    
    private WrittingDB curWrite;
    private LinkedList readDbs;
    private Environment readEnv;
    
    public RollingDataStore(String name, StoreDescriptor[] descriptors, int db_count, DatabaseType type) throws Exception {
        readDbs = new LinkedList();
        
        mainDBType = type;
        maxDBCount = db_count;
        descs = descriptors;
        
        storeDir = SieveContext.getModuleProperty("imsCore", "dbDir");
        if(storeDir == null) {
            throw new RuntimeException("imsCore.dbDir must be defined in the configuration file");
        }
        
        curDBnum = 0;
        
        writeDbConfig = new DatabaseConfig();
        writeDbConfig.setAllowCreate(true);
        writeDbConfig.setTransactional(false);
        writeDbConfig.setType(type);
        writeDbConfig.setReadUncommitted(true);
        writeDbConfig.setCacheSize(256L*1024L*1024L);
        if(type == DatabaseType.QUEUE) {
            writeDbConfig.setRecordLength(512);
        }/* else {
            writeDbConfig.setPageSize(512);
        }*/
        
        EnvironmentConfig econf = new EnvironmentConfig();
        econf.setAllowCreate(true);
        econf.setNoLocking(true);
        econf.setPrivate(true);
        econf.setThreaded(true);
        econf.setInitializeCache(true);
        //econf.setRegister(true);
        //econf.setRunRecovery(true);
        econf.setTransactional(false);
        
        //System.out.println("readEnv = new Environment(" + new File(storeDir) + ", " + econf + ");");
        readEnv = new Environment(new File(storeDir), econf);
        
        open();
    }
    
    public void roll(String prefix) throws DatabaseException, FileNotFoundException {
        if(curWrite != null) {
            curWrite.close();
            
            ReadingDB remDb = null;
            ReadingDB rdb = openForReading(curWrite.getDBDir(), curWrite.getDBNum());
            synchronized(readDbs) {
                readDbs.addFirst(rdb);
            
                if(readDbs.size() > maxDBCount) {
                    remDb = (ReadingDB)readDbs.removeLast();
                }
            }
            
            if(remDb != null) {
                remDb.close();
            }
        }
        
        File dbDir = new File(storeDir + File.separator);
        dbDir.mkdirs();
        
        curWrite = new WrittingDB(descs, writeDbConfig, dbDir.toString() + File.separator, curDBnum);
        
        ++curDBnum;
    }
    
    private ReadingDB openForReading(String dir, int num) throws DatabaseException, FileNotFoundException {
        DatabaseConfig readDbConfig = new DatabaseConfig();
        readDbConfig.setReadOnly(true);
        readDbConfig.setAllowCreate(false);
        readDbConfig.setTransactional(false);
        readDbConfig.setType(mainDBType);
        if(mainDBType == DatabaseType.QUEUE) {
            readDbConfig.setRecordLength(512);
        }/* else {
            readDbConfig.setPageSize(512);
        }*/

        return new ReadingDB(descs, readDbConfig, dir, num, readEnv);
    }
    
    private void open() throws DatabaseException, FileNotFoundException {
        Config conf = Config.getConfiguration("eunomia.module.imsCore." + storeName);
        curDBnum = conf.getInt("curDBnum", -1);
        
        if(curDBnum == -1) {
            // We naver ran before, start from scratch
            curDBnum = 0;
            roll(null);
            
            return;
        }
        
        int curtmp = curDBnum;
        
        File dbFile = new File(storeDir + File.separator);
        if(!dbFile.exists()) {
            dbFile.mkdirs();
        }
        
        curWrite = new WrittingDB(descs, writeDbConfig, dbFile.toString() + File.separator, curtmp);

        try {
            for (--curtmp; curtmp >= curDBnum - maxDBCount && curtmp >= 0; --curtmp) {
                dbFile = new File(storeDir + File.separator);
                ReadingDB rdb = openForReading(dbFile.toString() + File.separator, curtmp);
                synchronized(readDbs) {
                    readDbs.addFirst(rdb);
                }
            }
        } catch (FileNotFoundException ex) {
            // we are done loading, maybe we should try continuing to 0?
        }
    }
    
    public void close() throws DatabaseException {
        curWrite.close();
        readEnv.close();
        
        Config conf = Config.getConfiguration("eunomia.module.imsCore." + storeName);
        conf.setInt("curDBnum", curDBnum);
        conf.save();
    }
    
    public Object getEntry(Object key) {
        Object[] dbs = null;
        
        synchronized(readDbs){
            // it'll do for now
            dbs = readDbs.toArray(); 
        }
        
        for (int i = 0; i < dbs.length; ++i) {
            ReadingDB db = (ReadingDB) dbs[i];
            
            Object o = db.getMap(0).get(key);
            if(o != null) {
                return o;
            }
        }
        
        return null;
    }
    
    public void putArray(EnvironmentEntry[] arr, int offset, int length) throws DatabaseException {
        curWrite.putArray(arr, offset, length);
    }
    
    public DataStoredMap getMap(int map, List sel_dbs) {
        Object[] dbs = null;
        StoredMap[] maps = null;
        
        synchronized(readDbs){
            // it'll do for now
            maps = new StoredMap[readDbs.size()];
            dbs = readDbs.toArray(); 
        }
        
        for (int i = 0; i < dbs.length; ++i) {
            ReadingDB db = (ReadingDB) dbs[i];
            StoredMap smap = db.getMap(map);
            maps[i] = smap;
        }
        
        return new BDBStoredMap(maps);
    }
    
    private SecondaryDatabase openSecondary(Database db, Object creator, String dir, String name, boolean reading, Environment env) throws DatabaseException, FileNotFoundException {
        SecondaryConfig secConfig = new SecondaryConfig();

        secConfig.setTransactional(false);
        secConfig.setSortedDuplicates(true);
        secConfig.setAllowPopulate(true);
        secConfig.setType(DatabaseType.BTREE);
        //secConfig.setReadUncommitted(false);
        //secConfig.setAllowCreate(!reading);
        secConfig.setAllowCreate(true);
        //secConfig.setReadOnly(reading);
        secConfig.setReadOnly(false);
        //secConfig.setImmutableSecondaryKey(!reading);

        if(creator instanceof SecondaryKeyCreator) {
            secConfig.setKeyCreator((SecondaryKeyCreator)creator);
        } else if(creator instanceof SecondaryMultiKeyCreator) {
            secConfig.setMultiKeyCreator((SecondaryMultiKeyCreator)creator);
        }

        if(env != null) {
            //System.out.println("return env.openSecondaryDatabase(" + null + ", " + dir + name + ", " + name + ", " + db + ", " + secConfig + ");");
            return env.openSecondaryDatabase(null, dir + name, name, db, secConfig);
        } else {
            return new SecondaryDatabase(dir + name, name, db, secConfig);
        }
    }

    public List getDatabases() {
        return null;
    }

    private class WrittingDB {
        private Database primaryDb;
        private Database[] secondaries;
        
        private EntryBinding primaryKeyBinding;
        private EntryBinding primaryValueBinding;
        private DatabaseEntry entryKey;
        private DatabaseEntry entryValue;
        
        private String dbDir;
        private int dbNum;

        public WrittingDB(StoreDescriptor[] desc, DatabaseConfig primaryConfig, String dbDir, int dbNum) throws DatabaseException, FileNotFoundException {
            this.dbNum = dbNum;
            this.dbDir = dbDir;

            entryKey = new DatabaseEntry();
            entryValue = new DatabaseEntry();

            secondaries = new Database[desc.length - 1];
            
            for (int i = 0; i < desc.length; ++i) {
                StoreDescriptor d = desc[i];
                if(i == 0) {
                    //System.out.println("primaryDb = new Database(" + dbDir + dbNum + d.getName() + ", " + dbNum + d.getName() + ", " + primaryConfig + ");");
                    primaryDb = new Database(dbDir + dbNum + d.getName(), dbNum + d.getName(), primaryConfig);
                    
                    primaryKeyBinding = d.getKeyBinding();
                    primaryValueBinding = d.getValueBinding();  
                } else {
                    secondaries[i - 1] = openSecondary(primaryDb, d.getKeyCreator(), dbDir, dbNum + d.getName(), false, null);
                }
            }
        }
        
        public void putArray(EnvironmentEntry[] arr, int offset, int length) throws DatabaseException {
            Database dbase = primaryDb;
            DatabaseType t = dbase.getConfig().getType();
            for (int i = 0; i < length; i++) {
                EnvironmentEntry ent = arr[offset + i];
                EnvironmentKey key = ent.getKey();
                
                primaryKeyBinding.objectToEntry(key, entryKey);
                primaryValueBinding.objectToEntry(ent, entryValue);
                
                if(t == DatabaseType.QUEUE) {
                    dbase.append(null, entryKey, entryValue);
                } else {
                    dbase.put(null, entryKey, entryValue);
                    //System.out.println("put: " + entryKey.getData().length + " " + entryValue.getData().length);
                }
            }
            
            long time = Util.time();
        }
        
        public void close() throws DatabaseException {
            primaryDb.sync();
            
            for (int i = 0; i < secondaries.length; ++i) {
                secondaries[i].sync();
            }
            
            for (int i = 0; i < secondaries.length; ++i) {
                 secondaries[i].close();
            }
            
            primaryDb.close();
        }
        
        public String getDBDir() {
            return dbDir;
        }
        
        public int getDBNum() {
            return dbNum;
        }
    }
    
    private class ReadingDB {
        private Database primaryDb;
        private Database[] secondaries;
        private StoredMap[] maps;
        
        private EntryBinding primaryKeyBinding;
        private EntryBinding primaryValueBinding;
        private DatabaseEntry entryKey;
        private DatabaseEntry entryValue;
        
        public ReadingDB(StoreDescriptor[] desc, DatabaseConfig primaryConfig, String dbDir, int dbNum, Environment env) throws DatabaseException, FileNotFoundException {
            entryKey = new DatabaseEntry();
            entryValue = new DatabaseEntry();

            secondaries = new Database[desc.length - 1];
            maps = new StoredMap[desc.length];
            
            for (int i = 0; i < desc.length; ++i) {
                StoreDescriptor d = desc[i];
                Database db;
                String dbName = dbNum + d.getName();
                
                if(i == 0) {
                    if(!new File(dbDir + dbName).exists()) {
                        throw new FileNotFoundException("Can't find primary database: " + dbDir + dbName);
                    }
                    
                    //System.out.println("primaryDb = env.openDatabase(" + null + ", " + dbDir + dbName + ", " + null + ", " + primaryConfig + ");");
                    primaryDb = env.openDatabase(null, dbName, null, primaryConfig);
                    //primaryDb = new Database(dbDir + d.getName(), null, primaryConfig);
                    
                    primaryKeyBinding = d.getKeyBinding();
                    primaryValueBinding = d.getValueBinding();
                    
                    db = primaryDb;
                } else {
                    System.out.println("Mapping: " + dbName + "> " + d.getKeyCreator().getClass());
                    secondaries[i - 1] = openSecondary(primaryDb, d.getKeyCreator(), dbDir, dbName, true, env);
                    db = secondaries[i - 1];
                    
                    /*try {
                        // Go Ninja, Mike - 1, BDB - 0.
                        Field f = Database.class.getDeclaredField("db");
                        f.setAccessible(true);
                        Db dd = (Db) f.get(db);
                        /*Constructor c = Environment.class.getDeclaredConstructor(DbEnv.class);
                        c.setAccessible(true);
                        c.newInstance(dd.get_env());
                        c.setAccessible(false);*/
                        /*dd.get_env().wrapper = primaryDb.getEnvironment();
                        f.setAccessible(false);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }*/
                }
                
                maps[i] = new StoredMap(db, d.getKeyBinding(), d.getValueBinding(), false);
            }
        }
        
        public StoredMap getMap(int map) {
            return maps[map];
        }
        
        public void close() throws DatabaseException {
            for (int i = 0; i < secondaries.length; ++i) {
                  secondaries[i].close();
            }
            
            primaryDb.close();
        }
    }
}