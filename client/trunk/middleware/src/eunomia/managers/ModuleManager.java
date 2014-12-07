/*
 * ModuleManager.java
 *
 * Created on October 23, 2005, 5:30 PM
 *
 */

package eunomia.managers;

import com.vivic.eunomia.module.Descriptor;
import com.vivic.eunomia.module.EunomiaModule;
import com.vivic.eunomia.sys.receptor.SieveContext;
import eunomia.comm.ReceptorClassLocator;
import eunomia.exception.ManagerException;
import eunomia.exception.ModuleTerminationFailureException;
import eunomia.managers.connectable.ConnectTuple;
import eunomia.managers.module.ModuleFile;
import eunomia.managers.module.ModuleLinker;
import eunomia.messages.receptor.ModuleHandle;
import eunomia.module.FlowProducerModule;
import eunomia.module.ProcMiddlewareModule;
import eunomia.module.AnlzMiddlewareModule;
import eunomia.module.MiddlewareModule;
import eunomia.plugin.interfaces.CollectionModule;
import com.vivic.eunomia.module.receptor.ReceptorProcessorModule;
import com.vivic.eunomia.module.receptor.ReceptorAnalysisModule;
import com.vivic.eunomia.module.flow.FlowModule;
import com.vivic.eunomia.sys.receptor.SieveModuleManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import org.apache.log4j.Logger;

/**
 *
 * @author Mikhail Sosonkin.
 */
public class ModuleManager implements SieveModuleManager {
    private static final String[] MOD_PACK = new String[] {
        "eunomia.module.receptor.proc.", "eunomia.module.receptor.flow.", "eunomia.module.receptor.anlz.", "eunomia.module.receptor.coll."
    };

    @Deprecated
    private static final String[] MOD_PACK_DEP = new String[] {
        "eunomia.plugin.rec.", "eunomia.receptor.module.", "eunomia.module.data.rec.", "eunomia.module.receptor.coll."
    };
    
    private static ModuleManager ins;
    private static Logger logger;
    
    private HashMap nameToFInst;
    private HashMap fInstToName;
    
    private AtomicInteger modID;
    private ModuleLinker linker;
    private Map handleToInst;
    
    public ModuleManager() {
        nameToFInst = new HashMap();
        fInstToName = new HashMap();
        handleToInst = new HashMap();
        modID = new AtomicInteger(0);
        linker = new ModuleLinker();
    }
    
    public void shutdown() {
        logger.info("Terminating all module instances");
        Object[] handle = handleToInst.keySet().toArray();
        for(int i = 0; i < handle.length; i++) {
            try {
                ModuleHandle h = (ModuleHandle)handle[i];
                terminateModule(h);
            } catch(ManagerException ex) {
                ex.printStackTrace();
            }
        }
    }
    
    public void startFlowModules() {
        Iterator it = linker.getModuleList(Descriptor.TYPE_FLOW).iterator();
        while (it.hasNext()) {
            ModuleFile file = (ModuleFile)it.next();
            try {
                ModuleManager.v().startModule_FLOW(file.getName());
            } catch (Exception ex) {
                logger.error("Unable to start flow module: " + file.getName());
            }
        }
    }
    
    private int getNextID(){
        return modID.getAndIncrement();
    }
    
    private void addModuleInstance(ModuleHandle handle, Object module) {
        handleToInst.put(handle, module);
    }
    
    public EunomiaModule getModule(ModuleHandle handle) {
        return (EunomiaModule)handleToInst.get(handle);
    }
    
    private MiddlewareModule getModuleInstance(ModuleHandle handle) {
        return (MiddlewareModule)handleToInst.get(handle);
    }
    
    private void removeModuleInstance(ModuleHandle handle) {
        handleToInst.remove(handle);
    }
    
    private EunomiaModule startModuleClass(String name, int type) throws IllegalAccessException, ClassNotFoundException, InstantiationException {
        ModuleFile file = linker.getModuleFile(name, type);
        if(file == null){
            throw new IllegalAccessException(ModuleFile.types[type] + " Module not defined: " + name);
        }

        String className;
        Class klass;
        
        className = MOD_PACK[type] + name + ".Main";
        try {
            klass = file.getClassLoader().loadClass(className);
        } catch (ClassNotFoundException ex) {
            className = MOD_PACK_DEP[type] + name + ".Main";
            klass = file.getClassLoader().loadClass(className);
        }
        
        EunomiaModule mod = (EunomiaModule)klass.newInstance();
        
        return mod;
    }
    
    public FlowModule startModule_FLOW(String name) throws IllegalAccessException, ClassNotFoundException, InstantiationException {
        if(getFlowModuleInstance(name) != null) {
            throw new IllegalAccessException("Flow Module '" + name + "' already instantiated");
        }
        
        EunomiaModule mod = startModuleClass(name, ModuleHandle.TYPE_FLOW);
        FlowModule wrap = new FlowProducerModule((FlowModule)mod);
        
        Class[] fClasses = wrap.getFilterMessageClassList();
        for (int i = 0; i < fClasses.length; i++) {
            ReceptorClassLocator.addClass(fClasses[i]);
        }
        
        nameToFInst.put(name, wrap);
        fInstToName.put(wrap, name);
        
        return wrap;
    }
    
    public ModuleHandle startModule_ANLZ(String name) throws IllegalAccessException, ClassNotFoundException, InstantiationException {
        ReceptorAnalysisModule mod = (ReceptorAnalysisModule)startModuleClass(name, ModuleHandle.TYPE_ANLZ);
        
        ModuleHandle handle = new ModuleHandle();
        
        handle.setInstanceID(getNextID());
        handle.setModuleName(name);
        handle.setModuleType(ModuleHandle.TYPE_ANLZ);
        handle.setReadOnly();
        
        AnlzMiddlewareModule wrap = new AnlzMiddlewareModule(handle, mod);
        addModuleInstance(handle, wrap);
        
        wrap.getThread().start();
        
        return handle;
    }
    
    public CollectionModule startModule_COLL(String name) throws IllegalAccessException, ClassNotFoundException, InstantiationException{
        CollectionModule mod = (CollectionModule)startModuleClass(name, ModuleHandle.TYPE_COLL);
        
        return mod;
    }
    
    public ModuleHandle startModule_PROC(String name) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        ReceptorProcessorModule mod = (ReceptorProcessorModule)startModuleClass(name, ModuleHandle.TYPE_PROC);
        
        ModuleHandle handle = new ModuleHandle();
        handle.setModuleName(name);
        handle.setInstanceID(getNextID());
        handle.setModuleType(ModuleHandle.TYPE_PROC);
        handle.setReadOnly();
        
        ProcMiddlewareModule wrap = new ProcMiddlewareModule(handle, mod);
        addModuleInstance(handle, wrap);
        
        return handle;
    }
    
    public void addDefaultConnect(ModuleHandle handle) {
        ProcMiddlewareModule mod = (ProcMiddlewareModule)getModuleInstance(handle);
        ReceptorManager.v().addDefaultConnect(mod.getConnectTuple());
    }
    
    public void terminateModule(ModuleHandle handle) throws ManagerException {
        if(handle.getModuleType() == ModuleHandle.TYPE_PROC) {
            ProcMiddlewareModule recMod = (ProcMiddlewareModule)getModuleInstance(handle);
            ReceptorManager.v().removeFlowProcessor(recMod.getConnectTuple());
            recMod.destroy();
        } else if(handle.getModuleType() == ModuleHandle.TYPE_ANLZ) {
            AnlzMiddlewareModule anl = (AnlzMiddlewareModule)getModuleInstance(handle);
            anl.destroy();
        }

        removeModuleInstance(handle);
    }
    
    public String[] getModuleFlowServerList(ReceptorProcessorModule mod){
        return ((ProcMiddlewareModule)mod).getFlowServerList();
    }

    public ConnectTuple getFlowProcessorConnectTuple(ModuleHandle handle) {
        return ((ProcMiddlewareModule)getProcessorModule(handle)).getConnectTuple();
    }
    
    public FlowModule getFlowModuleInstance(String name){
        return (FlowModule)nameToFInst.get(name);
    }
    
    public String getFlowModuleName(FlowModule mod){
        return (String)fInstToName.get(mod);
    }
    
    public String[] getModuleNames(int type) {
        List files = linker.getModuleList(type);
        String[] list = new String[files.size()];
        for (int i = 0; i < list.length; i++) {
            list[i] = ((ModuleFile)files.get(i)).getName();
        }
        
        return list;
    }

    public ProcMiddlewareModule getProcessorModule(ModuleHandle handle){
        return (ProcMiddlewareModule)getModuleInstance(handle);
    }
    
    public AnlzMiddlewareModule getAnalysisModule(ModuleHandle handle){
        return (AnlzMiddlewareModule)getModuleInstance(handle);
    }
    
    public List getModuleHandleList(String name, int type) {
        List list = new ArrayList();
        
        Iterator it = handleToInst.keySet().iterator();
        while (it.hasNext()) {
            ModuleHandle h = (ModuleHandle) it.next();
            if( (name == null || name.equals(h.getModuleName())) && (type == -1 || h.getModuleType() == type) ) {
                list.add(h);
            }
        }
        
        return list;
    }
    
    public EunomiaModule getInstanceEnsure(String name, int type) throws ManagerException, IllegalAccessException, ClassNotFoundException, InstantiationException {
        if(name == null || type == -1) {
            throw new ManagerException("Requires both type and name");
        }
        
        List list = getModuleHandleList(name, type);
        if(list.size() > 0) {
            return (EunomiaModule)getModuleInstance((ModuleHandle)list.get(0));
        }
        
        ModuleHandle handle = null;
        if(type == ModuleHandle.TYPE_PROC) {
            handle = startModule_PROC(name);
        } else if(type == ModuleHandle.TYPE_ANLZ) {
            handle = startModule_ANLZ(name);
        }
        
        return (EunomiaModule)getModuleInstance(handle);
    }

    public ModuleLinker getLinker() {
        return linker;
    }
    
    public EunomiaModule unwrap(EunomiaModule mod) {
        return ((MiddlewareModule)mod).getModule();
    }

    public static ModuleManager v(){
        if(ins == null){
            logger = Logger.getLogger(ModuleManager.class);
            ins = new ModuleManager();
        }
        
        return ins;
    }
}