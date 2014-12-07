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
        return "lossyHistogram";
    }

    public int moduleType() {
        return eunomia.plugin.interfaces.Descriptor.TYPE_PROC;
    }

    public String shortDescription() {
        return "Lossy Counting Histogram";
    }

    public String longDescription() {
        return "Provides information on most active flows.";
    }

    public int version() {
        return 0;
    }
}