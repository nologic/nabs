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
        return "NABFlow";
    }

    public int moduleType() {
        return eunomia.plugin.interfaces.Descriptor.TYPE_FLOW;
    }
    
    public String shortDescription() {
        return "NAB Flow";
    }

    public String longDescription() {
        return "NABS flow record interpreter.";
    }

    public int version() {
        return 0;
    }
}