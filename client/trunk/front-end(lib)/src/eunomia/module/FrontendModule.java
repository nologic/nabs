/*
 * FrontendModule.java
 *
 * Created on January 14, 2007, 1:18 PM
 *
 */

package eunomia.module;

import eunomia.core.receptor.Receptor;
import eunomia.messages.receptor.ModuleHandle;
import com.vivic.eunomia.module.EunomiaModule;

/**
 *
 * @author Mikhail Sosonkin
 */
public abstract class FrontendModule {
    protected EunomiaModule module;
    protected ModuleHandle handle;
    protected Receptor receptor;

    public FrontendModule(ModuleHandle handle, EunomiaModule mod, Receptor receptor) {
        this.handle = handle;
        this.module = mod;
        this.receptor = receptor;
    }
    
    public EunomiaModule getModule() {
        return module;
    }

    public ModuleHandle getHandle() {
        return handle;
    }
    
    public Receptor getReceptor() {
        return receptor;
    }
}