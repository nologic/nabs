/*
 * MiddlewareModule.java
 *
 * Created on May 3, 2007, 9:57 PM
 *
 */

package eunomia.module;

import eunomia.messages.receptor.ModuleHandle;
import com.vivic.eunomia.module.EunomiaModule;

/**
 *
 * @author Mikhail Sosonkin
 */
public class MiddlewareModule {
    protected EunomiaModule module;
    protected ModuleHandle handle;

    public MiddlewareModule(ModuleHandle handle, EunomiaModule mod) {
        this.handle = handle;
        this.module = mod;
    }
    
    public EunomiaModule getModule() {
        return module;
    }

    public ModuleHandle getHandle() {
        return handle;
    }
}