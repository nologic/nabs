/*
 * ReceptorStateListener.java
 *
 * Created on January 18, 2006, 4:21 PM
 *
 */

package eunomia.core.managers.listeners;

import eunomia.core.managers.event.state.AddDatabaseEvent;
import eunomia.core.managers.event.state.AddDatabaseTypeEvent;
import eunomia.core.managers.event.state.AddModuleEvent;
import eunomia.core.managers.event.state.AddStreamServerEvent;
import eunomia.core.managers.event.state.ReceptorUserAddedEvent;
import eunomia.core.managers.event.state.ReceptorUserRemovedEvent;
import eunomia.core.managers.event.state.RemoveDatabaseEvent;
import eunomia.core.managers.event.state.RemoveStreamServerEvent;
import eunomia.core.managers.event.state.StreamStatusChangedEvent;

/**
 *
 * @author Mikhail Sosonkin
 */
public interface ReceptorStateListener {
    public void databaseAdded(AddDatabaseEvent e);
    public void databaseRemoved(RemoveDatabaseEvent e);
    public void databaseTypeAdded(AddDatabaseTypeEvent e);
    
    public void moduleAdded(AddModuleEvent e);
    
    public void streamServerAdded(AddStreamServerEvent e);
    public void streamServerRemoved(RemoveStreamServerEvent e);
    public void streamStatusChanged(StreamStatusChangedEvent e);
    
    public void receptorUserAdded(ReceptorUserAddedEvent e);
    public void receptorUserRemoved(ReceptorUserRemovedEvent e);
}