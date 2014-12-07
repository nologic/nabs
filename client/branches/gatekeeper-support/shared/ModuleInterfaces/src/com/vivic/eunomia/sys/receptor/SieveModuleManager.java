/*
 * SieveModuleManager.java
 *
 * Created on July 22, 2007, 12:23 AM
 *
 */

package com.vivic.eunomia.sys.receptor;

import com.vivic.eunomia.module.receptor.ReceptorModule;
import com.vivic.eunomia.module.receptor.FlowModule;
import eunomia.messages.receptor.ModuleHandle;

/**
 *
 * @author Mikhail Sosonkin
 */
public interface SieveModuleManager {
    public FlowModule getFlowModuleInstance(String name);
    public String getFlowModuleName(FlowModule mod);
    public String[] getModuleNames(int type);
    public ReceptorModule getProcessorModule(ModuleHandle handle);
    //public AnlzMiddlewareModule getAnalysisModule(ModuleHandle handle);
}
