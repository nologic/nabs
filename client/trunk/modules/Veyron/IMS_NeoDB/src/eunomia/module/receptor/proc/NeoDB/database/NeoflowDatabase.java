/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package eunomia.module.receptor.proc.NeoDB.database;

import com.sleepycat.collections.StoredMap;
import com.sleepycat.collections.StoredValueSet;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.je.SecondaryConfig;
import com.sleepycat.je.SecondaryDatabase;
import eunomia.module.receptor.proc.NeoDB.bindings.NeoflowEntityBinding;
import eunomia.module.receptor.proc.NeoDB.bindings.UnsignedIntegerBinding;
import eunomia.module.receptor.proc.NeoDB.creators.NeoflowEndTimeKeyCreator;
import eunomia.module.receptor.proc.NeoDB.creators.NeoflowExistTimeKeyCreator;
import eunomia.module.receptor.proc.NeoDB.creators.NeoflowHostKeyCreator;
import eunomia.module.receptor.proc.NeoDB.creators.NeoflowStartTimeKeyCreator;
import eunomia.receptor.module.NEOFlow.NEOFlow;
import java.io.File;
import java.util.Set;

/**
 *
 * @author justin
 */
public class NeoflowDatabase {
    private Environment env;
    private Database db;
    private boolean openState;
    private StoredValueSet values;
    private SecondaryDatabase valuesByHostDB;
    private SecondaryDatabase valuesByStartTimeDB;
    private SecondaryDatabase valuesByEndTimeDB;
    private SecondaryDatabase valuesByExistTimeDB;
    private StoredMap valuesByHostMap;
    private StoredMap valuesByStartTimeMap;
    private StoredMap valuesByEndTimeMap;
    private StoredMap valuesByExistTimeMap;
    private static final String DB_ENVIRONMENT_DIR = "/home/justin/testenv";
            
    
    public NeoflowDatabase() {
        openState = false;
    }
    
    public boolean open() {
        System.out.println("NeoflowDatabase: opening databases");
        if (!openEnvironment()) {
            return false;
        }
        if (!openDatabase()) {
            closeEnvironment();
            return false;
        }
        openValueSet();
        openSecondaries();
        openSecondaryMaps();
        
        openState = true;
        return true;
    }
    
    public boolean add(NEOFlow f) {
        if (!openState) {
            return false;
        }
        
        values.add(f);
        return true;
    }

    public boolean isOpen() {
        return openState;
    }
    
    public void close() {
        System.out.println("NeoflowDatabase: Closing databases");
        closeSecondaries();
        closeDatabase();
        closeEnvironment();
        openState = false;
    }
    
    private boolean closeEnvironment() {
        try {
            env.close();
            return true;
        } catch (DatabaseException ex) {
            ex.printStackTrace();
            return false;
        }
    }
    
    private boolean closeDatabase() {
        try {
            db.close();
            return true;
        } catch (DatabaseException ex) {
            ex.printStackTrace();
            return false;
        }
    }
    
    private boolean closeSecondaries() {
        try {
            valuesByHostDB.close();
            valuesByStartTimeDB.close();
            valuesByEndTimeDB.close();
            valuesByExistTimeDB.close();
            return true;
        } catch (DatabaseException ex) {
            ex.printStackTrace();
            return false;
        }
    }
    
    private boolean openEnvironment() {
        try {
            EnvironmentConfig envConfig = new EnvironmentConfig();
            envConfig.setAllowCreate(true);
            envConfig.setTransactional(false);

            env = new Environment(new File(DB_ENVIRONMENT_DIR), envConfig);
            return true;
        } catch (DatabaseException ex) {
            ex.printStackTrace();
            return false;
        }
    }
    
    private boolean openDatabase() {
        try {
            DatabaseConfig dbConfig = new DatabaseConfig();
            dbConfig.setAllowCreate(true);
            dbConfig.setTransactional(false);

            db = env.openDatabase(null, "Neoflow", dbConfig);

            return true;
        } catch (DatabaseException ex) {
            ex.printStackTrace();
            return false;
        }
    }
    
    private boolean openSecondaries() {
        try {
            // Host index
            SecondaryConfig secConfig = new SecondaryConfig();
            secConfig.setAllowCreate(true);
            secConfig.setTransactional(false);
            secConfig.setSortedDuplicates(true);
            secConfig.setMultiKeyCreator(new NeoflowHostKeyCreator());
            valuesByHostDB = env.openSecondaryDatabase(null, "NeoflowHostIndex", db, secConfig);
            
            // Start time index
            secConfig = new SecondaryConfig();
            secConfig.setAllowCreate(true);
            secConfig.setTransactional(false);
            secConfig.setSortedDuplicates(true);
            secConfig.setKeyCreator(new NeoflowStartTimeKeyCreator());
            valuesByStartTimeDB = env.openSecondaryDatabase(null, "NeoflowStartTimeIndex", db, secConfig);
            
            // End time index
            secConfig = new SecondaryConfig();
            secConfig.setAllowCreate(true);
            secConfig.setTransactional(false);
            secConfig.setSortedDuplicates(true);
            secConfig.setKeyCreator(new NeoflowEndTimeKeyCreator());
            valuesByEndTimeDB = env.openSecondaryDatabase(null, "NeoflowEndTimeIndex", db, secConfig);
            
            // Exist time index
            secConfig = new SecondaryConfig();
            secConfig.setAllowCreate(true);
            secConfig.setTransactional(false);
            secConfig.setSortedDuplicates(true);
            secConfig.setMultiKeyCreator(new NeoflowExistTimeKeyCreator());
            valuesByExistTimeDB = env.openSecondaryDatabase(null, "NeoflowExistTimeIndex", db, secConfig);
            
            return true;
        } catch (DatabaseException ex) {
            ex.printStackTrace();
            return false;
        }
    }
    
    private void openSecondaryMaps() {
        valuesByHostMap = new StoredMap(valuesByHostDB, new UnsignedIntegerBinding(), new NeoflowEntityBinding(), false);
        valuesByStartTimeMap = new StoredMap(valuesByStartTimeDB, new UnsignedIntegerBinding(), new NeoflowEntityBinding(), false);
        valuesByEndTimeMap = new StoredMap(valuesByEndTimeDB, new UnsignedIntegerBinding(), new NeoflowEntityBinding(), false);
        valuesByExistTimeMap = new StoredMap(valuesByExistTimeDB, new UnsignedIntegerBinding(), new NeoflowEntityBinding(), false);
    }
    
    private void openValueSet() {
        values = new StoredValueSet(db, new NeoflowEntityBinding(), true);
    }
    
    public Set getValues() {
        return values;
    }
    
    public StoredMap getValuesByHostMap() {
        return valuesByHostMap;
    }
    
    public StoredMap getValuesByStartTimeMap() {
        return valuesByStartTimeMap;
    }
    
    public StoredMap getValuesByEndTimeMap() {
        return valuesByEndTimeMap;
    }
    
    public StoredMap getValuesByExistTimeMap() {
        return valuesByExistTimeMap;
    }
}