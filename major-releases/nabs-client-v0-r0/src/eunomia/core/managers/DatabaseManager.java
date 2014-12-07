/*
 * DatabaseManager.java
 *
 * Created on June 28, 2005, 6:06 PM
 *
 */

package eunomia.core.managers;

import java.util.*;
import java.io.*;

import eunomia.core.data.staticData.*;
import eunomia.core.data.staticData.db.*;
import eunomia.config.*;
import eunomia.core.data.streamData.*;
import eunomia.core.managers.listeners.*;

import org.apache.log4j.*;

/**
 *
 * @author Mikhail Sosonkin
 */
public class DatabaseManager {
    public static final DatabaseManager ins = new DatabaseManager();
    
    private List listeners;
    private List databases;
    private HashMap reqSerialToStream;
    
    private static Logger logger;
    
    static {
        logger = Logger.getLogger(DatabaseManager.class);
    }
    
    public DatabaseManager() {
        databases = new LinkedList();
        listeners = new LinkedList();
        reqSerialToStream = new HashMap();
    }

    public void addDatabaseManagerListener(DatabaseManagerListener l){
        listeners.add(l);
    }
    
    public void removeDatabaseManagerListener(DatabaseManagerListener l){
        listeners.remove(l);
    }
    
    private void fireRemovedEvent(Database db){
        Iterator it = listeners.iterator();
        
        while(it.hasNext()){
            ((DatabaseManagerListener)it.next()).databaseRemoved(db);
        }
    }
    
    private void fireAddedEvent(Database db){
        Iterator it = listeners.iterator();
        
        while(it.hasNext()){
            ((DatabaseManagerListener)it.next()).databaseAdded(db);
        }
    }
    
    public Database createDefaultDatabase(String name) throws Exception {
        Database db = new MySQLDatabase(name);
        addDatabase(db);
        
        return db;
    }
    
    private void addDatabaseNoFire(Database db){
        databases.add(db);
    }
    
    public void addDatabase(Database db){
        addDatabaseNoFire(db);
        fireAddedEvent(db);
    }
    
    private void removeDatabaseNoFire(Database db){
        databases.remove(db);
    }
    
    public void removeDatabase(Database db){
        removeDatabaseNoFire(db);
        fireRemovedEvent(db);
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
    
    public void putRequestForDB(StreamDataSource sds, int serial) throws Exception {
        Database db = getDatabaseBySerial(serial);
        if(db != null){
            sds.addDatabase(db);
        } else {
            Integer i = Integer.valueOf(serial);
            reqSerialToStream.put(i, sds);
        }
    }
    
    public void saveDatabases() throws IOException {
        Config config = Config.getConfiguration("manager.databases");
        
        config.setArray("databases", databases.toArray());
        
        Iterator it = databases.iterator();
        while(it.hasNext()){
            Database db = (Database)it.next();
            db.save();
        }
        
        config.save();
    }
    
    public void loadDatabases() throws Exception {
        Config config = Config.getConfiguration("manager.databases");
        Object[] names = config.getArray("databases", null);
        if(names == null){
            return;
        }
        
        for(int i = 0; i < names.length; i++){
            try {
                Database db = new MySQLDatabase(names[i].toString());
                db.load();
                addDatabaseNoFire(db);
                db.connect();
            } catch (Exception e){
                e.printStackTrace();
            }
        }
        fillRequests();
    }
    
    private void fillRequests() throws Exception {
        Iterator it = reqSerialToStream.keySet().iterator();
        
        while(it.hasNext()){
            Integer i = (Integer)it.next();
            StreamDataSource sds = (StreamDataSource)reqSerialToStream.get(i);
            Database db = getDatabaseBySerial(i.intValue());
            sds.addDatabase(db);
        }
    }
}