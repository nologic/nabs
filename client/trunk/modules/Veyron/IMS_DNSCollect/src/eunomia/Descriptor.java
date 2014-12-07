package eunomia;

import com.vivic.eunomia.module.Dependency;

/**
 *
 * @author justin
 */
public class Descriptor implements com.vivic.eunomia.module.Descriptor {

    public String moduleName() {
        return "dnsCollect";
    }

    public int moduleType() {
        return TYPE_PROC;
    }

    public String longDescription() {
        return "DNS flow collection module";
    }

    public String shortDescription() {
        return "DNS flow collector";
    }

    public int version() {
        return 0;
    }

    public Dependency[] getDependencies() {
        return new Dependency[] {new Dependency("DNSFlow", TYPE_FLOW),
                                 new Dependency("imsCore", Descriptor.TYPE_LIBB)};
    }
}