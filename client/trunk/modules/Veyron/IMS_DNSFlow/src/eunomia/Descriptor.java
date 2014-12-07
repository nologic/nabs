package eunomia;

import com.vivic.eunomia.module.Dependency;

public class Descriptor implements com.vivic.eunomia.module.Descriptor {
    public String moduleName() {
        return "DNSFlow";
    }

    public int moduleType() {
        return TYPE_FLOW;
    }

    public String shortDescription() {
        return "DNS Flow";
    }
    
    public String longDescription() {
        return "DNS flow record collector";
    }

    public int version() {
        return 0;
    }

    public Dependency[] getDependencies() {
        return null;
    }
}
