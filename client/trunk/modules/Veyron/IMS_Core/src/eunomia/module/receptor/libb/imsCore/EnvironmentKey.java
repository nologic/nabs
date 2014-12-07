/*
 * EnvironmentKey.java
 *
 * Created on January 27, 2008, 10:48 AM
 *
 */

package eunomia.module.receptor.libb.imsCore;

import eunomia.module.receptor.libb.imsCore.bind.BoundObject;
import java.io.Serializable;

/**
 *
 * @author Mikhail Sosonkin
 */
public abstract class EnvironmentKey implements BoundObject {
    
    public EnvironmentKey() {
    }
    
    public abstract int getEnvID();
    public abstract EnvironmentKey clone();
}
