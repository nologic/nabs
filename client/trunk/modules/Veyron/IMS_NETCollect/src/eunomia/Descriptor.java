/*
 * Descriptor.java
 *
 * Created on January 14, 2008, 8:18 PM
 *
 */

package eunomia;

import com.vivic.eunomia.module.Dependency;

/**
 *
 * @author Mikhail Sosonkin
 */
public class Descriptor implements com.vivic.eunomia.module.Descriptor {
    
    public Descriptor() {
    }

    public String moduleName() {
        return "netCollect";
    }

    public int moduleType() {
        return Descriptor.TYPE_PROC;
    }

    public String longDescription() {
        return "Data collector module for IMS";
    }

    public String shortDescription() {
        return "IMS Data Collector";
    }

    public int version() {
        return 0;
    }

    public Dependency[] getDependencies() {
        return new Dependency[] {new Dependency("NABFlowV2", Descriptor.TYPE_FLOW),
                                 new Dependency("NEOFlow", Descriptor.TYPE_FLOW),
                                 new Dependency("imsCore", Descriptor.TYPE_LIBB)};
    }
}