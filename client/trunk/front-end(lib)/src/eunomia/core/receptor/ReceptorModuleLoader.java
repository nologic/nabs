/*
 * ReceptorModuleClassLoader.java
 *
 * Created on December 25, 2007, 4:37 PM
 *
 */

package eunomia.core.receptor;

import com.vivic.eunomia.sys.frontend.ConsoleContext;
import com.vivic.eunomia.sys.frontend.ConsoleReceptor;
import eunomia.util.loader.ModuleClassLoader;
import java.lang.reflect.Method;
import java.net.URL;

/**
 *
 * @author Mikhail Sosonkin
 */
public class ReceptorModuleLoader extends ModuleClassLoader {
    private static final String CONSOLE_CONTEXT = ConsoleContext.class.getName();
    
    private Receptor receptor;
    
    public ReceptorModuleLoader(URL url, Receptor receptor) {
        super( (url == null?new URL[]{}:new URL[]{url}) );
        
        this.receptor = receptor;
    }
    
    public synchronized Class loadClass(String name, boolean resolve) throws ClassNotFoundException {
        if(name.equals(CONSOLE_CONTEXT)) {
            return getConsoleContext();
        }
        
        return super.loadClass(name, resolve);
    }
    
    private Class getConsoleContext() throws ClassNotFoundException {
        Class context = this.getDuplicateClass(CONSOLE_CONTEXT);
        
        try {
            Method method = context.getMethod("setReceptor", ConsoleReceptor.class);
            method.invoke(null, receptor);
        } catch (Exception e) {
            throw new ClassNotFoundException(e.getMessage());
        }
        
        return context;
    }
}