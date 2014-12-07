/*
 * ModuleLinkerListener.java
 *
 * Created on March 27, 2006, 8:41 PM
 *
 */

package eunomia.core.managers.listeners;

import eunomia.core.managers.event.linker.MissingDependencyEvent;
import eunomia.core.managers.event.linker.ModuleFileAddedEvent;
import eunomia.core.managers.event.linker.ModuleFileRemovedEvent;

/**
 *
 * @author Mikhail Sosonkin
 */
public interface ModuleLinkerListener {
    public void missingDependency(MissingDependencyEvent e);
    public void moduleFileAdded(ModuleFileAddedEvent e);
    public void moduleFileRemoved(ModuleFileRemovedEvent e);
}