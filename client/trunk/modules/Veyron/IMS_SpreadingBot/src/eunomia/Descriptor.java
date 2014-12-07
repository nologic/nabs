package eunomia;

import com.vivic.eunomia.module.Dependency;

/**
 *
 * @author Mikhail Sosonkin
 */
public class Descriptor implements com.vivic.eunomia.module.Descriptor {

    public String moduleName() {
        return "spreadingBot";
    }

    public int moduleType() {
        return Descriptor.TYPE_ANLZ;
    }

    public String longDescription() {
        return "Detects spreading bots";
    }

    public String shortDescription() {
        return "Bot Detector";
    }

    public int version() {
        return 0;
    }

    public Dependency[] getDependencies() {
        return new Dependency[] {new Dependency("darkspace", Descriptor.TYPE_PROC), 
                                 new Dependency("ccChannels", Descriptor.TYPE_PROC),
                                 new Dependency("spammer", Descriptor.TYPE_PROC)};
    }

}
