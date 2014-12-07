/*
 * ModuleClassLoader.java
 *
 * Created on October 22, 2006, 8:32 PM
 *
 */

package eunomia.util.loader;

import java.net.URL;
import java.net.URLClassLoader;

/**
 *
 * @author Mikhail Sosonkin
 */
public class ModuleClassLoader extends URLClassLoader {
    ModuleClassLoader(URL[] urls, ClassLoader parent) {
	super(urls, parent);
    }

    public ModuleClassLoader(URL[] urls) {
	super(urls);
    }

    public synchronized Class loadClass(String name, boolean resolve)
	throws ClassNotFoundException
    {
        if(name.equals("eunomia.Descriptor")){
            return findClass(name);
        }
	
        return super.loadClass(name, resolve);
    }
}
