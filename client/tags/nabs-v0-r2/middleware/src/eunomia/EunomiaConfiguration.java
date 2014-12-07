/*
 * EunomiaConfiguration.java
 *
 * Created on December 22, 2006, 8:59 PM
 *
 */

package eunomia;

import java.io.FileInputStream;
import java.util.Properties;

/**
 *
 * @author Mikhail Sosonkin
 */
public class EunomiaConfiguration {
    private static final String SettingsFile = "config.nab";
    private static final String LISTEN_PORT = "Port";
    
    private static Properties properties;
 
    static {
        properties = new Properties();
        try {
            FileInputStream fin = new FileInputStream(SettingsFile);
            properties.load(fin);
            fin.close();
        } catch (Exception e){
            System.out.println("Unable to read configuration file: " + SettingsFile + " (" + e.getMessage() + ")");
        }
    }
    
    public static int getListenPort() {
        return getInt(LISTEN_PORT, 4185);
    }
    
    public static int getInt(String key, int def) {
        return Integer.parseInt(getString(key, Integer.toString(def)));
    }
    
    public static String getString(String key, String def){
        return properties.getProperty(key, def);
    }
}