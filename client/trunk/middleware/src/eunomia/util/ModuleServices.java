/*
 * ModuleSerices.java
 *
 * Created on January 5, 2008, 3:01 PM
 *
 */

package eunomia.util;

import com.vivic.eunomia.module.EunomiaModule;
import com.vivic.eunomia.sys.exception.IncorrectObjectException;
import com.vivic.eunomia.sys.receptor.SieveModuleServices;
import eunomia.managers.ModuleManager;
import eunomia.managers.ReceptorManager;
import eunomia.messages.receptor.ModuleHandle;
import eunomia.module.MiddlewareModule;

/**
 *
 * @author Mikhail Sosonkin
 */
public class ModuleServices implements SieveModuleServices {
    private static ModuleServices instance;
    
    static {
        instance = new ModuleServices();
    }
    
    private ModuleServices() {
    }
    
    public static ModuleServices v() {
        return instance;
    }

    public void addDefaultConnect(ModuleHandle handle) {
        ReceptorManager.v().addDefaultConnect(ModuleManager.v().getFlowProcessorConnectTuple(handle));
    }

    public void removeDefaultConnect(ModuleHandle handle) {
        ReceptorManager.v().removeDefaultConnect(ModuleManager.v().getFlowProcessorConnectTuple(handle));
    }

    public void connectAllSensors(ModuleHandle handle) {
        ReceptorManager.v().addFlowProcessor(ModuleManager.v().getFlowProcessorConnectTuple(handle));
    }

    public void disconnectAllSensors(ModuleHandle handle) {
        ReceptorManager.v().removeFlowProcessor(ModuleManager.v().getFlowProcessorConnectTuple(handle));
    }

    public ModuleHandle getModuleHandle(EunomiaModule module) throws IncorrectObjectException {
        if(module instanceof MiddlewareModule) {
            return ((MiddlewareModule)module).getHandle();
        }
        
        throw new IncorrectObjectException("");
    }
}