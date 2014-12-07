/*
 * ModuleManager.java
 *
 * Created on October 23, 2005, 4:54 PM
 *
 */

package eunomia.core.managers;

import eunomia.config.*;
import eunomia.core.data.staticData.DatabaseReportListener;
import eunomia.core.managers.interfaces.ModuleFilterEditor;
import eunomia.core.managers.listeners.ModuleLinkerListener;
import eunomia.core.managers.listeners.ModuleManagerListener;
import eunomia.core.receptor.*;
import eunomia.core.receptor.comm.ReceptorOutComm;
import eunomia.messages.FilterEntryMessage;
import eunomia.messages.module.ModuleMessage;
import eunomia.messages.module.msg.*;
import eunomia.messages.receptor.ModuleHandle;
import eunomia.plugin.GUIPlugin;
import eunomia.plugin.interfaces.GUIModule;
import eunomia.flow.Filter;
import eunomia.flow.FilterEntry;
import eunomia.messages.receptor.ncm.AnalysisReportMessage;
import eunomia.messages.receptor.ncm.AnalysisSummaryMessage;
import eunomia.plugin.interfaces.GUIStaticAnalysisModule;
import eunomia.receptor.module.interfaces.FlowModule;
import eunomia.util.number.ModInteger;

import java.io.*;
import java.util.*;
import org.apache.log4j.Logger;

/**
 *
 * @author Mikhail Sosonkin
 */
public class ModuleManager implements Runnable, ConfigChangeListener, ModuleLinkerListener {
    private List listeners;
    private List dbReportListeners;
    private ReceptorOutComm recOut;
    private ModInteger modRetriever;
    private HashMap hashToModule;
    private HashMap modToHandle;
    private HashMap handleToAnalMod;
    private Map dbToReport;
    private List handles;
    private Receptor receptor;
    private Set checkingSet;
    private int refresh;
    private List phantomModules;
    private Thread refThread;
    private ModuleFilterEditor filterEditor;
    
    private static Logger logger;
    
    static {
        logger = Logger.getLogger(ModuleManager.class);
    }
    
    public ModuleManager(Receptor rec) {
        recOut = rec.getOutComm();
        dbReportListeners = new LinkedList();
        dbToReport = new HashMap();
        listeners = new LinkedList();
        modRetriever = new ModInteger();
        receptor = rec;
        modToHandle = new HashMap();
        hashToModule = new HashMap();
        handleToAnalMod = new HashMap();
        checkingSet = new HashSet();
        handles = new LinkedList();
        phantomModules = Collections.synchronizedList(new LinkedList());
        
        refresh = Settings.getRefreshInterval();
        Settings.addConfigChangeListener(this);
        ModuleLinker.v().addModuleLinkerListener(this);
        
        (refThread = new Thread(this, "Module Manager")).start();
    }
    
    public void addDatabaseReport(AnalysisSummaryMessage msg){
        dbToReport.put(msg.getDatabase(), msg);
        fireDatabaseReportListener(msg);
    }

    public AnalysisSummaryMessage getDatabaseReport(String db){
        return (AnalysisSummaryMessage)dbToReport.get(db);
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

    private void fireDatabaseReportListener(GUIStaticAnalysisModule mod){
        Iterator it = dbReportListeners.iterator();
        while (it.hasNext()) {
            DatabaseReportListener l = (DatabaseReportListener) it.next();
            l.showAnalysisReport(mod);
        }
    }

    public void reset(){
        hashToModule.clear();
        modToHandle.clear();
        handles.clear();
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
            ((ModuleManagerListener)it.next()).moduleListChanged();
        }
    }
    
    private void fireModuleAdded(ModuleHandle handle){
        Iterator it = listeners.iterator();
        
        while(it.hasNext()){
            ((ModuleManagerListener)it.next()).moduleAdded(handle);
        }
    }
    
    private void fireModuleRemoved(ModuleHandle handle, GUIModule module){
        Iterator it = listeners.iterator();
        
        while(it.hasNext()){
            ((ModuleManagerListener)it.next()).moduleRemoved(handle, module);
        }
    }
    
    public List getModuleList(){
        List list = null;
        synchronized(hashToModule){
            list = Arrays.asList(hashToModule.values().toArray());
        }
        return list;
    }
    
    public List getModuleList(String name){
        Object[] mods;
        synchronized(hashToModule){
            mods = hashToModule.values().toArray();
        }
        
        List list = new LinkedList();
        for(int i = 0; i < mods.length; i++){
            if(((GUIPlugin)mods[i]).getName().equals(name)){
                list.add(mods[i]);
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
    
    public Iterator getHandles(){
        return getHandlesList().iterator();
    }
    
    public void loadModuleList(Iterator it){
        boolean hasUpdated = false;
        
        while(it.hasNext()){
            ModuleHandle handle = (ModuleHandle)it.next();
            GUIModule mod = getModuleById(handle.getInstanceID());
            if(mod == null){
                mod = newModuleInstantiation(handle, true);
                hasUpdated = true;
            }
        }
        
        if(hasUpdated){
            fireModuleListChanged();
        }
    }
    
    private void addPhantomPlugin(ModuleHandle handle){
        if(!phantomModules.contains(handle)){
            phantomModules.add(handle);
        }
    }
    
    public void listChanged() {
        Object[] handles = phantomModules.toArray();
        for (int i = 0; i < handles.length; i++) {
            ModuleHandle handle = (ModuleHandle) handles[i];
            if(newModuleInstantiation(handle, false) != null){
                phantomModules.remove(handle);
                fireModuleAdded(handle);
            }
        }
    }
    
    private GUIModule newModuleInstantiation(ModuleHandle handle, boolean addPhantom){
        try {
            GUIModule mod = instantiateModule(handle.getModuleName());
            if(mod == null){
                if(addPhantom){
                    logger.error("Loading module: No JAR specified for module " + handle.getModuleName());
                    addPhantomPlugin(handle);
                }
                
                return null;
            }
            
            addModuleInstance(mod, handle);
            recOut.getModuleControlData(handle);
            return mod;
        } catch(InstantiationException ie){
            ie.printStackTrace();
        } catch(IllegalAccessException iae){
            iae.printStackTrace();
        } catch(ClassNotFoundException cnfe){
            if(addPhantom){
                logger.error("Loading module: JAR does not countain module " + handle.getModuleName());
                addPhantomPlugin(handle);
            }
        }
        
        return null;
    }
    
    private GUIStaticAnalysisModule newAnalysisInstantiation(ModuleHandle handle) {
        try {
            GUIStaticAnalysisModule mod = instantiateAnalysisModule(handle.getModuleName());
            if(mod == null){
                return null;
            }
            
            addAnalysisModuleInstance(mod, handle);
            
            return mod;
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        } catch (IllegalAccessException ex) {
            ex.printStackTrace();
        } catch (InstantiationException ex) {
            ex.printStackTrace();
        }
        
        return null;
    }
    
    public void addModuleInstance(GUIModule mod, ModuleHandle handle){
        synchronized(hashToModule){
            hashToModule.put(handle, mod);
            handles.add(handle);
            modToHandle.put(mod, handle);
            modToHandle.put(((GUIPlugin)mod).getModule(), handle);
        }
    }
    
    public void removeModuleInstance(ModuleHandle handle){
        GUIModule mod = getModuleById(handle.getInstanceID());
        synchronized(hashToModule){
            hashToModule.remove(handle);
            modToHandle.remove(mod);
            handles.remove(handle);
        }
    }
    
    public void addAnalysisModuleInstance(GUIStaticAnalysisModule mod, ModuleHandle handle) {
        synchronized(handleToAnalMod) {
            handleToAnalMod.put(handle, mod);
        }
    }
    
    public void removeAnalysisModuleInstance(ModuleHandle handle){
    }
    
    public void sendControlData(GUIModule module) throws IOException {
        ModuleHandle handle = getModuleHandle(module);
        recOut.sendModuleControlData(handle, module);
    }
    
    public void getControlData(GUIModule module) throws IOException {
        ModuleHandle handle = getModuleHandle(module);
        recOut.getModuleControlData(handle);
    }
    
    public void stopModule(GUIModule module) throws IOException {
        ModuleHandle handle = getModuleHandle(module);
        recOut.sendAction(handle, ActionMessage.STOP);
    }
    
    public void startModule(GUIModule module) throws IOException {
        ModuleHandle handle = getModuleHandle(module);
        recOut.sendAction(handle, ActionMessage.START);
    }
    
    public void resetModule(GUIModule module) throws IOException {
        ModuleHandle handle = getModuleHandle(module);
        recOut.sendAction(handle, ActionMessage.RESET);
    }
    
    public void updateModule(GUIModule module){
        ModuleHandle handle = getModuleHandle(module);
        boolean isNotUpdating = false;
        
        synchronized(checkingSet){
            if(isNotUpdating = !checkingSet.contains(module)){
                checkingSet.add(module);
            }
        }
        
        if(isNotUpdating){
            recOut.getModuleStatusMessage(handle);
        }
    }
    
    public void createModInstance(String name){
        recOut.instantiateModule(name);
    }
    
    public void terminateModInstance(GUIModule mod){
        ModuleHandle handle = getModuleHandle(mod);
        recOut.terminateModule(handle);
    }
    
    public void terminateModInstance(ModuleHandle handle){
        recOut.terminateModule(handle);
    }
    
    public void getFilter(GUIModule module, ModuleFilterEditor editor){
        ModuleHandle handle = getModuleHandle(module);
        filterEditor = editor;
        recOut.getModuleFilterList(handle);
    }
    
    public void setFilter(GUIModule module, Filter filter){
        ModuleHandle handle = getModuleHandle(module);
        FilterEntryMessage[] wList = null;
        FilterEntryMessage[] bList = null;
        
        FilterEntry[] entries = filter.getWhiteList().getAsArray();
        if(entries != null){
            wList = new FilterEntryMessage[entries.length];
            for(int i = 0; i < entries.length; i++){
                wList[i] = entries[i].getFilterEntryMessage();
            }
        }
        
        entries = filter.getBlackList().getAsArray();
        if(entries != null){
            bList = new FilterEntryMessage[entries.length];
            for(int i = 0; i < entries.length; i++){
                bList[i] = entries[i].getFilterEntryMessage();
            }
        }
        
        recOut.sendChangeFilter(handle, wList, bList);
    }
    
    public GenericModuleMessage prepareGenericMessage(GUIModule mod){
        GenericModuleMessage gmm = new GenericModuleMessage();
        ModuleHandle handle = getModuleHandle(mod);
        gmm.setModuleID(handle.getInstanceID());
        
        return gmm;
    }
    
    public void sendGenericMessage(GUIModule mod, GenericModuleMessage gmm){
        receptor.sendMessage(gmm, mod.getReceiver());
    }
    
    public ModuleHandle getModuleHandle(GUIModule mod){
        return (ModuleHandle)modToHandle.get(mod);
    }
    
    public GUIStaticAnalysisModule instantiateAnalysisModule(String name) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        String className = "eunomia.module.data.gui." + name + ".Main";
        
        ModuleDescriptor desc = ModuleLinker.v().getAnlzMapping(name);
        if(desc == null){
            return null;
        }
        
        ClassLoader loader = desc.getClassLoader();
        Class klass = loader.loadClass(className);
        GUIStaticAnalysisModule mod = (GUIStaticAnalysisModule)klass.newInstance();
        
        return mod;
    }
    
    public GUIModule instantiateModule(String name) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        String className = "eunomia.plugin.gui." + name + ".Main";
        
        ModuleDescriptor desc = ModuleLinker.v().getProcMapping(name);
        if(desc == null){
            return null;
        }
        
        ClassLoader loader = desc.getClassLoader();
        Class klass = loader.loadClass(className);
        GUIModule mod = new GUIPlugin(name, (GUIModule)klass.newInstance());
        mod.setReceptor(receptor);
        
        return mod;
    }
    
    public GUIModule getModuleByHandle(ModuleHandle handle){
        return (GUIModule)hashToModule.get(handle);
    }
    
    public GUIModule getModuleById(int id){
        modRetriever.setInt(id);
        return (GUIModule)hashToModule.get(modRetriever);
    }
    
    public void moduleTerminated(ModuleHandle handle){
        GUIModule mod = getModuleById(handle.getInstanceID());
        removeModuleInstance(handle);
        fireModuleRemoved(handle, mod);
    }
    
    public void moduleInstantiated(ModuleHandle handle){
        if(newModuleInstantiation(handle, true) != null){
            fireModuleAdded(handle);
        }
    }
    
    public void analysisModuleInstantiated(ModuleHandle handle){
        newAnalysisInstantiation(handle);
    }
    
    public GUIStaticAnalysisModule getAnalysisModule(ModuleHandle handle){
        return (GUIStaticAnalysisModule)handleToAnalMod.get(handle);
    }
    
    public void analysisModuleReport(AnalysisReportMessage report){
        ModuleHandle handle = report.getHandle();
        GUIStaticAnalysisModule mod = getAnalysisModule(handle);
        
        if(mod == null){
            mod = newAnalysisInstantiation(handle);
        }
        mod.setResult(report.getReportInputStream());
        
        fireDatabaseReportListener(mod);
    }
    
    public void closeAnalysisModule(ModuleHandle handle) {
    }
    
    public void processModuleMessage(ModuleMessage msg) throws Exception {
        GUIModule mod = getModuleById(msg.getModuleID());
        
        if(msg instanceof ModuleStatusMessage){
            synchronized(checkingSet){
                checkingSet.remove(mod);
            }
            mod.updateStatus(((ModuleStatusMessage)msg).getInputStream());
        } else if(msg instanceof ModuleControlDataMessage){
            mod.setControlData(((ModuleControlDataMessage)msg).getInputStream());
        } else if(msg instanceof ChangeFilterMessage){
            ChangeFilterMessage cfm = (ChangeFilterMessage)msg;
            Filter filter = new Filter();
            
            FilterEntryMessage[] fems = cfm.getWhiteList();
            if(fems != null){
                for(int i = 0; i < fems.length; i++){
                    FlowModule fmod = FlowModuleManager.ins.getFlowModuleInstance(fems[i].getFlowModule());
                    if(fmod != null){
                        filter.addFilterWhite(fmod.getNewFilterEntry(fems[i]));
                    }
                }
            }
            
            fems = cfm.getBlackList();
            if(fems != null){
                for(int i = 0; i < fems.length; i++){
                    FlowModule fmod = FlowModuleManager.ins.getFlowModuleInstance(fems[i].getFlowModule());
                    if(fmod != null){
                        filter.addFilterBlack(fmod.getNewFilterEntry(fems[i]));
                    }
                }
            }
            
            if(filterEditor != null){
                filterEditor.editModuleFilterResp(mod, filter);
            }
        } else {
            System.out.println("Unknown message: " + msg + "\n\tfor module: " + mod);
        }
    }
    
    public void configurationChanged() {
        refresh = Settings.getRefreshInterval();
    }
    
    public void run(){
        while(true){
            try {
                int rate = receptor.getRefreshRate();
                if(rate < 200){
                    rate = 200;
                }
                Thread.sleep(rate);
            } catch(Exception e){
                e.printStackTrace();
            }
            
            try {
                Iterator mods = getModules();
                while(mods.hasNext()){
                    GUIModule mod = (GUIModule)mods.next();
                    updateModule(mod);
                }
            } catch(Exception e){
                e.printStackTrace();
            }
        }
    }
}