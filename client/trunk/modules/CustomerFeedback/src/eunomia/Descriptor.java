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
        return "feedBack";
    }

    public int moduleType() {
        return com.vivic.eunomia.module.Descriptor.TYPE_PROC;
    }

    public String shortDescription() {
        return "Customer Feedback";
    }

    public String longDescription() {
        return "Provides a direct line of communication between the customer and Vivic LLC.";
    }

    public int version() {
        return 0;
    }
    
    public Dependency[] getDependencies() {
        return null;
    }
}