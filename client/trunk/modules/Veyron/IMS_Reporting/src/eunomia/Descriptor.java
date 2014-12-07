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
        return "imsSqlReport";
    }

    public int moduleType() {
        return Descriptor.TYPE_ANLZ;
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
            new Dependency("imsCore", Descriptor.TYPE_LIBB)
        };
    }
}