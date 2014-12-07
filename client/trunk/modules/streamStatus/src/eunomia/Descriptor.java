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
        return "streamStatus";
    }

    public int moduleType() {
        return com.vivic.eunomia.module.Descriptor.TYPE_PROC;
    }

    public String shortDescription() {
        return "Stream Status";
    }

    public String longDescription() {
        return "Tracks simple flow statistics.";
    }

    public int version() {
        return 0;
    }

    public Dependency[] getDependencies() {
        return null;
    }
}