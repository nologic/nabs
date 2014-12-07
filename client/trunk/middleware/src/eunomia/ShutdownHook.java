package eunomia;

import eunomia.managers.ClientManager;
import eunomia.managers.DatabaseManager;
import eunomia.managers.ModuleManager;
import eunomia.managers.ReceptorManager;
import org.apache.log4j.Logger;

/**
 *
 * @author Mikhail Sosonkin
 */
public class ShutdownHook extends Thread {
    private static Logger logger;
    
    static {
        logger = Logger.getLogger(ShutdownHook.class);
    }
    
    public ShutdownHook() {
    }
    
    public void run() {
        logger.info("Shutting down");
        ClientManager.v().shutdown();
        ReceptorManager.v().shutdown();
        ModuleManager.v().shutdown();
        DatabaseManager.v().shutdown();
        logger.info("Great working with you, bye bye");
    }
}