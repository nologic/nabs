/*
 * ModuleRemovedEvent.java
 *
 * Created on June 24, 2007, 1:07 AM
 *
 */

package eunomia.core.managers.event.state.module;

import eunomia.core.receptor.Receptor;
import eunomia.messages.receptor.ModuleHandle;
import eunomia.module.FrontendModule;

/**
 *
 * @author Mikhail Sosonkin
 */
public class ModuleRemovedEvent extends ModuleManagerEvent {
    private ModuleHandle handle;
    private FrontendModule module;
    
    public ModuleRemovedEvent(Receptor r) {
        super(r);
    }

    public ModuleHandle getHandle() {
        return handle;
    }

    public void setHandle(ModuleHandle handle) {
        this.handle = handle;
    }

    public FrontendModule getModule() {
        return module;
    }

    public void setModule(FrontendModule module) {
        this.module = module;
    }
    
}