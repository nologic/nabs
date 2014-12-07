/*
 * ModuleManager.java
 *
 * Created on October 23, 2005, 4:54 PM
 *
 */

package eunomia.core.managers;

import com.vivic.eunomia.module.Descriptor;
import eunomia.core.data.staticData.DatabaseReportListener;
import eunomia.core.managers.event.linker.MissingDependencyEvent;
import eunomia.core.managers.event.linker.ModuleFileAddedEvent;
import eunomia.core.managers.event.linker.ModuleFileRemovedEvent;
import eunomia.core.managers.event.state.module.ModuleAddedEvent;
import eunomia.core.managers.event.state.module.ModuleListChangedEvent;
import eunomia.core.managers.event.state.module.ModuleRemovedEvent;
import eunomia.core.managers.exception.NoModuleJarException;
import eunomia.core.managers.interfaces.ModuleFilterEditor;
import eunomia.core.managers.listeners.ModuleLinkerListener;
import eunomia.core.managers.listeners.ModuleManagerListener;
import eunomia.core.receptor.comm.ReceptorOutComm;
import eunomia.messages.receptor.ModuleHandle;
import eunomia.module.ProcFrontendModule;
import com.vivic.eunomia.module.frontend.GUIModule;
import eunomia.flow.Filter;
import eunomia.messages.receptor.ncm.AnalysisSummaryMessage;
import eunomia.module.AnlzFrontendModule;
import eunomia.module.FrontendModule;
import eunomia.module.comm.InterModuleOutComm;
import com.vivic.eunomia.module.EunomiaModule;
import com.vivic.eunomia.module.receptor.FlowModule;
import com.vivic.eunomia.sys.frontend.ConsoleModuleManager;
import eunomia.config.ConfigChangeListener;
import eunomia.config.Settings;
import eunomia.core.receptor.Receptor;
import eunomia.util.Util;
import java.io.DataOutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.apache.log4j.Logger;

/**
 *
 * @author Mikhail Sosonkin
 */
public class ModuleManager implements Runnable, ConfigChangeListener, ModuleLinkerListener, ConsoleModuleManager {
    private static String[] classPrefix = new String[] {
        "eunomia.plugin.gui.", // PROC
        "eunomia.receptor.module.", //FLOW
        "eunomia.module.data.gui." // ANLZ
    };
    
    private static Constructor[] wrapperClass = new Constructor[4];
    
    private List listeners;
    private List dbReportListeners;
    private ReceptorOutComm recOut;
    private HashMap hashToModule;
    private HashMap modToHandle;
    private HashMap handleToAnalMod;
    private HashMap nameToFInst;
    private List handles;
    private Receptor receptor;
    private Set checkingSet;
    private Set downloadSet;
    private int refresh;
    private List phantomModules;
    private ModuleFilterEditor filterEditor;
    private Thread refThread;
    
    //events
    private ModuleListChangedEvent mlce;
    private ModuleAddedEvent mae;
    private ModuleRemovedEvent mre;
    
    private static Logger logger;
    
    static {
        try {
            wrapperClass[ModuleHandle.TYPE_PROC] = ProcFrontendModule.class.getConstructor(ModuleHandle.class, EunomiaModule.class, Receptor.class);
            wrapperClass[ModuleHandle.TYPE_ANLZ] = AnlzFrontendModule.class.getConstructor(ModuleHandle.class, EunomiaModule.class, Receptor.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        logger = Logger.getLogger(ModuleManager.class);
    }
    
    public ModuleManager(Receptor rec) {
        mlce = new ModuleListChangedEvent(rec);
        mae = new ModuleAddedEvent(rec);
        mre = new ModuleRemovedEvent(rec);
        
        recOut = rec.getOutComm();
        dbReportListeners = new LinkedList();
        listeners = new LinkedList();
        receptor = rec;
        modToHandle = new HashMap();
        hashToModule = new HashMap();
        handleToAnalMod = new HashMap();
        handles = new LinkedList();
        nameToFInst = new HashMap();
        
        downloadSet = Collections.synchronizedSet(new HashSet());
        checkingSet = Collections.synchronizedSet(new HashSet());
        phantomModules = Collections.synchronizedList(new LinkedList());
        
        ModuleLinker linker = receptor.getLinker();
        Iterator it = linker.getDescriptors().iterator();
        while (it.hasNext()) {
            ModuleDescriptor desc = (ModuleDescriptor) it.next();
            if(desc.getType() == Descriptor.TYPE_FLOW) {
                loadFlowModule(desc);
            }
        }
        
        refresh = Settings.v().getRefreshInterval();
        Settings.v().addConfigChangeListener(this);
        receptor.getLinker().addModuleLinkerListener(this);
        
        (refThread = new Thread(this, "Module Manager")).start();
    }
    
    public int getSieveModuleInstanceCount() {
        return phantomModules.size() + handles.size();
    }
    
    public void setDatabaseReport(AnalysisSummaryMessage msg){
        fireDatabaseReportListener(msg);
    }
    
    public void addDatabaseReportListener(DatabaseReportListener l) {
        dbReportListeners.add(l);
    }
    
    public void removeDatabaseReportListener(DatabaseReportListener l) {
        dbReportListeners.remove(l);
    }
    
    private void fireDatabaseReportListener(AnalysisSummaryMessage msg){
        Iterator it = dbReportListeners.iterator();
        while (it.hasNext()) {
            DatabaseReportListener l = (DatabaseReportListener) it.next();
            l.setAnalysisSummaryReport(msg);
        }
    }
    
    public void reset(){
        hashToModule.clear();
        modToHandle.clear();
        handles.clear();
        checkingSet.clear();
    }
    
    public void wakeRefreshThread(){
        refThread.interrupt();
    }
    
    public void addModuleManagerListener(ModuleManagerListener l){
        listeners.add(l);
    }
    
    public void removeModuleManagerListener(ModuleManagerListener l){
        listeners.remove(l);
    }
    
    private void fireModuleListChanged(){
        Iterator it = listeners.iterator();
        
        while(it.hasNext()){
            ((ModuleManagerListener)it.next()).moduleListChanged(mlce);
        }
    }
    
    private void fireModuleAdded(ModuleHandle handle){
        mae.setHandle(handle);
        Iterator it = listeners.iterator();
        
        while(it.hasNext()){
            ((ModuleManagerListener)it.next()).moduleAdded(mae);
        }
    }
    
    private void fireModuleRemoved(ModuleHandle handle, FrontendModule module){
        mre.setHandle(handle);
        mre.setModule(module);
        Iterator it = listeners.iterator();
        
        while(it.hasNext()){
            ((ModuleManagerListener)it.next()).moduleRemoved(mre);
        }
    }
    
    public List getModuleList(){
        List list = null;
        synchronized(hashToModule){
            list = Arrays.asList(hashToModule.values().toArray());
        }
        return list;
    }
    
    public List getModuleHandles(String name, int type){
        Object[] mods;
        synchronized(hashToModule){
            mods = hashToModule.values().toArray();
        }
        
        List list = new LinkedList();
        for(int i = 0; i < mods.length; i++){
            FrontendModule mod = (FrontendModule)mods[i];
            ModuleHandle handle = mod.getHandle();
            if(handle.getModuleType() == type && handle.getModuleName().equals(name)){
                list.add(handle);
            }
        }
        
        return list;
    }
    
    public Iterator getModules(){
        return getModuleList().iterator();
    }
    
    public List getHandlesList(){
        return Collections.unmodifiableList(handles);
    }
    
    public void loadModuleList(Iterator it){
        boolean hasUpdated = false;
        
        while(it.hasNext()){
            ModuleHandle handle = (ModuleHandle)it.next();
            FrontendModule mod = getModule(handle);
            if(mod == null){
                newModuleInstantiation(handle, true);
                hasUpdated = true;
            }
        }
        
        if(hasUpdated){
            fireModuleListChanged();
        }
    }
    
    public void checkModuleHash(String name, int type, byte[] hash) {
        ModuleDescriptor module = receptor.getLinker().getMapping(name, type);
        if(module != null && !Arrays.equals(module.getHash(), hash)) {
            logger.info("Updating module '" + name + "'");
            
            receptor.getLinker().deleteModule(module);
            downloadModule(name, type);
        }
    }
    
    private void addPhantomModule(ModuleHandle handle){
        if(!phantomModules.contains(handle)){
            phantomModules.add(handle);
            downloadModule(handle.getModuleName(), handle.getModuleType());
        }
    }
    
    public void downloadModule(String name, int type) {
        if(!downloadSet.contains(name)) {
            downloadSet.add(name);
            receptor.getOutComm().getModuleJar(name, type);
        }
    }
    
    public void missingDependency(MissingDependencyEvent e) {
        downloadModule(e.getDependency().getName(), e.getDependency().getType());
    }
    
    public void moduleFileAdded(ModuleFileAddedEvent e) {
        ModuleDescriptor desc = e.getModuleDescriptor();
        
        if(desc.getType() == Descriptor.TYPE_FLOW) {
            loadFlowModule(desc);
        }
        
        String modName = desc.getName();
        Object[] handles = phantomModules.toArray();
        
        downloadSet.remove(modName);
        
        for (int i = 0; i < handles.length; i++) {
            ModuleHandle handle = (ModuleHandle) handles[i];
            if(handle.getModuleName().equals(modName)) {
                if(newModuleInstantiation(handle, false) != null){
                    phantomModules.remove(handle);
                    fireModuleAdded(handle);
                }
            }
        }
    }
    
    public void moduleFileRemoved(ModuleFileRemovedEvent e) {
    }
    
    private void loadFlowModule(ModuleDescriptor desc) {
        try {
            FlowModule mod = (FlowModule)newModuleInstance(desc);
            nameToFInst.put(desc.getName(), mod);
            
            Class[] list = mod.getFilterMessageClassList();
            for (int i = 0; i < list.length; i++) {
                receptor.getClassLocator().addClass(list[i]);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } 
    }
    
    private EunomiaModule newModuleInstance(String modName, int type) throws ClassNotFoundException, InstantiationException, IllegalAccessException, NoModuleJarException{
        ModuleDescriptor desc = receptor.getLinker().getMapping(modName, type);
        if(desc == null){
            throw new NoModuleJarException("Loading module: No JAR specified for module " + modName);
        }
        
        return newModuleInstance(desc);
    }
    
    private EunomiaModule newModuleInstance(ModuleDescriptor desc) throws ClassNotFoundException, InstantiationException, IllegalAccessException, NoModuleJarException{
        String className = classPrefix[desc.getType()] + desc.getName() + ".Main";
        ClassLoader loader = desc.getClassLoader();
        Class klass = loader.loadClass(className);
        
        return (EunomiaModule)klass.newInstance();
    }
    
    private FrontendModule instantiateModule(ModuleHandle handle) throws ClassNotFoundException, InstantiationException, IllegalAccessException, NoModuleJarException, InvocationTargetException {
        EunomiaModule module = newModuleInstance(handle.getModuleName(), handle.getModuleType());
        FrontendModule mod = (FrontendModule)wrapperClass[handle.getModuleType()].newInstance(handle, module, receptor);
        
        addModuleInstance(mod, handle);
        
        return mod;
    }
    
    private FrontendModule newModuleInstantiation(ModuleHandle handle, boolean addPhantom){
        try {
            int type = handle.getModuleType();
            FrontendModule module = instantiateModule(handle);
            
            switch(type) {
                case ModuleHandle.TYPE_PROC: {
                    recOut.getModuleControlData(handle);
                    recOut.getModuleListeningList(handle);
                    break;
                }
                
                case ModuleHandle.TYPE_ANLZ: {
                    recOut.getAnalysisParameters(handle);
                    break;
                }
            }
            
            return module;
        } catch(InstantiationException ie){
            ie.printStackTrace();
        } catch(IllegalAccessException iae){
            iae.printStackTrace();
        } catch(InvocationTargetException ite) {
            ite.printStackTrace();
        } catch(ClassNotFoundException cnfe){
            if(addPhantom){
                logger.error("Loading module: JAR does not contain module " + handle.getModuleName());
                addPhantomModule(handle);
            }
        } catch(NoModuleJarException nmj){
            logger.error(nmj.getMessage());
            addPhantomModule(handle);
        }
        
        return null;
    }
    
    private void addModuleInstance(FrontendModule mod, ModuleHandle handle){
        synchronized(hashToModule){
            hashToModule.put(handle, mod);
            handles.add(handle);
            modToHandle.put(mod.getModule(), handle);
            modToHandle.put(mod, handle);
        }
    }
    
    public void removeModuleInstance(ModuleHandle handle){
        FrontendModule mod = getModule(handle);
        synchronized(hashToModule){
            hashToModule.remove(handle);
            modToHandle.remove(mod);
            handles.remove(handle);
        }
    }
    
    public void updateModule(FrontendModule module){
        boolean isNotUpdating = false;
        
        if(isNotUpdating = !checkingSet.contains(module)){
            checkingSet.add(module);
        }
        
        if(isNotUpdating){
            if(module instanceof ProcFrontendModule) {
                recOut.getModuleStatusMessage(module.getHandle());
            } else if(module instanceof AnlzFrontendModule) {
                recOut.getAnalysisReport(module.getHandle());
            }
        }
    }
    
    public void moduleUpdated(FrontendModule module){
        checkingSet.remove(module);
    }
    
    public void getFilter(ProcFrontendModule module, ModuleFilterEditor editor){
        filterEditor = editor;
        recOut.getModuleFilterList(module.getHandle());
    }
    
    public void filterReceived(Filter filter, ProcFrontendModule mod){
        if(filterEditor != null){
            filterEditor.editModuleFilterResp(mod, filter);
        }
    }
    
    public DataOutputStream openInterModuleStream(GUIModule mod) {
        ModuleHandle handle = getModuleHandle(mod);
        return new InterModuleOutComm(handle, receptor);
    }
    
    public ModuleHandle getModuleHandle(GUIModule mod){
        return (ModuleHandle)modToHandle.get(mod);
    }
    
    public FrontendModule getModule(ModuleHandle handle) {
        return (FrontendModule)hashToModule.get(handle);
    }
    
    public FlowModule getFlowModule(String name) {
         return (FlowModule)nameToFInst.get(name);
    }
    
    public EunomiaModule getEunomiaModule(ModuleHandle handle) {
        return (EunomiaModule)hashToModule.get(handle);
    }
    
    public void moduleTerminated(ModuleHandle handle){
        FrontendModule mod = getModule(handle);
        removeModuleInstance(handle);
        fireModuleRemoved(handle, mod);
    }
    
    public void moduleInstantiated(ModuleHandle handle){
        if(newModuleInstantiation(handle, true) != null){
            fireModuleAdded(handle);
        }
    }
    
    public void configurationChanged() {
        refresh = Settings.v().getRefreshInterval();
    }
    
    public void run(){
        while(true){
            int rate = receptor.getRefreshRate();
            if(rate < 200){
                rate = 200;
            }
            
            Util.threadSleep(rate);
            
            Iterator mods = getModules();
            while(mods.hasNext()){
                Object o = mods.next();
                FrontendModule mod = (FrontendModule)o;
                updateModule(mod);
            }
        }
    }
}