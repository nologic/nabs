/*
 * EnvironmentEntry.java
 *
 * Created on January 6, 2008, 5:41 PM
 *
 */

package eunomia.module.receptor.libb.imsCore;

import eunomia.module.receptor.libb.imsCore.bind.BoundObject;
import eunomia.module.receptor.libb.imsCore.db.NetEnv;

/**
 *
 * @author Mikhail Sosonkin
 */
public abstract class EnvironmentEntry implements BoundObject {
    protected transient StoreEnvironment env;
    
    public EnvironmentEntry(StoreEnvironment e) {
        this.env = e;
    }
    
    public void setEnv(StoreEnvironment e) {
        env = e;
    }
    
    public abstract EnvironmentKey getKey();
    public abstract EnvironmentEntry clone();
}
