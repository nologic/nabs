/*
 * SieveContext.java
 *
 * Created on July 22, 2007, 12:21 AM
 *
 */

package com.vivic.eunomia.sys.receptor;

/**
 *
 * @author Mikhail Sosonkin
 */
public class SieveContext {
    private static SieveModuleManager modMan;

    public static SieveModuleManager getModuleManager() {
        return modMan;
    }

    public static void setModuleManager(SieveModuleManager m) {
        if(modMan == null) {
            modMan = m;
        }
    }
    
}