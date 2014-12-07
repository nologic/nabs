/*
 * Descriptor.java
 *
 * Created on December 15, 2006, 11:29 AM
 *
 */

package eunomia;

import com.vivic.eunomia.module.Dependency;

/**
 *
 * @author kulesh
 */
public class Descriptor implements com.vivic.eunomia.module.Descriptor{

    public String moduleName() {
        return "networkPolicy";
    }

    public int moduleType() {
        return com.vivic.eunomia.module.Descriptor.TYPE_PROC;
    }

    public String longDescription() {
        return "Allows user to create network traffic policies and enforce them.";
    }

    public String shortDescription() {
        return "Network Policy Manager";
    }

    public int version() {
        return 0;
    }
    
    public Dependency[] getDependencies() {
        return new Dependency[]{new Dependency("NABFlow", Descriptor.TYPE_FLOW),
                                new Dependency("jcommon", Descriptor.TYPE_LIBB),
                                new Dependency("jfreechart", Descriptor.TYPE_LIBB)};
    }
}
