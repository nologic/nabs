/*
 * Descriptor.java
 *
 * Created on December 15, 2006, 11:29 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package eunomia;

/**
 *
 * @author kulesh
 */
public class Descriptor implements eunomia.plugin.interfaces.Descriptor{

    public String moduleName() {
        return "networkPolicy";
    }

    public int moduleType() {
        return eunomia.plugin.interfaces.Descriptor.TYPE_PROC;
    }

    public String longDescription() {
        return "Allows user to create network traffic policies and enforce them.";
    }

    public String shortDescription() {
        return "Network Policy Manager";
    }

    public int version() {
        return 0;
    }

}
