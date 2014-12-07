/*
 * DatabaseConfig.java
 *
 * Created on December 25, 2006, 1:05 AM
 *
 */

package eunomia.config;

import com.sleepycat.bind.EntryBinding;
import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;
import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

/**
 *
 * @author Mikhail Sosonkin
 */
public class DatabaseAccess implements Runnable {
    private EnvironmentConfig config;
    private Environment environ;
    private DatabaseConfig dbConfig;
    private Database data;
    private File dbDir;
    private long lastSave;
    private boolean hasChanged;
    private Timer timer;
    
    public DatabaseAccess(File file) throws DatabaseException {
        file.mkdirs();
        dbDir = file;
        hasChanged = false;
        timer = new Timer("Config Timer");

        timer.schedule(new TimerTask() {
            public void run() {
                if(hasChanged) {
                    try {
                        ensureSave();
                    } catch (DatabaseException ex) {
                    }
                }
            }
        }, 2000, 1000);
        
        
        openDatabase();
        Runtime.getRuntime().addShutdownHook(new Thread(this));
    }
    
    public void lazySave() {
        hasChanged = true;
    }
    
    public synchronized void ensureSave() throws DatabaseException {
        // is there a better way of doing this w/ BDB?
        hasChanged = false;
        closeDatabase();
        openDatabase();
    }
    
    public synchronized void closeDatabase() throws DatabaseException {
        if(dbConfig.getDeferredWrite()){
            data.sync();
        }
        data.close();
        environ.cleanLog();
        environ.close();
    }
    
    public synchronized void openDatabase() throws DatabaseException {
        config = new EnvironmentConfig();
        config.setAllowCreate(true);
        
        environ = new Environment(dbDir, config);
        
        dbConfig = new DatabaseConfig();
        dbConfig.setAllowCreate(true);
        
        data = environ.openDatabase(null, "EunomiaConfig", dbConfig);
    }
    
    public void putBoolean(String key, boolean value) throws DatabaseException {
        putBoundObject(key, Boolean.valueOf(value), Boolean.class);
    }
    
    public boolean getBoolean(String key) throws DatabaseException {
        return ((Boolean)getBoundObject(key, Boolean.class)).booleanValue();
    }
    
    public void putString(String key, String value) throws DatabaseException {
        putBoundObject(key, value, String.class);
    }
    
    public String getString(String key) throws DatabaseException {
        return (String)getBoundObject(key, String.class);
    }
    
    public void putInt(String key, int value) throws DatabaseException {
        putBoundObject(key, Integer.valueOf(value), Integer.class);
    }
    
    public int getInt(String key) throws DatabaseException {
        return ((Integer)getBoundObject(key, Integer.class)).intValue();
    }
    
    public synchronized void putData(String key, byte[] value) throws DatabaseException {
        DatabaseEntry keyEntry = new DatabaseEntry(key.getBytes());
        DatabaseEntry valueEntry = new DatabaseEntry(value);
    
        data.put(null, keyEntry, valueEntry);
        if(dbConfig.getDeferredWrite()){
            data.sync();
        }
        environ.sync();
    }
    
    public synchronized byte[] getData(String key) throws DatabaseException {
        DatabaseEntry keyEntry = new DatabaseEntry(key.getBytes());
        DatabaseEntry valueEntry = new DatabaseEntry();
        byte[] ret = null;
        
        OperationStatus status = data.get(null, keyEntry, valueEntry, LockMode.DEFAULT);
        
        if(status == OperationStatus.SUCCESS){
            ret = valueEntry.getData();
        }
        
        return ret;
    }
    
    public synchronized void remData(String key) throws DatabaseException {
        DatabaseEntry keyEntry = new DatabaseEntry(key.getBytes());
        
        data.removeSequence(null, keyEntry);
    }
    
    private synchronized void putBoundObject(String key, Object obj, Class klass) throws DatabaseException {
        DatabaseEntry keyEntry = new DatabaseEntry(key.getBytes());
        DatabaseEntry valueEntry = new DatabaseEntry();
        
        EntryBinding binding = TupleBinding.getPrimitiveBinding(klass);
        binding.objectToEntry(obj, valueEntry);
        
        data.put(null, keyEntry, valueEntry);
    }
    
    private synchronized Object getBoundObject(String key, Class klass) throws DatabaseException {
        DatabaseEntry keyEntry = new DatabaseEntry(key.getBytes());
        DatabaseEntry valueEntry = new DatabaseEntry();
        EntryBinding binding = TupleBinding.getPrimitiveBinding(klass);
        
        OperationStatus status = data.get(null, keyEntry, valueEntry, LockMode.DEFAULT);
        if(status == OperationStatus.SUCCESS) {
            return binding.entryToObject(valueEntry);
        }
        
        throw new DatabaseException("Field '" + key + "' not found");
    }

    public void run() {
        try {
            ensureSave();
        } catch (DatabaseException ex) {
        }
    }
}