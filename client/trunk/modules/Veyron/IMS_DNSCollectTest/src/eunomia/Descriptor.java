/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package eunomia;

import com.vivic.eunomia.module.Dependency;

/**
 *
 * @author justin
 */
public class Descriptor implements com.vivic.eunomia.module.Descriptor {

    public String moduleName() {
        return "DNSCollectTest";
    }

    public int moduleType() {
        return TYPE_ANLZ;
    }

    public String longDescription() {
        return "Module to test the functionality of DNSCollect and the entire DNS db system";
    }

    public String shortDescription() {
        return "General DNS testing module.";
    }

    public int version() {
        return 0;
    }

    public Dependency[] getDependencies() {
        return new Dependency[] {new Dependency("imsCore", Descriptor.TYPE_LIBB),
                                 new Dependency("bootIms", Descriptor.TYPE_ANLZ)};
    }

}
