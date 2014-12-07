/*
 * EunomiaConfiguration.java
 *
 * Created on December 22, 2006, 8:59 PM
 *
 */

package eunomia;

import com.vivic.eunomia.sys.receptor.SieveModuleConfig;
import java.io.FileInputStream;
import java.util.Properties;

/**
 *
 * @author Mikhail Sosonkin
 */
public class EunomiaConfiguration {
    private static final String SettingsFile = "config.nab";
    
    private static final String DBD_DIR = "BDB.dir";
    private static final String DBD_CACHE = "BDB.EachCacheBytes";
    
    private static Properties properties;
    private static boolean lock = false;
 
    static {
        properties = new Properties();
    }
    
    public static void lock() {
        lock = true;
    }
    
    public static void setValue(String key, String val) {
        if(!lock) {
            properties.setProperty(key, val);
        }
    }
    
    public static SieveModuleConfig getModuleConfig() {
        return new SieveModuleConfig() {
            public String getString(String prop) {
                return properties.getProperty(prop);
            }
        };
    }
    
    public static int getBDBCache() {
        return getInt(DBD_CACHE, 14773686);
    }
    
    public static String getBDBDir() {
        return getString(DBD_DIR, "./data_maps");
    }
    
    public static int getInt(String key, int def) {
        return Integer.parseInt(getString(key, Integer.toString(def)));
    }
    
    public static String getString(String key, String def){
        return properties.getProperty(key, def);
    }
}