/*
 * SeiveReceptorManager.java
 *
 * Created on January 5, 2008, 1:47 PM
 *
 */

package com.vivic.eunomia.sys.receptor;

import com.vivic.eunomia.module.EunomiaModule;
import com.vivic.eunomia.sys.exception.IncorrectObjectException;
import eunomia.messages.receptor.ModuleHandle;

/**
 *
 * @author Mikhail Sosonkin
 */
public interface SieveModuleServices {
    public void addDefaultConnect(ModuleHandle handle);
    public void removeDefaultConnect(ModuleHandle handle);
    public void connectAllSensors(ModuleHandle handle);
    public void disconnectAllSensors(ModuleHandle handle);
    public ModuleHandle getModuleHandle(EunomiaModule module) throws IncorrectObjectException;
}