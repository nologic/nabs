/*
 * ConsoleModuleManager.java
 *
 * Created on July 21, 2007, 1:41 AM
 *
 */

package com.vivic.eunomia.sys.frontend;

import com.vivic.eunomia.module.EunomiaModule;
import com.vivic.eunomia.module.frontend.GUIModule;
import com.vivic.eunomia.module.receptor.FlowModule;
import eunomia.messages.receptor.ModuleHandle;
import java.io.DataOutputStream;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author Mikhail Sosonkin
 */
public interface ConsoleModuleManager {
    public int getSieveModuleInstanceCount();
    public List getModuleList();
    public List getModuleHandles(String name, int type);
    public Iterator getModules();
    public List getHandlesList();
    public DataOutputStream openInterModuleStream(GUIModule mod);
    public EunomiaModule getEunomiaModule(ModuleHandle handle);
    public FlowModule getFlowModule(String name);
}