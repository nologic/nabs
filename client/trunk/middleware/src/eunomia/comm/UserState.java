/*
 * UserState.java
 *
 * Created on February 21, 2006, 4:40 PM
 *
 */

package eunomia.comm;

import eunomia.exception.ManagerException;
import eunomia.managers.ModuleManager;
import eunomia.messages.receptor.ModuleHandle;
import eunomia.module.AnlzMiddlewareModule;
import com.vivic.eunomia.module.receptor.ReceptorProcessorModule;
import eunomia.module.ReportingModule;
import java.util.*;

/**
 *
 * @author Mikhail Sosonkin
 */
public class UserState {
    private Set connected;
    private Set handles;
    
    public UserState(String user) {
        connected = new HashSet();
        handles = new HashSet();
    }
    
    public ModuleHandle startProcModule(String name) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        ModuleHandle handle = ModuleManager.v().startModule_PROC(name);
        handles.add(handle);
        
        return handle;
    }
    
    public ModuleHandle startAnlzModule(String name) throws IllegalAccessException, ClassNotFoundException, InstantiationException {
        ModuleHandle handle = ModuleManager.v().startModule_ANLZ(name);
        handles.add(handle);
        
        return handle;
    }
    
    public void terminateModule(ModuleHandle handle) throws ManagerException {
        if(handles.contains(handle)){
            ModuleManager.v().terminateModule(handle);
            handles.remove(handle);
        }
    }
    
    public void addHandle(ModuleHandle handle){
        handles.add(handle);
    }

    public List getHandles(){
        return Arrays.asList(handles.toArray());
    }
    
    public boolean hasHandle(ModuleHandle handle){
        return handles.contains(handle);
    }
    
    public ReportingModule getReportingModule(ModuleHandle handle) {
        if(hasHandle(handle)) {
            switch (handle.getModuleType()) {
                case ModuleHandle.TYPE_PROC:
                    return ModuleManager.v().getProcessorModule(handle);
                case ModuleHandle.TYPE_ANLZ:
                    return ModuleManager.v().getAnalysisModule(handle);
            }
        }
        
        return null;
    }
}