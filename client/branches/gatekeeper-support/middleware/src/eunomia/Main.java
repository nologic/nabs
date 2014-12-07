/*
 * Main.java
 *
 * Created on October 22, 2006, 9:48 PM
 *
 */

package eunomia;

import eunomia.config.Config;
import eunomia.exception.DependencyFailureException;
import eunomia.managers.ClientManager;
import eunomia.managers.DatabaseManager;
import eunomia.managers.ModuleManager;
import eunomia.managers.ReceptorManager;
import eunomia.managers.StateManager;
import eunomia.managers.UserManager;
import eunomia.messages.receptor.ModuleHandle;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Layout;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

/**
 *
 * @author Mikhail Sosonkin
 */
public class Main {
    private static Logger logger;
    private static EunomiaAppender appender;
    
    public static void addEventer(EunomiaEventer ap){
        appender.addEventer(ap);
    }
    
    public static void removeEventer(EunomiaEventer ap){
        appender.removeEventer(ap);
    }
    
    public static void main(String[] argv) throws Exception {
        Layout layout = new PatternLayout("%d{HH:mm:ss} %-5p: %m%n");
        appender = new EunomiaAppender(layout, System.out);
        BasicConfigurator.configure(appender);
        logger = Logger.getLogger(Main.class);

        try {
            Config.setGlobalName("receptor");
        } catch (Exception e) {
            logger.error("Unable to open configurations database: " + e.getMessage());
            logger.error("\tIs there another instance already running?");
            System.exit(1);
        }
        
        logger.info("Start up");
        
        moduleConfiguration("modules.nab");
        loadDatabasesDrivers("dbdrivers.nab");
        
        try {
            // Start flow modules
            ModuleManager.v().startFlowModules();
            // Start user manager
            UserManager.initialize("passwd.nab");
            // start the server.
            ReceptorManager.startUp();
            // start client server
            ClientManager.v();
            // load databases;
            DatabaseManager.v().loadDatabases();
        } catch (Exception e) {
            logger.error("Unable to start: " + e.getMessage());
            logger.error("\tIs there another instance already running?");
            System.exit(1);
        }
        
        try {
            ModuleHandle handle = ModuleManager.v().startModule_PROC("streamStatus");
            if(handle != null) {
                StateManager.v().addGlobalInstance(handle);
                ModuleManager.v().addDefaultConnect(handle);
            }
        } catch(Exception e){
            e.printStackTrace();
            logger.error("Unable to load streamStatus module");
        }
    }
    
    /*
     *  file format:
     *
     *  # marks a comment <br>
     *  [jar path/url] <br>
     */
    private static void moduleConfiguration(String filename) throws IOException {
        boolean announced = false;
        
        File file = new File(filename);
        if(!file.exists()){
            return;
        }
        
        FileReader fin = new FileReader(file);
        BufferedReader buf = new BufferedReader(fin);
        
        String line = null;
        
        while( (line = getNonComment(buf)) != null){
            if(!announced) {
                logger.info("Module definitions: " + filename);
                announced = true;
            }
            
            line = line.trim();
            if(line.equals("")){
                continue;
            }
            
            File jarFile = new File(line);
            if(jarFile.exists()){
                try {
                    ModuleManager.v().getLinker().loadModule(jarFile);
                } catch (DependencyFailureException e) {
                    logger.error("Unable to load module: " + jarFile + " (" + e.getMessage() + ")");
                } catch (Throwable e){
                    logger.error("Invalid module '" + jarFile + "' - " + e.toString());
                    //e.printStackTrace();
                }
            } else {
                logger.error("Jar not found: " + jarFile);
            }
        }
        
        fin.close();
    }
    
    private static void loadDatabasesDrivers(String filename) throws IOException {
        logger.info("JDBC Driver definitions: " + filename);
        
        File file = new File(filename);
        if(!file.exists()){
            logger.error("Definitions file not found: " + file.getAbsolutePath());
            return;
        }
        
        FileReader fin = new FileReader(file);
        BufferedReader buf = new BufferedReader(fin);
        
        String line = null;
        
        while( (line = getNonComment(buf)) != null){
            String[] driver = line.split(" ");
            
            if(driver.length > 2) {
                String name = driver[0];
                String classname = driver[1];
                String path = "";
                
                for(int i = 2; i < driver.length; ++i) {
                    path += " " + driver[i];
                }
                path = path.trim();
                
                try {
                    URLClassLoader cll = (URLClassLoader)ClassLoader.getSystemClassLoader();
                    Class klass = URLClassLoader.class;
                    Method method;

                    method = klass.getDeclaredMethod("addURL", URL.class);
                    // bad hack, must change at some point later. need a custom class loader.
                    method.setAccessible(true);
                    method.invoke(cll, new File(path).toURI().toURL());
                    method.setAccessible(false);
                    Class.forName(classname);
                    DatabaseManager.v().addJDBCDatabase(name);
                } catch (Exception e) {
                    e.printStackTrace();
                    continue;
                }
            }
        }
        
        fin.close();
    }
    
    private static String getNonComment(BufferedReader buf) throws IOException {
        String line = null;
        while( (line = buf.readLine()) != null && (line = line.trim()).startsWith("#"));
        
        return line;
    }
}
