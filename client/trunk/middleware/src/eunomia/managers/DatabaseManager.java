/*
 * DatabaseManager.java
 *
 * Created on June 28, 2005, 6:06 PM
 *
 */

package eunomia.managers;

import com.vivic.eunomia.sys.data.DataMap;
import com.vivic.eunomia.sys.receptor.SieveDataManager;
import com.vivic.eunomia.sys.util.Util;
import eunomia.EunomiaConfiguration;
import eunomia.config.Config;
import eunomia.data.Database;
import eunomia.data.relational.JDBCDatabase;
import eunomia.util.loader.ModuleClassLoader;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.Driver;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;


/**
 *
 * @author Mikhail Sosonkin
 */
public class DatabaseManager implements SieveDataManager {
    private List listeners;
    private List databases;
    private Map reqSerialToStream;
    private Map typeToClass;
    private Map nameToDriver;
    private Map nameToMapdb;
    private ThreadGroup analGroup;
    private List bdbEnvs;
    
    private static Logger logger;
    private static DatabaseManager ins;
    
    static {
        logger = Logger.getLogger(DatabaseManager.class);
    }
    
    private DatabaseManager() throws Exception {
        analGroup = new ThreadGroup("Analysis Thread Group");
        analGroup.setMaxPriority(Thread.MIN_PRIORITY);

        typeToClass = new HashMap();
        
        databases = new LinkedList();
        listeners = new LinkedList();
        reqSerialToStream = new HashMap();
        nameToDriver = new HashMap();
        nameToMapdb = new HashMap();
        bdbEnvs = new LinkedList();
    }
    
    public void shutdown() {
    }
    
    public DataMap getDBMap(String name, Map paramMap) throws Exception {
        return null;
    }
    
    public ThreadGroup getThreadGroup() {
        return analGroup;
    }

    public Database createDefaultDatabase(String type, String name) throws Exception {
        Class cls = (Class)typeToClass.get(type);
        if(cls != null){
            Database db = (Database)cls.newInstance();
            db.setName(name);
            db.setJdbcType(type);
            addDatabase(db);

            return db;
        }
        
        return null;
    }
    
    public void addJDBCDatabase(String name, Driver driver) throws ClassNotFoundException {
        typeToClass.put(name, JDBCDatabase.class);
        nameToDriver.put(name, driver);
    }
    
    public Driver getDriver(String name) {
        return (Driver)nameToDriver.get(name);
    }
    
    public List getJDBCList() {
        return Arrays.asList(typeToClass.keySet().toArray());
    }
    
    private void addDatabaseNoFire(Database db){
        databases.add(db);
    }
    
    public void addDatabase(Database db){
        addDatabaseNoFire(db);
    }
    
    private void removeDatabaseNoFire(Database db){
        databases.remove(db);
    }
    
    public void removeDatabase(Database db){
        removeDatabaseNoFire(db);
    }
    
    public List getDatabaseList(){
        return Collections.unmodifiableList(databases);
    }
    
    public Database getDatabaseBySerial(int serial){
        List list = getDatabaseList();
        Iterator it = list.iterator();
        while(it.hasNext()){
            Database db = (Database)it.next();
            if(db.getSerial() == serial){
                return db;
            }
        }
        
        return null;
    }
    
    public Database getDatabaseByName(String name){
        List list = getDatabaseList();
        Iterator it = list.iterator();
        while(it.hasNext()){
            Database db = (Database)it.next();
            if(db.getName().equals(name)){
                return db;
            }
        }
        
        return null;
    }
    
    public void saveDatabases() throws IOException {
        Config config = Config.getConfiguration("manager.databases");
        
        String[] names = new String[databases.size()];
        String[] types = new String[databases.size()];
        
        Iterator it = databases.iterator();
        int i = 0;
        while(it.hasNext()){
            Database db = (Database)it.next();
            
            names[i] = db.getName();
            types[i] = db.getJdbcType();
            
            db.save();
            
            ++i;
        }

        config.setArray("databases", names);
        config.setArray("types", types);
        
        config.save();
    }
    
    public void loadDatabases() throws Exception {
        Config config = Config.getConfiguration("manager.databases");
        Object[] names = config.getArray("databases", null);
        Object[] types = config.getArray("types", null);
        
        if(names == null){
            return;
        }
        
        for(int i = 0; i < names.length; i++){
            try {
                Database db = createDefaultDatabase(types[i].toString(), names[i].toString());
                if(db != null) {
                    db.load();
                }
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }
    
    public static DatabaseManager v(){
        if(ins == null){
            try {
                ins = new DatabaseManager();
            } catch(Exception e){
                e.printStackTrace();
            }
        }
        
        return ins;
    }
}