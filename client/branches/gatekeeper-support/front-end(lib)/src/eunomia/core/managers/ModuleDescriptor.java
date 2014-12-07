/*
 * ModuleDescriptor.java
 *
 * Created on July 4, 2006, 9:15 PM
 *
 */

package eunomia.core.managers;

import com.vivic.eunomia.module.Descriptor;
import eunomia.util.Util;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.security.NoSuchAlgorithmException;

/**
 *
 * @author Mikhail Sosonkin
 */
public class ModuleDescriptor {
    public static final String types[] = new String[]{"Realtime Flow Processor", "Flow Generator", "Forensic Analyzer", "Database Collector", "External Library"};
    
    private String path;
    private Descriptor desc;
    private ClassLoader loader;
    private byte[] hash;
    
    public ModuleDescriptor(String p, ClassLoader l, Descriptor d) {
        path = p;
        loader = l;
        desc = d;
    }
    
    public byte[] getHash() {
        if(hash == null) {
            try {
                hash = Util.md5(new File(path).toURI());
            } catch (NoSuchAlgorithmException ex) {
                ex.printStackTrace();
            } catch (MalformedURLException ex) {
                ex.printStackTrace();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        
        return hash;
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