/*
 * Descriptor.java
 *
 * Created on October 19, 2006, 10:52 PM
 *
 */

package eunomia;

/**
 *
 * @author Mikhail Sosonkin
 */
public class Descriptor implements eunomia.plugin.interfaces.Descriptor {
    public String moduleName() {
        return "hostView";
    }

    public int moduleType() {
        return eunomia.plugin.interfaces.Descriptor.TYPE_PROC;
    }

    public String longDescription() {
        return "Provides a view of a specific host's activity.";
    }

    public String shortDescription() {
        return "Host View";
    }

    public int version() {
        return 0;
    }
}