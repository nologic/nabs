/*
 * Descriptor.java
 *
 * Created on December 4, 2007, 10:27 PM
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
        return "imsCore";
    }

    public int moduleType() {
        return Descriptor.TYPE_LIBB;
    }

    public String longDescription() {
        return null;
    }

    public String shortDescription() {
        return null;
    }

    public int version() {
        return 0;
    }

    public Dependency[] getDependencies() {
        return new Dependency[] {
            new Dependency("db", Descriptor.TYPE_LIBB),
            new Dependency("mysql", Descriptor.TYPE_LIBB),
        };
    }
}