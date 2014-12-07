/*
 * Descriptor.java
 *
 * Created on October 19, 2006, 11:31 PM
 *
 */

package eunomia;

/**
 *
 * @author Mikhail Sosonkin
 */
public class Descriptor implements eunomia.plugin.interfaces.Descriptor {
    public String moduleName() {
        return "hostDetails";
    }

    public int moduleType() {
        return eunomia.plugin.interfaces.Descriptor.TYPE_PROC;
    }

    public String shortDescription() {
        return "Host Details";
    }

    public String longDescription() {
        return "Shows detailed information about a specific host.";
    }

    public int version() {
        return 0;
    }
}