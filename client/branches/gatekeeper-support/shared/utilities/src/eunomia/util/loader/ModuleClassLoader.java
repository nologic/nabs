/*
 * ModuleClassLoader.java
 *
 * Created on October 22, 2006, 8:32 PM
 *
 */

package eunomia.util.loader;

import eunomia.util.io.EunomiaObjectInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.apache.log4j.Logger;

/**
 *
 * @author Mikhail Sosonkin
 */
public class ModuleClassLoader extends URLClassLoader {
    private static final String EUNOMIA_DESCRIPTOR = "eunomia.Descriptor";
    private static final int EUNOMIA_DESCRIPTOR_HASH = EUNOMIA_DESCRIPTOR.hashCode();
    
    private static final String JAVA_IO_OBJECTINPUTSTREAM = "java.io.ObjectInputStream";
    private static final int JAVA_IO_OBJECTINPUTSTREAM_HASH = JAVA_IO_OBJECTINPUTSTREAM.hashCode();
    
    private static final String EUNOMIA_UTIL_IO_EUNOMIAOBJECTINPUTSTREAM = "eunomia.util.io.EunomiaObjectInputStream";
    private static final int EUNOMIA_UTIL_IO_EUNOMIAOBJECTINPUTSTREAM_HASH = EUNOMIA_UTIL_IO_EUNOMIAOBJECTINPUTSTREAM.hashCode();
    
    private static Logger logger;
    private Set loaders;
    
    ModuleClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
        
        loaders = new HashSet();
    }
    
    public ModuleClassLoader(URL[] urls) {
        super(urls);
        
        loaders = new HashSet();
    }
    
    public String toString() {
        StringBuilder b = new StringBuilder();
        
        b.append(hashCode() + " ");
        Iterator it = loaders.iterator();
        while (it.hasNext()) {
            URLClassLoader cl = (URLClassLoader) it.next();
            URL[] us = cl.getURLs();
            for (int i = 0; i < us.length; ++i) {
                try {
                    File file = new File(us[i].toURI());
                    b.append(file.getName() + ";");
                } catch (URISyntaxException ex) {
                    ex.printStackTrace();
                }
            }
        }
        
        return b.toString();
    }
    
    public void addPath(File file) {
        try {
            addURL(file.toURI().toURL());
        } catch (MalformedURLException ex) {
            ex.printStackTrace();
        }
    }
    
    public void addLoader(ClassLoader loader) {
        loaders.add(loader);
    }
    
    public synchronized Class loadClass(String name, boolean resolve) throws ClassNotFoundException {
        int hash = name.hashCode();
        
        if(hash == EUNOMIA_DESCRIPTOR_HASH && name.equals(EUNOMIA_DESCRIPTOR)){
            return findClass(name);
        } /*else if(hash == JAVA_IO_OBJECTINPUTSTREAM_HASH && name.equals(JAVA_IO_OBJECTINPUTSTREAM)) {
            logger.warn("Please use '" + EunomiaObjectInputStream.class + "' instead of '" + name +"'");
        } */else if(hash == EUNOMIA_UTIL_IO_EUNOMIAOBJECTINPUTSTREAM_HASH && name.equals(EUNOMIA_UTIL_IO_EUNOMIAOBJECTINPUTSTREAM)) {
            return getDuplicate_EunomiaObjectInputStream();
        }
        
        Class klass = null;
            /*try {
                klass = super.loadClass(name, resolve);
                if(name.indexOf("NABFlow") != -1) {
                    System.out.println(name + " found in " + this);
                }
                
                return klass;
            } catch(Throwable th) {
            }
            
            if(klass == null) {*/
            Iterator it = loaders.iterator();
            while (it.hasNext()) {
                ClassLoader loader = (ClassLoader) it.next();
                try {
                    klass = loader.loadClass(name);
                } catch(Throwable ex) {
                }

                if(klass != null) {
                    return klass;
                }
            }
          
        return super.loadClass(name, resolve);
    }
    
    // Java Voodoo
    private Class getDuplicate_EunomiaObjectInputStream() throws ClassNotFoundException {
        Class klass = EunomiaObjectInputStream.class;
        Class dupKlass = null;
        
        try {
            InputStream in = this.getParent().getResourceAsStream("eunomia/util/io/EunomiaObjectInputStream.class");
            byte[] bytes = new byte[in.available()];
            in.read(bytes);
            
            dupKlass = defineClass(klass.getName(), bytes, 0, bytes.length);
            
            Method method = dupKlass.getMethod("setClassLoader", ClassLoader.class);
            method.invoke(null, this);
        } catch(Exception e){
            e.printStackTrace();
        }
        
        return dupKlass;
    }
}
