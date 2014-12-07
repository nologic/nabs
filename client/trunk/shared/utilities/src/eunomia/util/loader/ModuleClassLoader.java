/*
 * ModuleClassLoader.java
 *
 * Created on October 22, 2006, 8:32 PM
 *
 */

package eunomia.util.loader;

import eunomia.util.io.EunomiaObjectInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
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
    
    private static final String EUNOMIA_UTIL_IO_EUNOMIAOBJECTINPUTSTREAM = "eunomia.util.io.EunomiaObjectInputStream";
    private static final int EUNOMIA_UTIL_IO_EUNOMIAOBJECTINPUTSTREAM_HASH = EUNOMIA_UTIL_IO_EUNOMIAOBJECTINPUTSTREAM.hashCode();
    
    private static Logger logger;
    static {
        logger = Logger.getLogger(ModuleClassLoader.class);
    }
    
    private Set loaders;
    
    ModuleClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
        
        loaders = new HashSet();
    }
    
    public ModuleClassLoader(URL[] urls) {
        super(urls);
        
        loaders = new HashSet();
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
        } else if(hash == EUNOMIA_UTIL_IO_EUNOMIAOBJECTINPUTSTREAM_HASH && name.equals(EUNOMIA_UTIL_IO_EUNOMIAOBJECTINPUTSTREAM)) {
            return getDuplicate_EunomiaObjectInputStream();
        }
        
        Class klass = null;

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
        
        try {
            // see if it was already loaded
            klass = findLoadedClass(name);
            
            // try locally 1st.
            if(klass == null) {
                klass = super.findClass(name);
            }
        } catch (ClassNotFoundException e) {
        }
        
        // For some reason Flow classes are located by this even though they are in a different loader.
        // what the hell is going on??? This is why this is checked last.
        return super.loadClass(name, resolve);
    }

    // Java Voodoo
    private Class getDuplicate_EunomiaObjectInputStream() throws ClassNotFoundException {
        Class dupKlass = null;
        
        try {
            dupKlass = getDuplicateClass("eunomia.util.io.EunomiaObjectInputStream");
            
            Method method = dupKlass.getMethod("setClassLoader", ClassLoader.class);
            method.invoke(null, this);
        } catch(Exception e){
            e.printStackTrace();
        }
        
        return dupKlass;
    }
    
    protected Class getDuplicateClass(String name) throws ClassNotFoundException {
        String resource = name.replace('.', '/') + ".class";
        byte[] bytes = null;
        
        try {
            InputStream in = getResourceAsStream(resource);
            bytes = new byte[in.available()];
            in.read(bytes);
        } catch(IOException e) {
            throw new ClassNotFoundException(e.getMessage());
        }
        
        return defineClass(name, bytes, 0, bytes.length);
    }
}
