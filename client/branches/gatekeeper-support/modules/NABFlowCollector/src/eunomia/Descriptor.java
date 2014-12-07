/*
 * Descriptor.java
 *
 * Created on December 27, 2006, 8:15 PM
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
        return "nabFlowCollector";
    }

    public int moduleType() {
        return com.vivic.eunomia.module.Descriptor.TYPE_COLL;
    }
    
    public String shortDescription() {
        return "NAB Flow collector";
    }

    public String longDescription() {
        return "Collects NAB Flows into an SQL Database";
    }

    public int version() {
        return 0;
    }
    
    public Dependency[] getDependencies() {
        return new Dependency[]{new Dependency("NABFlow", Descriptor.TYPE_FLOW)};
    }
}