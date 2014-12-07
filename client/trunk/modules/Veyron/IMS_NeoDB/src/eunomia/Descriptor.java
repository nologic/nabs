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
        return "NeoDB";
    }

    public int moduleType() {
        return TYPE_PROC;
    }

    public String longDescription() {
        return "Neoflow BDB test module";
    }

    public String shortDescription() {
        return "Neoflow BDB test";
    }

    public int version() {
        return 0;
    }

    public Dependency[] getDependencies() {
        return new Dependency[] {new Dependency("NEOFlow", TYPE_FLOW)};
    }

}
