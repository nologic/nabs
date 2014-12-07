/*
 * ModuleDescriptor.java
 *
 * Created on July 4, 2006, 9:15 PM
 *
 */

package eunomia.core.managers;

import eunomia.plugin.interfaces.Descriptor;

/**
 *
 * @author Mikhail Sosonkin
 */
public class ModuleDescriptor {
    public static final String types[] = new String[]{"Realtime Flow Processor", "Flow Generator", "Forensic Analyzer", "Database Collector"};
    
    private String path;
    private Descriptor desc;
    private ClassLoader loader;
    
    public ModuleDescriptor(String p, ClassLoader l, Descriptor d) {
        path = p;
        loader = l;
        desc = d;
    }

    public String getName() {
        return desc.moduleName();
    }

    public String getPath() {
        return path;
    }

    public int getType() {
        return desc.moduleType();
    }
    
    public Descriptor getDescriptor(){
        return desc;
    }
    
    public ClassLoader getClassLoader(){
        return loader;
    }
    
    public String toString(){
        return types[getType()] + ": " + getName();
    }
}