/*
 * ModuleManager.java
 *
 * Created on October 23, 2005, 5:30 PM
 *
 */

package eunomia.managers;

import eunomia.managers.connectable.ConnectTuple;
import eunomia.util.loader.ModuleClassLoader;
import eunomia.modules.FlowProcessorModule;
import eunomia.messages.receptor.*;
import eunomia.modules.AnalysisModule;
import eunomia.modules.FlowProducerModule;
import eunomia.plugin.interfaces.*;
import eunomia.receptor.module.interfaces.FlowModule;
import eunomia.util.number.ModInteger;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.*;
import java.util.*;
import org.apache.log4j.Logger;

/**
 *
 * @author Mikhail Sosonkin.
 */
public class ModuleManager {
    private static ModuleManager ins;
    private static Logger logger;
    private static ModInteger mInt;
    private static ModuleHandle retriever;
    
    private HashMap handleToInstance;
    private HashMap nameToLoader;
    
    private HashMap nameToFInst;
    private HashMap fInstToName;

    private HashMap analModToLoader;
    private HashMap handleToAnalInst;
    
    private int modID;
    private Object idLock;
    
    static {
        mInt = new ModInteger();
        retriever = new ModuleHandle();
    }
    
    public ModuleManager() {
        handleToInstance = new HashMap();
        nameToLoader = new HashMap();
        nameToFInst = new HashMap();
        fInstToName = new HashMap();
        analModToLoader = new HashMap();
        handleToAnalInst = new HashMap();
        modID = 0;
        idLock = new Object();
    }
    
    private int getNextID(){
        synchronized(idLock){
            return modID++;
        }
    }
    
    public AnalysisModule startStaticAnalysisModule(String name) throws IllegalAccessException, ClassNotFoundException, InstantiationException{
        if(!analModToLoader.containsKey(name)){
            throw new IllegalAccessException("Analysis Module not defined: " + name);
        }
        
        ClassLoader loader = (ClassLoader)analModToLoader.get(name);
        if(loader == null){
            loader = ClassLoader.getSystemClassLoader();
        }
        String className = "eunomia.module.data.rec." + name + ".Main";
        Class klass = loader.loadClass(className);
        StaticAnalysisModule mod = (StaticAnalysisModule)klass.newInstance();
        ModuleHandle handle = new ModuleHandle();
        
        handle.setInstanceID(getNextID());
        handle.setModuleName(name);
        handle.setReadOnly();
        
        AnalysisModule wrap = new AnalysisModule(mod, handle, name);
        
        handleToAnalInst.put(handle, wrap);
        
        return wrap;
    }
    
    public ModuleHandle startModule(String moduleName) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        if(!nameToLoader.containsKey(moduleName)){
            throw new IllegalAccessException("Module not defined: " + moduleName);
        }
        
        ClassLoader loader = (ClassLoader)nameToLoader.get(moduleName);
        if(loader == null){
            loader = ClassLoader.getSystemClassLoader();
        }
        String className = "eunomia.plugin.rec." + moduleName + ".Main";
        Class klass = loader.loadClass(className);
        ReceptorModule mod = (ReceptorModule)klass.newInstance();
        
        ModuleHandle handle = new ModuleHandle();
        handle.setModuleName(moduleName);
        handle.setInstanceID(getNextID());
        handle.setReadOnly();
        
        FlowProcessorModule wrap = new FlowProcessorModule(mod, handle);
        handleToInstance.put(handle, wrap);
        
        //ReceptorManager.v().addFlowProcessor(wrap.getConnectTuple());
        
        return handle;
    }
    
    public void addDefaultConnect(ModuleHandle handle) {
        FlowProcessorModule mod = (FlowProcessorModule)getModule(handle);
        ReceptorManager.v().addDefaultConnect(mod.getConnectTuple());
    }
    
    public void terminateModule(ModuleHandle handle){
        FlowProcessorModule recMod = (FlowProcessorModule)getModule(handle);
        recMod.destroy();
        ReceptorManager.v().removeFlowProcessor(recMod.getConnectTuple());
        handleToInstance.remove(handle);
    }
    
    public String[] getModuleFlowServerList(ReceptorModule mod){
        return ((FlowProcessorModule)mod).getFlowServerList();
    }

    public ConnectTuple getFlowProcessorConnectTuple(ModuleHandle handle) {
        return ((FlowProcessorModule)getModule(handle)).getConnectTuple();
    }
    
    public void addFlowModule(String name, File jarFile) throws MalformedURLException, ClassNotFoundException, InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        URLClassLoader cll = (URLClassLoader)ClassLoader.getSystemClassLoader();
        Class klass = URLClassLoader.class;
        Method method;
        
        method = klass.getDeclaredMethod("addURL", URL.class);
        // bad hack, must change at some point later. need a custom class loader.
        method.setAccessible(true);
        method.invoke(cll, jarFile.toURI().toURL());
        method.setAccessible(false);
        
        FlowModule mod = new FlowProducerModule((FlowModule)cll.loadClass("eunomia.receptor.module." + name + ".Main").newInstance());
        nameToFInst.put(name, mod);
        fInstToName.put(mod, name);
    }
    
    public FlowModule getFlowModuleInstance(String name){
        return (FlowModule)nameToFInst.get(name);
    }
    
    public String getFlowModuleName(FlowModule mod){
        return (String)fInstToName.get(mod);
    }
    
    public void addModule(File jarFile) throws MalformedURLException {
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
                if(nameToLoader.containsKey(name)){
                    logger.error("Module with name \'" + name + "\' already defined. Diplicate not added. (" + jarFile + ")");
                    return;
                }
                if(desc.moduleType() == Descriptor.TYPE_PROC){
                    nameToLoader.put(name, loader);
                    logger.info("Loaded Processing module: " + name + " - " + desc.shortDescription());
                } else if(desc.moduleType() == Descriptor.TYPE_FLOW){
                    addFlowModule(name, jarFile);
                    logger.info("Loaded Flow module: " + name + " - " + desc.shortDescription());
                } else if(desc.moduleType() == Descriptor.TYPE_ANLZ){
                    analModToLoader.put(name, loader);
                    logger.info("Loaded Analysis module: " + name + " - " + desc.shortDescription());
                } else if(desc.moduleType() == Descriptor.TYPE_COLL){
                    logger.info("Loaded Collection module: " + name + " - " + desc.shortDescription());
                }
            }
        } catch (Exception ex) {
            logger.error("Invalid module: " + jarFile);
        }
    }
    
    public void removeModule(String name){
        nameToLoader.remove(name);
    }
    
    public List getModuleList(){
        return Arrays.asList(nameToLoader.keySet().toArray());
    }
    
    public List getFlowModuleNamesList(){
        return Arrays.asList(fInstToName.values().toArray());
    }
    
    public List getAnalysisModuleNamesList(){
        return Arrays.asList(analModToLoader.keySet().toArray());
    }

    public List getHandles(){
        return Arrays.asList(handleToInstance.keySet().toArray());
    }
    
    public ReceptorModule getModule(int id){
        retriever.setInstanceID(id);
        return (ReceptorModule)handleToInstance.get(retriever);
    }
    
    public ReceptorModule getModule(ModuleHandle handle){
        return getModule(handle.getInstanceID());
    }
    
    public AnalysisModule getAnalysisModule(ModuleHandle handle){
        return (AnalysisModule)handleToAnalInst.get(handle);
    }
    
    public static ModuleManager v(){
        if(ins == null){
            logger = Logger.getLogger(ModuleManager.class);
            ins = new ModuleManager();
        }
        
        return ins;
    }
}