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
        return "streamStatus";
    }

    public int moduleType() {
        return eunomia.plugin.interfaces.Descriptor.TYPE_PROC;
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
}