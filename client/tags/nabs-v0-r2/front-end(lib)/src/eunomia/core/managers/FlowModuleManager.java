/*
 * FlowModuleManager.java
 *
 * Created on July 4, 2006, 10:03 PM
 *
 */

package eunomia.core.managers;

import eunomia.core.managers.listeners.ModuleLinkerListener;
import eunomia.receptor.module.interfaces.FlowModule;
import java.util.HashMap;

/**
 *
 * @author Mikhail Sosonkin
 */
public class FlowModuleManager {
    public static final FlowModuleManager ins = new FlowModuleManager();
    
    private HashMap nameToFInst;
    private HashMap fInstToName;
    
    private FlowModuleManager() {
        nameToFInst = new HashMap();
        fInstToName = new HashMap();
    }
    
    public void addModule(String name) throws ClassNotFoundException, InstantiationException, IllegalAccessException{
        if(!nameToFInst.containsKey(name)){
            FlowModule mod = (FlowModule)ClassLoader.getSystemClassLoader().loadClass("eunomia.receptor.module." + name + ".Main").newInstance();
            nameToFInst.put(name, mod);
            fInstToName.put(mod, name);
        }
    }
    
    public FlowModule getFlowModuleInstance(String name){
        return (FlowModule)nameToFInst.get(name);
    }
    
    public String getFlowModuleName(FlowModule mod){
        return (String)fInstToName.get(mod);
    }
    
    public FlowModule getFlowModuleInstance(Class klass){
        String name = klass.getName();
        if(name.startsWith("eunomia.receptor.module.")){
            String[] parts = name.split("\\.");
            if(parts.length >= 3){
                return getFlowModuleInstance(parts[3]);
            }
        }
        
        return null;
    }
    
    public String[] getNamesArray(){
        return (String[])nameToFInst.keySet().toArray(new String[]{});
    }
}
