/*
 * ModuleListChangedEvent.java
 *
 * Created on June 24, 2007, 1:04 AM
 *
 */

package eunomia.core.managers.event.state.module;

import eunomia.core.receptor.Receptor;

/**
 *
 * @author Mikhail Sosonkin
 */
public class ModuleListChangedEvent extends ModuleManagerEvent {
    
    public ModuleListChangedEvent(Receptor r) {
        super(r);
    }
    
}
