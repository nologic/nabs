/*
 * Descriptor.java
 *
 * Created on November 21, 2006, 9:38 PM
 *
 */

package eunomia;

/**
 *
 * @author Mikhail Sosonkin
 */
public class Descriptor implements eunomia.plugin.interfaces.Descriptor {
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
}