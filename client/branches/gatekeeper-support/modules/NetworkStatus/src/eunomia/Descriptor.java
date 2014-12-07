/*
 * Descriptor.java
 *
 * Created on May 20, 2007, 2:28 PM
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
        return "networkStatus";
    }

    public int moduleType() {
        return com.vivic.eunomia.module.Descriptor.TYPE_PROC;
    }

    public String shortDescription() {
        return "Network Status";
    }

    public String longDescription() {
        return "Shows the summary of the entire network.";
    }

    public int version() {
        return 0;
    }
    
    public Dependency[] getDependencies() {
        return new Dependency[]{
            new Dependency("pieChart", Descriptor.TYPE_PROC),
            new Dependency("lossyHistogram", Descriptor.TYPE_PROC),
            new Dependency("hostDetails", Descriptor.TYPE_PROC),
        };
    }
}