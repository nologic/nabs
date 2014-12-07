/*
 * SieveModuleManager.java
 *
 * Created on July 22, 2007, 12:23 AM
 *
 */

package com.vivic.eunomia.sys.receptor;

import com.vivic.eunomia.module.EunomiaModule;
import com.vivic.eunomia.module.receptor.ReceptorProcessorModule;
import com.vivic.eunomia.module.flow.FlowModule;
import eunomia.messages.receptor.ModuleHandle;
import java.util.List;

/**
 * This class provides various methods for accessing module instances.
 * @author Mikhail Sosonkin
 */
public interface SieveModuleManager {
    /**
     * Returns the instance of the flow producer with the specified name. It is often
     * useful to access flow producer to make flow object, filter object, etc. Flow
     * producer modules are instantiated onces at startup of Sieve. Those instances are
     * then reused.
     * @param name Flow producer with module name: 'name'
     * @return Flow producer instance.
     */
    public FlowModule getFlowModuleInstance(String name);
    
    /**
     * 
     * @param mod 
     * @return 
     */
    public String getFlowModuleName(FlowModule mod);
    /**
     * Collects and returns a list of module names with specified type.
     * @param type Module type.
     * @return Array of module
     */
    public String[] getModuleNames(int type);
    public ReceptorProcessorModule getProcessorModule(ModuleHandle handle);
    /**
     * 
     * @param name 
     * @param type 
     * @return 
     */
    public List getModuleHandleList(String name, int type);
    
    public EunomiaModule getModule(ModuleHandle handle);
    
    /**
     * Returns a module instance. This method will check if there is an instance of that
     * module already exists. If so, then it will take the 1st available instance (not
     * guaranteed to be the sameone across calls). If not, then a new instance will be
     * created and returned.
     * @param name Name of the module
     * @param type Module type
     * @throws java.lang.Exception 
     * @return Module instance.
     */
    public EunomiaModule getInstanceEnsure(String name, int type) throws Exception;
    
    /**
     * Returns an instance in it's raw form. Internally all modules are maintained in 
     * wrapper object. Designed to intercept all method calls. However, this means that
     * the module object cannot be used in its natural form and cannot be cast to the
     * implementing type. So to achieve that, this method will extract the module's main
     * object.
     * @param mod Module object as returned by getInstanceEnsure().
     * @return The object created by instantiating the Main class for a module.
     */
    public EunomiaModule unwrap(EunomiaModule mod);
}