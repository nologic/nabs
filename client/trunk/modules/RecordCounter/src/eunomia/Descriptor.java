/*
 * Descriptor.java
 *
 * Created on November 21, 2006, 9:38 PM
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
        return "recordCounter";
    }

    public int moduleType() {
        return Descriptor.TYPE_ANLZ;
    }

    public String longDescription() {
        return "Counts flows recorded on a database";
    }

    public String shortDescription() {
        return "Record Counter";
    }

    public int version() {
        return 0;
    }

    public Dependency[] getDependencies() {
        return null;
    }
}