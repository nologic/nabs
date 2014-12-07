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
        return "hostView";
    }

    public int moduleType() {
        return com.vivic.eunomia.module.Descriptor.TYPE_PROC;
    }

    public String longDescription() {
        return "Provides a view of a specific host's activity.";
    }

    public String shortDescription() {
        return "Host View";
    }

    public int version() {
        return 0;
    }
    
    public Dependency[] getDependencies() {
        return new Dependency[]{new Dependency("NABFlow", Descriptor.TYPE_FLOW),
                                new Dependency("NABFlowV2", Descriptor.TYPE_FLOW)};
    }
}