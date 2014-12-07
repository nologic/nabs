/*
 * ModuleLinker.java
 *
 * Created on April 18, 2007, 8:40 PM
 *
 */

package eunomia.managers.module;

import eunomia.exception.DependencyFailureException;
import com.vivic.eunomia.module.Dependency;
import com.vivic.eunomia.module.Descriptor;
import eunomia.managers.DatabaseManager;
import eunomia.util.loader.ModuleClassLoader;
import java.io.File;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.log4j.Logger;

/**
 *
 * @author Mikhail Sosonkin
 */
public class ModuleLinker {
    private static Logger logger;
    
    private Map nameTypeToDesc;
    private Map tempMap;
    
    static {
        logger = Logger.getLogger(ModuleLinker.class);
    }
    
    public ModuleLinker() {
        nameTypeToDesc = new HashMap();
        tempMap = new HashMap();
    }
    
    public void loadJDBCDriver(String name, String className, File file) throws MalformedURLException, ClassNotFoundException, InstantiationException, IllegalAccessException, SQLException {
        URLClassLoader loader = new URLClassLoader(new URL[]{file.toURI().toURL()});
        Class klass = loader.loadClass(className);
        Driver driver = (Driver) klass.newInstance();
        DatabaseManager.v().addJDBCDatabase(name, driver);
    }
        
    private void addModuleFile(ModuleFile file) throws MalformedURLException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        String name = file.getName();
        int type = file.getType();

        String desc = file.getDescriptor().shortDescription();
        logger.info("Loaded '" + ModuleFile.types[type] + "' module: " + name + (desc == null?"":" - " + desc));
        
        nameTypeToDesc.put(name + type, file);
    }
    
    public ModuleFile getModuleFile(String name, int type) {
        return (ModuleFile)nameTypeToDesc.get(name + type);
    }
    
    //
    private ModuleFile getTempModuleFile(String name, int type) {
        return (ModuleFile)tempMap.get(name + type);
    }
    
    private void addTempModuleFile(ModuleFile file) {
        tempMap.put(file.getName() + file.getType(), file);
    }
    
    private void removeTempModuleFile(ModuleFile file){
        tempMap.remove(file.getName() + file.getType());
    }
    //
    
    public List getModuleList(int type) {
        List list = new ArrayList();
        Iterator it = nameTypeToDesc.values().iterator();
        while (it.hasNext()) {
            ModuleFile file = (ModuleFile) it.next();
            if(file.getType() == type || type == -1) {
                list.add(file);
            }
        }
        
        return list;
    }
    
    private void getAllDependencies(Dependency[] deps, List depList) throws DependencyFailureException {
        if(deps == null) {
            return;
        }
        
        for (int i = 0; i < deps.length; i++) {
            String name = deps[i].getName();
            int type = deps[i].getType();
            
            ModuleFile desc = getModuleFile(name, type);
            if(desc == null) {
                desc = getTempModuleFile(name, type);
            }
                
            if(desc != null) {
                depList.add(desc);
                getAllDependencies(desc.getDescriptor().getDependencies(), depList);
            } else {
                throw new DependencyFailureException("dependency missing: " + name);
            }
        }
    }
    
    private void releaseModule(ModuleFile file) throws DependencyFailureException, MalformedURLException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        List depList = new ArrayList();
        Dependency[] deps = file.getDescriptor().getDependencies();
        
        getAllDependencies(deps, depList);

        ClassLoader loader = file.getClassLoader();

        if(loader instanceof ModuleClassLoader) {
            ModuleClassLoader mLoader = (ModuleClassLoader)loader;
            Iterator it = depList.iterator();
            while (it.hasNext()) {
                ModuleFile mod = (ModuleFile) it.next();
                
                ClassLoader modLoader = mod.getClassLoader();
                if(modLoader != null) {
                    mLoader.addLoader(modLoader);
                }
            }
        }
        
        addModuleFile(file);
    }
    
    private void scanPending() {
        Iterator it = tempMap.entrySet().iterator();
        while (it.hasNext()) {
            Entry entry = (Entry) it.next();
            ModuleFile desc = (ModuleFile)entry.getValue();
            
            try {
                releaseModule(desc);
                it.remove();
            } catch (Exception ex) {
            }
        }
    }
    
    private void loadLibrary(File file, ModuleClassLoader loader) throws DependencyFailureException, MalformedURLException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        String fname = file.getName();
        int dashInd = fname.lastIndexOf("-");
        int dotInd = fname.lastIndexOf(".");
        
        String vers = fname.substring(dashInd + 1, dotInd);
        String name = fname.substring(0, dashInd);
        
        Descriptor desc = new LibraryDescriptor(name, vers);
        ModuleFile modFile = new ModuleFile(file, loader, desc);
        
        releaseModule(modFile);
        scanPending();
    }
    
    public void loadModule(File jarFile) throws MalformedURLException, DependencyFailureException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        ModuleClassLoader loader = new ModuleClassLoader(new URL[]{jarFile.toURI().toURL()});
        
        Class descClass = null;
        try {
            descClass = loader.loadClass("eunomia.Descriptor");
        } catch (ClassNotFoundException ex){
            loadLibrary(jarFile, loader);
            return;
        }
        
        Object dClass = descClass.newInstance();
        if(dClass instanceof Descriptor){
            Descriptor desc = (Descriptor)dClass;
            ModuleFile modFile = new ModuleFile(jarFile, loader, desc);

            String name = desc.moduleName();
            int type = desc.moduleType();

            if(getModuleFile(name, type) != null){
                logger.error("Module with name \'" + name + "\' already defined. Duplicate not added. (" + jarFile + ")");
                return;
            }

            addTempModuleFile(modFile);
            releaseModule(modFile); // will throw exception if the module is not accepted.
            removeTempModuleFile(modFile);

            scanPending();
        }
    }
}
