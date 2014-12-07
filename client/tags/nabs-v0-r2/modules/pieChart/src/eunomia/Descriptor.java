/*
 * Descriptor.java
 *
 * Created on October 19, 2006, 10:52 PM
 *
 */

package eunomia;

/**
 * This is the class universal to all types of modules. It is used to describe
 * specific parameters needed to present the module to the user and to load the
 * module into the Eunomia system.
 * @author Mikhail Sosonkin
 */
public class Descriptor implements eunomia.plugin.interfaces.Descriptor {
    public String moduleName() {
        return "pieChart";
    }

    public int moduleType() {
        return eunomia.plugin.interfaces.Descriptor.TYPE_PROC;
    }

    public String shortDescription() {
        return "Pie Chart";
    }

    public String longDescription() {
        return "Shows a destribution of data types.";
    }

    public int version() {
        return 0;
    }
}