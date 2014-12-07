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
        return "lossyHistogram";
    }

    public int moduleType() {
        return com.vivic.eunomia.module.Descriptor.TYPE_PROC;
    }

    public String shortDescription() {
        return "Lossy Counting Histogram";
    }

    public String longDescription() {
        return "Provides information on most active flows.";
    }

    public int version() {
        return 0;
    }
    
    public Dependency[] getDependencies() {
        return new Dependency[]{new Dependency("NABFlow", Descriptor.TYPE_FLOW),
                                new Dependency("NABFlowV2", Descriptor.TYPE_FLOW),
                                new Dependency("jcommon", Descriptor.TYPE_LIBB),
                                new Dependency("jfreechart", Descriptor.TYPE_LIBB)};
    }
}