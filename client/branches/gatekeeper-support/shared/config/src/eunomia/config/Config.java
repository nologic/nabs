/*
 * Config.java
 *
 * Created on June 24, 2005, 7:33 PM
 *
 */

package eunomia.config;

import com.sleepycat.je.DatabaseException;
import java.io.*;
import java.util.*;

/**
 *
 * @author Mikhail Sosonkin
 */

public class Config {
    private static DatabaseAccess database;
    private static Map keyToConfig;
    private static String globalName;
    
    private List listeners;
    private String prefix;
    
    static {
        keyToConfig = Collections.synchronizedMap(new HashMap());
    }
    
    private Config(String keyPrefix) {
        prefix = "config." + keyPrefix + ".";
        listeners = new LinkedList();
    }
    
    public boolean containsField(String field) {
        try {
            return database.getData(prefix + field) != null;
        } catch (DatabaseException ex) {
            return false;
        }
    }
    
    public void deleteField(String field) {
        try {
            database.remData(prefix + field);
        } catch (DatabaseException ex) {
            //ex.printStackTrace();
        }
    }

    public void setArray(String field, Object[] arr){
        int i = 0;
        for(i = 0; i < arr.length; i++){
            setString(field + "." + i, arr[i].toString());
        }
        
        String remField = field + "." + i;
        while(containsField(remField)){
            deleteField(remField);
            remField = field + "." + ++i;
        }
    }
    
    public Object[] getArray(String field, Object[] def){
        Vector v = new Vector();
        int i = 0;
        
        String f = field + "." + i;
        String val;
        while( (val = getString(f, null)) != null){
            v.add(val);
            f = field + "." + ++i;
        }
        
        return v.toArray();
    }

    public String getString(String field, String def){
        try {
            String ret = database.getString(prefix + field);
            if(ret != null){
                return ret;
            }
        } catch (Exception e){
            //e.printStackTrace();
        }
        
        return def;
    }
    
    public void setString(String field, String value){
        try {
            database.putString(prefix + field, value);
        } catch (DatabaseException ex) {
            //ex.printStackTrace();
        }
    }
    
    public int getInt(String field, int def){
        try {
            return database.getInt(prefix + field);
        } catch (DatabaseException ex) {
            //ex.printStackTrace();
        }
        
        return def;
    }
    
    public void setInt(String field, int value){
        try {
            database.putInt(prefix + field, value);
        } catch (DatabaseException ex) {
            //ex.printStackTrace();
        }
    }

    public boolean getBoolean(String field, boolean def){
        try {
            return database.getBoolean(prefix + field);
        } catch (DatabaseException ex) {
            //ex.printStackTrace();
        }
        
        return def;
    }
    
    public void setBoolean(String field, boolean value){
        try {
            database.putBoolean(prefix + field, value);
        } catch (DatabaseException ex) {
            //ex.printStackTrace();
        }
    }

    public void setBytes(String field, byte[] bytes){
        try {
            database.putData(prefix + field, bytes);
        } catch (DatabaseException ex) {
            ex.printStackTrace();
        }
    }
    
    public byte[] getBytes(String field, byte[] def){
        try {
            return database.getData(prefix + field);
        } catch (DatabaseException ex) {
            return def;
        }
    }

    public void save() {
        database.lazySave();
        fireConfigChangeEvent();
    }
    
    private void fireConfigChangeEvent(){
        Iterator it = listeners.iterator();
        while(it.hasNext()){
            ((ConfigChangeListener)it.next()).configurationChanged();
        }
    }
    
    public void addConfigChangeListener(ConfigChangeListener l){
        listeners.add(l);
    }
    
    public void removeConfigChangeListener(ConfigChangeListener l){
        listeners.remove(l);
    }
    
    public static final void setGlobalName(String name) throws DatabaseException{
        globalName = name;
        database = new DatabaseAccess(new File(System.getProperty("user.home") + File.separator + ".eunomia" + File.separator + globalName));
    }
    
    public static final Config getConfiguration(String name){
        if(database == null) {
            try {
                database = new DatabaseAccess(new File(System.getProperty("user.home") + File.separator + ".eunomia" + File.separator + globalName));
            } catch (DatabaseException ex) {
                ex.printStackTrace();
                return null;
            }
        }
        
        Config config = (Config)keyToConfig.get(name);
        if(config == null){
            config = new Config(name);
            keyToConfig.put(name, config);
        }
        
        return config;
    }

    public static void closeAll() {
        try {
            database.closeDatabase();
            database = null;
        } catch (DatabaseException ex) {
            //ex.printStackTrace();
        }
    }
}