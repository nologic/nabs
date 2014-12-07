/*
 * Descriptor.java
 *
 * Created on December 4, 2007, 10:25 PM
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
        return "bootIms";
    }

    public int moduleType() {
        return Descriptor.TYPE_ANLZ;
    }

    public String longDescription() {
        return "Starts up IMS services";
    }

    public String shortDescription() {
        return "Boot IMS";
    }

    public int version() {
        return 0;
    }

    public Dependency[] getDependencies() {
        return new Dependency[] {
            new Dependency("netCollect", Descriptor.TYPE_PROC),
            new Dependency("dnsCollect", Descriptor.TYPE_PROC),
            new Dependency("imsCore", Descriptor.TYPE_LIBB)
        };
    }
}