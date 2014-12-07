/*
 * Config.java
 *
 * Created on June 24, 2005, 7:33 PM
 *
 */

package eunomia.config;

import java.io.*;
import java.util.*;

/**
 *
 * @author Mikhail Sosonkin
 */

public class Config {
    private Properties prop;
    private File file;
    private List listeners;
    
    private Config(File f) {
        file = f;
        f.getParentFile().mkdirs();
        prop = new Properties();
        listeners = new LinkedList();
        try {
            FileInputStream fin = new FileInputStream(f);
            prop.loadFromXML(fin);
            fin.close();
        } catch(Exception e){
        }
    }
    
    public String getString(String field, String def){
        return prop.getProperty(field, def);
    }
    
    public int getInt(String field, int def){
        try {
            return Integer.parseInt(getString(field, "b"));
        } catch(Exception e){
            e.printStackTrace();
        }
        
        return def;
    }
    
    public boolean getBoolean(String field, boolean def){
        try {
            return Boolean.parseBoolean(getString(field, "b"));
        } catch(Exception e){
            e.printStackTrace();
        }
        
        return def;
    }
    
    public byte[] getBytes(String field, byte[] def){
        return null;
    }
    
    public void setArray(String field, Object[] arr){
        int i = 0;
        for(i = 0; i < arr.length; i++){
            setString(field + "." + i, arr[i].toString());
        }
        
        String remField = field + "." + i;
        while(prop.containsKey(remField)){
            prop.remove(remField);
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

    public void setString(String field, String value){
        prop.setProperty(field, value);
    }
    
    public void setInt(String field, int value){
        setString(field, Integer.toString(value));
    }
    
    public void setBoolean(String field, boolean value){
        setString(field, Boolean.toString(value));
    }
    
    public void setBytes(String field){
    }
    
    public void save() throws IOException {
        FileOutputStream fout = new FileOutputStream(file);
        prop.storeToXML(fout, "Settings For Eunomia");
        fout.close();
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
    
    public static final Config getConfiguration(String name){
        String fileName = System.getProperty("user.home") + File.separator + ".eunomia" + File.separator + name.replace('.', File.separatorChar) + ".xml";
        return new Config(new File(fileName));
    }
}