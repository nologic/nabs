/*
 * UserState.java
 *
 * Created on February 21, 2006, 4:40 PM
 *
 */

package eunomia.comm;

import eunomia.managers.ModuleManager;
import eunomia.messages.receptor.ModuleHandle;
import eunomia.modules.AnalysisModule;
import eunomia.plugin.interfaces.ReceptorModule;
import java.util.*;

/**
 *
 * @author Mikhail Sosonkin
 */
public class UserState {
    private ModuleHandle retriever;
    
    private Set connected;
    private Set handles;
    private Set analSet;
    
    public UserState(String user) {
        connected = new HashSet();
        handles = new HashSet();
        analSet = new HashSet();
        retriever = new ModuleHandle();
    }
    
    public void addProcHandle(ModuleHandle handle){
        handles.add(handle);
    }
    
    public ModuleHandle startProcModule(String name) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        ModuleHandle handle = ModuleManager.v().startModule(name);
        handles.add(handle);
        
        return handle;
    }
    
    public AnalysisModule startAnlzModule(String name) throws IllegalAccessException, ClassNotFoundException, InstantiationException {
        AnalysisModule mod = ModuleManager.v().startStaticAnalysisModule(name);
        analSet.add(mod.getHandle());
        
        return mod;
    }
    
    public boolean containsAnlzHandle(ModuleHandle handle){
        return analSet.contains(handle);
    }
    
    public void terminateProcModule(ModuleHandle handle){
        if(handles.contains(handle)){
            ModuleManager.v().terminateModule(handle);
            handles.remove(handle);
        }
    }
    
    public List getProcHandles(){
        return Arrays.asList(handles.toArray());
    }
    
    public ReceptorModule getProcModule(int mod){
        synchronized(retriever){
            retriever.setInstanceID(mod);

            if(handles.contains(retriever)){
                return ModuleManager.v().getModule(mod);
            }
        }
        
        return null;
    }
}
