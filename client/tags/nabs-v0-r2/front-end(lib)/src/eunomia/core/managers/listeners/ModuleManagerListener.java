/*
 * ModuleManagerListener.java
 *
 * Created on January 5, 2006, 11:47 PM
 */

package eunomia.core.managers.listeners;

import eunomia.messages.receptor.ModuleHandle;
import eunomia.plugin.interfaces.GUIModule;

/**
 *
 * @author Mikhail Sosonkin
 */
public interface ModuleManagerListener {
    public void moduleListChanged();
    public void moduleAdded(ModuleHandle handle);
    public void moduleRemoved(ModuleHandle handle, GUIModule module);
}