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
public class EunomiaClassLoader extends URLClassLoader {
    public EunomiaClassLoader(){
        this(new URL[]{}, ClassLoader.getSystemClassLoader());
    }
    
    public EunomiaClassLoader(URL[] urls, ClassLoader parent) {
	super(urls, parent);
    }

    public EunomiaClassLoader(URL[] urls) {
	super(urls);
    }
    
    public void addURL(URL url) {
        super.addURL(url);
    }

    public synchronized Class loadClass(String name, boolean resolve)
	throws ClassNotFoundException
    {
        return super.loadClass(name, resolve);
    }
}
