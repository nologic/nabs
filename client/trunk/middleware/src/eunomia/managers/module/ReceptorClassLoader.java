/*
 * ReceptorClassLoader.java
 *
 * Created on March 19, 2008, 11:21 PM
 *
 */

package eunomia.managers.module;

import eunomia.util.loader.ModuleClassLoader;
import java.net.URL;
import java.util.HashSet;

/**
 *
 * @author Mikhail Sosonkin
 */
public class ReceptorClassLoader extends ModuleClassLoader {
    private String username;
    private ModuleClassLoader loader;
    
    public ReceptorClassLoader(ModuleClassLoader loader) {
        super(null);
        this.loader = loader;
    }
    
    public ReceptorClassLoader(URL[] urls) {
        super(urls);
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
