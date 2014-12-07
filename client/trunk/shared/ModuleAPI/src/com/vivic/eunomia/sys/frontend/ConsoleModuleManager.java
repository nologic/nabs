/*
 * ConsoleModuleManager.java
 *
 * Created on July 21, 2007, 1:41 AM
 *
 */

package com.vivic.eunomia.sys.frontend;

import com.vivic.eunomia.module.EunomiaModule;
import com.vivic.eunomia.module.frontend.FrontendProcessorModule;
import com.vivic.eunomia.module.flow.FlowModule;
import eunomia.messages.receptor.ModuleHandle;
import java.io.DataOutputStream;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author Mikhail Sosonkin
 */
public interface ConsoleModuleManager {
    /**
     * Returns the number of total module instantiations on the Sieve. This number includes
     * both phantom (with no corresponing instance on the Console) and actual modules.
     *
     * @return Number of module instances.
     */
    public int getSieveModuleInstanceCount();

    /**
     * Returns a new list of all module handles.
     *
     * @return module handles list.
     */
    public List getModuleList();

    /**
     * Returns a new list of module handles with specified characteristics. This methods allows
     * you to choose handles with specified name and type.
     *
     * @param name name of the module
     * @param type type of the module
     * @return List of handles that match both the name and type.
     */
    public List getModuleHandles(String name, int type);

    /**
     * Returns the iterator for <code>getModuleList()</code>.
     *
     * @return Iterator of the module handles list.
     */
    public Iterator getModules();

    /**
     * Returns an unmodifiable list of module handles.
     *
     * @return module handles list.
     */
    public List getHandlesList();

    /**
     * Returns a data output stream for module intercomm. This is the method for sending messages
     * from Console to Sieve side of the 'mod' module. To use this method the module must pass in
     * it's main class as the parameter. Then write data into the output stream. To send the message,
     * the stream must be closed.
     *
     * @param mod module instance.
     * @return output stream.
     */
    public DataOutputStream openInterModuleStream(FrontendProcessorModule mod);

    /**
     * Returns the module instance for a specific handle.
     *
     * @param handle instance handle
     * @return Module instance
     */
    public EunomiaModule getEunomiaModule(ModuleHandle handle);

    /**
     * Returns the flow module with a specific name.
     *
     * @param name name of the flow module.
     * @return Flow module.
     */
    public FlowModule getFlowModule(String name);
}