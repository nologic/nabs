/*
 * Descriptor.java
 *
 * Created on November 4, 2007, 9:00 PM
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
        return "spammer";
    }

    public int moduleType() {
        return Descriptor.TYPE_PROC;
    }

    public String longDescription() {
        return "Locates spamming servers";
    }

    public String shortDescription() {
        return "Spammer";
    }

    public int version() {
        return 0;
    }

    public Dependency[] getDependencies() {
        return new Dependency[] {new Dependency("NABFlowV2", Descriptor.TYPE_FLOW)};
    }
}