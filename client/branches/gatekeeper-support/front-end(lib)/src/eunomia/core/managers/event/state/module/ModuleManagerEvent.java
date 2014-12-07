/*
 * ModuleManagerEvent.java
 *
 * Created on June 24, 2007, 1:00 AM
 *
 */

package eunomia.core.managers.event.state.module;

import eunomia.core.managers.ModuleManager;
import eunomia.core.managers.event.ReceptorEvent;
import eunomia.core.receptor.Receptor;

/**
 *
 * @author Mikhail Sosonkin
 */
public class ModuleManagerEvent extends ReceptorEvent {
    public ModuleManagerEvent(Receptor r) {
        super(r);
    }
    
    public ModuleManager getModuleManager() {
        return receptor.getManager();
    }
    
    public Object getSource() {
        return receptor.getManager();
    }
}