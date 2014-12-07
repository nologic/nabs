/*
 * ModuleManagerListener.java
 *
 * Created on January 5, 2006, 11:47 PM
 */

package eunomia.core.managers.listeners;

import eunomia.core.managers.event.state.module.ModuleAddedEvent;
import eunomia.core.managers.event.state.module.ModuleListChangedEvent;
import eunomia.core.managers.event.state.module.ModuleRemovedEvent;

/**
 *
 * @author Mikhail Sosonkin
 */
public interface ModuleManagerListener {
    public void moduleListChanged(ModuleListChangedEvent e);
    public void moduleAdded(ModuleAddedEvent e);
    public void moduleRemoved(ModuleRemovedEvent e);
}