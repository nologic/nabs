/*
 * Descriptor.java
 *
 * Created on February 11, 2008, 8:35 PM
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
        return "exProcAnlz";
    }

    public int moduleType() {
        return Descriptor.TYPE_PROC;
    }

    public String longDescription() {
        return "Example module for creating Analysis and Processing Veyron modules.";
    }

    public String shortDescription() {
        return "Analysis and Processing Veyron module";
    }

    public int version() {
        return 0;
    }

    public Dependency[] getDependencies() {
        return new Dependency[] {
            new Dependency("rtCollect", Descriptor.TYPE_PROC),
            new Dependency("imsCore", Descriptor.TYPE_LIBB),
            new Dependency("bootIms", Descriptor.TYPE_ANLZ)
        };
    }

}
