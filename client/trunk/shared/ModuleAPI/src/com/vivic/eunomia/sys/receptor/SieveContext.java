/*
 * SieveContext.java
 *
 * Created on July 22, 2007, 12:21 AM
 *
 */

package com.vivic.eunomia.sys.receptor;

/**
 * On the Sieve side there is only one module context, compared to Console where
 * it can connect to multiple Sieves. To access the various components of the system
 * from within the module, the user will need to use this class to retrieve
 * instances of Managers.
 * @author Mikhail Sosonkin
 */
public class SieveContext {
    private static SieveModuleManager modMan;
    private static SieveDataManager dataMan;
    private static SieveModuleServices modServ;
    private static SieveModuleConfig modConfig;

    public static SieveModuleServices getModuleServices() {
        return modServ;
    }
    
    /**
     * Returns the current module manager instance.
     * @return Module manager object.
     */
    public static SieveModuleManager getModuleManager() {
        return modMan;
    }
    
    /**
     * Returns the the current data manager instance.
     * @return Data manager object.
     */
    public static SieveDataManager getDataManager() {
        return dataMan;
    }
    
    /*
     * Returns a property from the configuration file for named module. For now
     * module name is not enforced, but it will be.
     */
    public static String getModuleProperty(String module, String prop) {
        return modConfig.getString(module + "." + prop);
    }

    // Assingments, can be done only once so that the modules can't change things.
    public static void setModuleServices(SieveModuleServices m) {
        if(modServ == null) {
            modServ = m;
        }
    }
    
    /**
     * This is a setter for the module manager. This method only allows for one instance,
     * once it is assigned all futher calls will be ignored. Sieve will perform the
     * assignment before any modules are instantiated, so the modules can assume that
     * there will be a valid module manager.
     * @param m Module managaer object
     */
    public static void setModuleManager(SieveModuleManager m) {
        if(modMan == null) {
            modMan = m;
        }
    }
    
    /**
     * This is a setter for the data manager. The same rules apply as those for
     * <CODE>setModuleManager()</CODE>.
     * @param m data manager object
     */
    public static void setDataManager(SieveDataManager m) {
        if(dataMan == null) {
            dataMan = m;
        }
    }
    
    public static void setModuleConfig(SieveModuleConfig m) {
        if(modConfig == null) {
            modConfig = m;
        }
    }
}