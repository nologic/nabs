/*
 * LibraryDescriptor.java
 *
 * Created on July 23, 2007, 1:15 AM
 *
 */

package eunomia.managers.module;

import com.vivic.eunomia.module.Dependency;
import com.vivic.eunomia.module.Descriptor;

/**
 *
 * @author Mikhail Sosonkin
 */
public class LibraryDescriptor implements Descriptor {
    private String name;
    private int version;
    
    public LibraryDescriptor(String name, String version) {
        this.name = name;
        this.version = version.hashCode();
    }

    public String moduleName() {
        return name;
    }

    public int moduleType() {
        return Descriptor.TYPE_LIBB;
    }

    public String longDescription() {
        return "This is an external library: " + name;
    }

    public String shortDescription() {
        return name;
    }

    public int version() {
        return version;
    }

    public Dependency[] getDependencies() {
        return null;
    }
}