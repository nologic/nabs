/*
 * ModuleAddedEvent.java
 *
 * Created on June 24, 2007, 1:05 AM
 *
 */

package eunomia.core.managers.event.state.module;

import eunomia.core.receptor.Receptor;
import eunomia.messages.receptor.ModuleHandle;

/**
 *
 * @author Mikhail Sosonkin
 */
public class ModuleAddedEvent extends ModuleManagerEvent {
    private ModuleHandle handle;
    
    public ModuleAddedEvent(Receptor r) {
        super(r);
    }

    public ModuleHandle getHandle() {
        return handle;
    }

    public void setHandle(ModuleHandle handle) {
        this.handle = handle;
    }
    
    
}
