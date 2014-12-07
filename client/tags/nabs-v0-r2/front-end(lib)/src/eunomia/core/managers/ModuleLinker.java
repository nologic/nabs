/*
 * ModuleLinker.java
 *
 * Created on March 22, 2006, 8:47 PM
 *
 */

package eunomia.core.managers;

import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.util.*;
import eunomia.config.*;
import eunomia.core.managers.exception.InvalidModuleFormatException;
import eunomia.core.managers.exception.ManagerException;
import eunomia.core.managers.exception.ModuleExistsException;
import eunomia.core.managers.listeners.*;
import eunomia.plugin.interfaces.Descriptor;
import eunomia.util.loader.ModuleClassLoader;
import java.io.*;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import org.apache.log4j.Logger;

/**
 *
 * @author Mikhail Sosonkin
 */
public class ModuleLinker implements ModuleLinkerListener {
    private static Logger logger = Logger.getLogger(ModuleLinker.class);
    private static final ModuleLinker ins = new ModuleLinker();
    
    private HashMap nameTypeToDescriptor;
    private List listeners;
    
    private ModuleLinker() {
        listeners = new LinkedList();
        nameTypeToDescriptor = new HashMap();
        load();
        scanDir("modules");
        
        this.addModuleLinkerListener(this);
    }
    
    public void addModuleLinkerListener(ModuleLinkerListener l){
        listeners.add(l);
    }
    
    public void removeModuleLinkerListener(ModuleLinkerListener l){
        listeners.remove(l);
    }
    
    public void fireListChanged(){
        Iterator it = listeners.iterator();
        while (it.hasNext()) {
            ModuleLinkerListener l = (ModuleLinkerListener) it.next();
            l.listChanged();
        }
    }
    
    public ModuleDescriptor loadModule(String file) throws MalformedURLException, ManagerException {
        File jarFile = new File(file);
        ModuleClassLoader loader = null;
        if(jarFile != null) {
            loader = new ModuleClassLoader(new URL[]{jarFile.toURI().toURL()});
        }
        Class descClass;
        try {
            descClass = loader.loadClass("eunomia.Descriptor");
            Object dClass = descClass.newInstance();
            if(dClass instanceof Descriptor){
                Descriptor desc = (Descriptor)dClass;
                String name = desc.moduleName();
                if(getMapping(name, desc.moduleType()) != null){
                    throw new ModuleExistsException("Module with name \'" + name + "\' already defined. Diplicate not added. (" + jarFile + ")");
                }
                if(desc.moduleType() == Descriptor.TYPE_PROC){
                    return loadProcessorModule(desc, loader, file);
                } else if(desc.moduleType() == Descriptor.TYPE_FLOW){
                    return loadFlowModule(desc, file);
                } else if(desc.moduleType() == Descriptor.TYPE_ANLZ){
                    return loadAnalysisModule(desc, loader, file);
                }
            }
        } catch (NoSuchMethodException ex) {
            ex.printStackTrace();
            throw new InvalidModuleFormatException("Invalid module format: " + file);
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
            throw new InvalidModuleFormatException("Invalid module format: " + file);
        } catch (IllegalAccessException ex) {
            ex.printStackTrace();
            throw new InvalidModuleFormatException("Invalid module format: " + file);
        } catch (InstantiationException ex) {
            ex.printStackTrace();
            throw new InvalidModuleFormatException("Invalid module format: " + file);
        } catch (MalformedURLException ex) {
            ex.printStackTrace();
            throw new InvalidModuleFormatException("Invalid module format: " + file);
        } catch (InvocationTargetException ex) {
            ex.printStackTrace();
            throw new InvalidModuleFormatException("Invalid module format: " + file);
        }
        
        return null;
    }
    
    private ModuleDescriptor loadProcessorModule(Descriptor desc, ModuleClassLoader loader, String file){
        ModuleDescriptor descriptor = new ModuleDescriptor(file, loader, desc);
        
        setMapping(descriptor);
        
        logger.info("Loaded processing module: " + desc.moduleName() + " - " + desc.shortDescription());
        return descriptor;
    }
    
    private ModuleDescriptor loadAnalysisModule(Descriptor desc, ModuleClassLoader loader, String file){
        ModuleDescriptor descriptor = new ModuleDescriptor(file, loader, desc);
        
        setMapping(descriptor);
        
        logger.info("Loaded analysis module: " + desc.moduleName() + " - " + desc.shortDescription());
        return descriptor;
    }

    private ModuleDescriptor loadFlowModule(Descriptor desc, String file) throws NoSuchMethodException, MalformedURLException, IllegalAccessException, InvocationTargetException, ClassNotFoundException, InstantiationException {
        URLClassLoader cll = (URLClassLoader)ClassLoader.getSystemClassLoader();
        Class klass = URLClassLoader.class;
        Method method = klass.getDeclaredMethod("addURL", URL.class);
        method.setAccessible(true);
        method.invoke(cll, new File(file).toURI().toURL());
        method.setAccessible(false);
        
        ModuleDescriptor descriptor = new ModuleDescriptor(file, cll, desc);
        setMapping(descriptor);
        FlowModuleManager.ins.addModule(descriptor.getName());
        
        logger.info("Loaded flow module: " + desc.moduleName() + " - " + desc.shortDescription());
        return descriptor;
    }
    
    private void setMapping(ModuleDescriptor md){
        nameTypeToDescriptor.put(md.getName() + md.getType(), md);
        fireListChanged();
    }
    
    public ModuleDescriptor getMapping(String name, int type){
        return (ModuleDescriptor)nameTypeToDescriptor.get(name + type);
    }
    
    public ModuleDescriptor getProcMapping(String name){
        return getMapping(name, Descriptor.TYPE_PROC);
    }
    
    public ModuleDescriptor getAnlzMapping(String name){
        return getMapping(name, Descriptor.TYPE_ANLZ);
    }
    
    public void removeMapping(ModuleDescriptor md){
        nameTypeToDescriptor.remove(md.getName() + md.getType());
        fireListChanged();
    }
    
    public List getDescriptors(){
        return Arrays.asList(nameTypeToDescriptor.values().toArray());
    }
    
    public void save() throws IOException {
        Config cfg = Config.getConfiguration("module.paths");
        List names = getDescriptors();
        String[] paths = new String[names.size()];
        
        Iterator it = names.iterator();
        int i = 0;
        while (it.hasNext()) {
            ModuleDescriptor desc = (ModuleDescriptor) it.next();
            paths[i] = desc.getPath();
            ++i;
        }
        cfg.setArray("paths", paths);
        cfg.save();
    }
    
    public void load(){
        Config cfg = Config.getConfiguration("module.paths");
        Object[] paths = cfg.getArray("paths", null);
        
        if(paths != null){
            for (int i = 0; i < paths.length; i++) {
                String path = (String)paths[i];
                try {
                    loadModule(path);
                } catch (Exception ex) {
                    logger.error(ex.getMessage());
                    ex.printStackTrace();
                }
            }
        }
    }
    
    private void scanDir(String dir){
        File file = new File(dir);
        if(file.isDirectory()){
            File[] jars = file.listFiles();
            for (int i = 0; i < jars.length; i++) {
                if(jars[i].getName().toLowerCase().endsWith(".jar")){
                    try {
                        loadModule(jars[i].getAbsolutePath());
                        logger.info("Found new module: " + jars[i]);
                    } catch (ManagerException ex){
                    } catch (MalformedURLException ex) {
                        ex.printStackTrace();
                    }
                }
            }
            try {
                save();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
    
    public static ModuleLinker v(){
        return ins;
    }
    
    public void listChanged() {
        try {
            save();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}