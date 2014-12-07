/*
 * ModuleLinkerEvent.java
 *
 * Created on July 9, 2007, 9:26 PM
 *
 */

package eunomia.core.managers.event.linker;

import eunomia.core.managers.ModuleLinker;
import eunomia.core.managers.event.ReceptorEvent;
import eunomia.core.receptor.Receptor;

/**
 *
 * @author Mikhail Sosonkin
 */
public abstract class ModuleLinkerEvent extends ReceptorEvent {
    
    public ModuleLinkerEvent(Receptor rec) {
        super(rec);
    }
    
    public ModuleLinker getModuleLinker() {
        return receptor.getLinker();
    }
    
    public Object getSource() {
        return receptor.getLinker();
    }
}
