/*
 * Descriptor.java
 *
 * Created on October 19, 2006, 10:52 PM
 *
 */

package eunomia;

import com.vivic.eunomia.module.Dependency;

/**
 *
 * @author Mikhail Sosonkin
 */
public class Descriptor implements com.vivic.eunomia.module.Descriptor {
    public String moduleName() {
        return "NEOFlow";
    }

    public int moduleType() {
        return com.vivic.eunomia.module.Descriptor.TYPE_FLOW;
    }
    
    public String shortDescription() {
        return "NEO Flow";
    }

    public String longDescription() {
        return "NEO Flow record interpreter.";
    }

    public int version() {
        return 0;
    }
    
    public Dependency[] getDependencies() {
        return null;
    }
}