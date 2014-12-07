/*
 * ModuleLinker.java
 *
 * Created on March 22, 2006, 8:47 PM
 *
 */

package eunomia.core.managers;

import eunomia.core.managers.event.linker.MissingDependencyEvent;
import eunomia.core.managers.event.linker.ModuleFileAddedEvent;
import eunomia.core.managers.event.linker.ModuleFileRemovedEvent;
import eunomia.core.receptor.ReceptorModuleLoader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.net.MalformedURLException;
import eunomia.core.managers.exception.InvalidModuleFormatException;
import eunomia.core.managers.exception.ManagerException;
import eunomia.core.managers.exception.ModuleDependencyException;
import eunomia.core.managers.exception.ModuleExistsException;
import eunomia.core.receptor.Receptor;
import com.vivic.eunomia.module.Dependency;
import com.vivic.eunomia.module.Descriptor;
import eunomia.config.Config;
import eunomia.core.managers.listeners.ModuleLinkerListener;
import com.vivic.eunomia.sys.util.Util;
import eunomia.util.loader.ModuleClassLoader;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.apache.log4j.Logger;

/**
 *
 * @author Mikhail Sosonkin
 */
public class ModuleLinker {
    private static Logger logger = Logger.getLogger(ModuleLinker.class);
    
    private Map nameTypeToDescriptor;
    private Map depSet;
    private List listeners;
    private File modulesDir;
    private Set unresolvedFlowMod;
    private String subName;
    private Receptor receptor;
    
    private ModuleClassLoader libraryLoader;
    
    private ModuleFileAddedEvent addedEvent;
    private ModuleFileRemovedEvent removedEvent;
    private MissingDependencyEvent depEvent;
    
    public ModuleLinker(String subname, Receptor rec) {
        this.subName = subname;
        receptor = rec;

        libraryLoader = new ReceptorModuleLoader(null, rec);
        
        removedEvent = new ModuleFileRemovedEvent(rec);
        addedEvent = new ModuleFileAddedEvent(rec);
        depEvent = new MissingDependencyEvent(rec);
        
        depSet = new HashMap();
        listeners = new LinkedList();
        nameTypeToDescriptor = new HashMap();
        unresolvedFlowMod = new HashSet();
        
        modulesDir = new File("modules" + File.separator + subname);
        modulesDir.mkdirs();
        
        scanDir(modulesDir);
    }

    public File getModulesDir() {
        return modulesDir;
    }
    
    public void addModuleLinkerListener(ModuleLinkerListener l){
        listeners.add(l);
    }
    
    public void removeModuleLinkerListener(ModuleLinkerListener l){
        listeners.remove(l);
    }
    
    private void fireModuleFileAdded(ModuleFileAddedEvent e){
        Iterator it = listeners.iterator();
        while (it.hasNext()) {
            ModuleLinkerListener l = (ModuleLinkerListener) it.next();
            l.moduleFileAdded(e);
        }
    }
    
    private void fireMissingDependency(MissingDependencyEvent e){
        Iterator it = listeners.iterator();
        while (it.hasNext()) {
            ModuleLinkerListener l = (ModuleLinkerListener) it.next();
            l.missingDependency(e);
        }
    }
    
    private void fireModuleFileRemoved(ModuleFileRemovedEvent e){
        Iterator it = listeners.iterator();
        while (it.hasNext()) {
            ModuleLinkerListener l = (ModuleLinkerListener) it.next();
            l.moduleFileRemoved(e);
        }
    }

    private void scanPending() {
        Iterator it = depSet.entrySet().iterator();
        while (it.hasNext()) {
            Entry entry = (Entry) it.next();
            ModuleDescriptor desc = (ModuleDescriptor)entry.getValue();
            
            try {
                loadDependencies(desc);
                it.remove();
                setMapping(desc);
            } catch (ModuleDependencyException ex) {
            }
        }
    }
    
    private void loadDependencies(ModuleDescriptor desc) throws ModuleDependencyException {
        Dependency[] deps = desc.getDescriptor().getDependencies();
        ClassLoader loader = desc.getClassLoader();

        if(loader != null && loader instanceof ModuleClassLoader) {
            ModuleClassLoader mLoader = (ModuleClassLoader)loader;
            List depsList = new ArrayList();
            
            getAllDependencies(deps, depsList);
            
            Iterator it = depsList.iterator();
            while (it.hasNext()) {
                ModuleDescriptor descriptor = (ModuleDescriptor) it.next();
                
                ClassLoader modLoader = descriptor.getClassLoader();
                if(modLoader != null) {
                    mLoader.addLoader(modLoader);
                }
            }
        }
    }
    
    // walk the whole dependecy tree.
    private void getAllDependencies(Dependency[] deps, List depList) throws ModuleDependencyException {
        if(deps == null) {
            return;
        }
        
        boolean missing = false;
        
        for (int i = 0; i < deps.length; i++) {
            String name = deps[i].getName();
            int type = deps[i].getType();
            
            ModuleDescriptor desc = getMapping(name, type);
            if(desc == null) {
                desc = getDepMapping(name, type);
            }
            
            if(desc != null) {
                depList.add(desc);
                try {
                    getAllDependencies(desc.getDescriptor().getDependencies(), depList);
                } catch (ModuleDependencyException e) {
                    missing = true;
                }
            } else {
                depEvent.setDependency(deps[i]);
                fireMissingDependency(depEvent);
                missing = true;
            }
        }

        if(missing) {
            throw new ModuleDependencyException("Missing dependencies");
        }
    }
    
    public void loadLibrary(File jarFile, LibraryDescriptor desc) throws MalformedURLException {
        libraryLoader.addPath(jarFile);
        ModuleDescriptor md = new ModuleDescriptor(jarFile.toString(), libraryLoader, desc);
        setMapping(md);
        
        logger.info("Loaded " + ModuleDescriptor.types[desc.moduleType()] + " module: " + desc.moduleName() + " - " + desc.shortDescription());
        
        scanPending();
    }
    
    public void loadModule(File jarFile) throws MalformedURLException, InvalidModuleFormatException {
        jarFile = jarFile.getAbsoluteFile();
        
        ModuleClassLoader loader = new ReceptorModuleLoader(jarFile.toURI().toURL(), receptor);
        
        loader.addLoader(Thread.currentThread().getContextClassLoader());
        logger.info("Loading module file: " + jarFile);
        
        try {
            Class descClass = loader.loadClass("eunomia.Descriptor");
            Object dClass = descClass.newInstance();
            if(dClass instanceof Descriptor){
                Descriptor desc = (Descriptor)dClass;
                String name = desc.moduleName();
                ModuleDescriptor descriptor = new ModuleDescriptor(jarFile.toString(), loader, desc);
                
                if(getMapping(name, desc.moduleType()) != null){
                    throw new ModuleExistsException("Module with name \'" + name + "\' already defined. Diplicate not added. (" + jarFile + ")");
                }
                
                setDepMapping(descriptor);
                loadDependencies(descriptor); // will throw exception if not all deps are found.
                delDepMapping(descriptor);

                setMapping(descriptor);
                logger.info("Loaded " + ModuleDescriptor.types[desc.moduleType()] + " module: " + desc.moduleName() + " - " + desc.shortDescription());
                
                scanPending();
            }
        } catch (ModuleDependencyException ex) {
            //ex.printStackTrace();
        } catch (Throwable ex) {
            ex.printStackTrace();
            throw new InvalidModuleFormatException("Invalid module format: " + jarFile + " (" + ex.getMessage() + ")");
        }
    }

    private void setMapping(ModuleDescriptor md){
        nameTypeToDescriptor.put(md.getName() + md.getType(), md);
        if(md.getType() == Descriptor.TYPE_LIBB) {
            Config conf = Config.getConfiguration("managers.ModuleLinker");
            File file = new File(md.getPath());
            conf.setString(file.getName() + ".name", md.getName());
            conf.setInt(file.getName() + ".vers", md.getDescriptor().version());
            conf.save();
        }
        
        addedEvent.setModuleDescriptor(md);
        fireModuleFileAdded(addedEvent);
    }
    
    private void removeMapping(ModuleDescriptor md){
        if(nameTypeToDescriptor.remove(md.getName() + md.getType()) != null) {
            Config conf = Config.getConfiguration("managers.ModuleLinker");
            File file = new File(md.getPath());
            conf.deleteField(file.getName() + ".name");
            conf.deleteField(file.getName() + ".vers");
            conf.save();

            removedEvent.setModuleDescriptor(md);
            fireModuleFileRemoved(removedEvent);
        }
    }
    
    public ModuleDescriptor getMapping(String name, int type){
        return (ModuleDescriptor)nameTypeToDescriptor.get(name + type);
    }
    
    // Only for modules w/ broken dependencies
    private void setDepMapping(ModuleDescriptor md){
        depSet.put(md.getName() + md.getType(), md);
    }
    
    private void delDepMapping(ModuleDescriptor md){
        depSet.remove(md.getName() + md.getType());
    }

    private ModuleDescriptor getDepMapping(String name, int type){
        return (ModuleDescriptor)depSet.get(name + type);
    }
    //
    
    public void deleteModule(ModuleDescriptor md) {
        removeMapping(md);
        File file = new File(md.getPath());
        file.deleteOnExit();
        addDelete(md.getPath());
    }
    
    public List getDescriptors(){
        return Arrays.asList(nameTypeToDescriptor.values().toArray());
    }
    
    private void addDelete(String name) {
        try {
            FileOutputStream fout = new FileOutputStream(modulesDir + File.separator + ".clean.nab~", true);
            fout.write((name + "\n").getBytes());
            fout.close();
        } catch(FileNotFoundException ex) {
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    private void scanDir(File file){
        try {
            File delFile = new File(file.toString() + File.separator + ".clean.nab~");
            String todel = new String(Util.catFile(delFile.toURI()));
            delFile.delete();
            String[] files = todel.split("\n");
            for (int i = 0; i < files.length; i++) {
                if(!new File(files[i]).delete()){
                    addDelete(files[i]);
                }
            }
        } catch(FileNotFoundException ex) {
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        
        if(file.isDirectory()){
            File[] jars = file.listFiles();
            Config conf = Config.getConfiguration("managers.ModuleLinker");
            
            for (int i = 0; i < jars.length; i++) {
                String jarName = jars[i].getName();
                if(jarName.toLowerCase().endsWith(".jar")){
                    try {
                        String libName = conf.getString(jarName + ".name", null);
                        int libVers = conf.getInt(jarName + ".vers", 0);
                        
                        if(libName == null) {
                            loadModule(jars[i]);
                        } else {
                            loadLibrary(jars[i], new LibraryDescriptor(libName, libVers));
                        }
                    } catch (MalformedURLException ex) {
                        ex.printStackTrace();
                    } catch(InvalidModuleFormatException ex) {
                        addDelete(jars[i].toString());
                    }
                }
            }
        }
    }
}