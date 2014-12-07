/*
 * ModuleFile.java
 *
 * Created on April 18, 2007, 8:39 PM
 *
 */

package eunomia.managers.module;

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
public class ModuleFile {
    public static final String types[] = new String[]{"Realtime Flow Processor", "Flow Generator", "Forensic Analyzer", "Database Collector", "External Library"};
    
    private File path;
    private Descriptor desc;
    private ClassLoader loader;
    private byte[] hash;
    
    public ModuleFile(File p, ClassLoader l, Descriptor d) {
        path = p;
        loader = l;
        desc = d;
    }
    
    public byte[] getHash() {
        if(hash == null) {
            try {
                hash = Util.md5(path.toURI());
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

    public File getPath() {
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
}