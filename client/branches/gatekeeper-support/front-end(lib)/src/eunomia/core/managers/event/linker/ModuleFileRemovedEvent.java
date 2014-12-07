/*
 * ModuleFileRemovedEvent.java
 *
 * Created on July 9, 2007, 10:01 PM
 *
 */

package eunomia.core.managers.event.linker;

import eunomia.core.managers.ModuleDescriptor;
import eunomia.core.receptor.Receptor;

/**
 *
 * @author Mikhail Sosonkin
 */
public class ModuleFileRemovedEvent extends ModuleLinkerEvent {
    private ModuleDescriptor descriptor;
    
    public ModuleFileRemovedEvent(Receptor rec) {
        super(rec);
    }

    public ModuleDescriptor getModuleDescriptor() {
        return descriptor;
    }

    public void setModuleDescriptor(ModuleDescriptor descriptor) {
        this.descriptor = descriptor;
    }
    
}
