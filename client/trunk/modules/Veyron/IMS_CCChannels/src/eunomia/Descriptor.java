/*
 * Descriptor.java
 *
 * Created on November 3, 2007, 5:01 PM
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
        return "ccChannels";
    }

    public int moduleType() {
        return Descriptor.TYPE_ANLZ;
    }

    public String longDescription() {
        return "Finds command and control channels";
    }

    public String shortDescription() {
        return "C&C Channels";
    }

    public int version() {
        return 0;
    }

    public Dependency[] getDependencies() {
        return new Dependency[] { new Dependency("imsCore", Descriptor.TYPE_LIBB) };
    }
}