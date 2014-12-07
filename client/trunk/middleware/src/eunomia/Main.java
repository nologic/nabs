/*
 * Main.java
 *
 * Created on October 22, 2006, 9:48 PM
 *
 */

package eunomia;

import bsh.EvalError;
import bsh.Interpreter;
import com.vivic.eunomia.sys.receptor.SieveContext;
import com.vivic.eunomia.sys.util.Util;
import eunomia.config.Config;
import eunomia.exception.DependencyFailureException;
import eunomia.managers.ClientManager;
import eunomia.managers.DatabaseManager;
import eunomia.managers.ModuleManager;
import eunomia.managers.ReceptorManager;
import eunomia.managers.StateManager;
import eunomia.managers.UserManager;
import eunomia.messages.receptor.ModuleHandle;
import eunomia.util.ModuleServices;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
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
        
        String startScript = "init.esh";
        if(argv.length > 0) {
            startScript = argv[0];
        }
        
        // Execute Start up script.
        runStartupScript(new File(startScript));
        
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
    
    public static void load() {
        try {
            // Start flow modules
            ModuleManager.v().startFlowModules();
            // start the server.
            ReceptorManager.startUp();
            // load databases;
            DatabaseManager.v().loadDatabases();
            // set the shutdown hook
            Runtime.getRuntime().addShutdownHook(new ShutdownHook());
        } catch (Exception e) {
            logger.error("Unable to start: " + e.getMessage());
            logger.error("\tIs there another instance already running?");
            System.exit(1);
        }
        
        // Initialize the context
        SieveContext.setModuleConfig(EunomiaConfiguration.getModuleConfig());
        SieveContext.setModuleManager(ModuleManager.v());
        SieveContext.setDataManager(DatabaseManager.v());
        SieveContext.setModuleServices(ModuleServices.v());
    }
    
    private static void runStartupScript(File initsh) throws EvalError, URISyntaxException, FileNotFoundException, IOException {
        Interpreter bsh = new Interpreter();

        String initScript = new String(Util.catFile(ClassLoader.getSystemResource("eunomia/util/scripts/commands.esh").toURI()));
        bsh.eval(initScript);
        
        if(initsh.exists()) {
            bsh.source(initsh.toString());
        }
    }
}