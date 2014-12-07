/*
 * AddFlowProcessorModule.java
 *
 * Created on May 15, 2007, 9:57 PM
 *
 */

package eunomia.core.managers.event.state;

import eunomia.core.receptor.Receptor;

/**
 *
 * @author Mikhail Sosonkin
 */
public class AddModuleEvent extends ReceptorStateEvent {
    private String mod;
    private int type;
    
    public AddModuleEvent(Receptor rec) {
        super(rec);
    }

    public String getModule() {
        return mod;
    }

    public void setModule(String mod) {
        this.mod = mod;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}