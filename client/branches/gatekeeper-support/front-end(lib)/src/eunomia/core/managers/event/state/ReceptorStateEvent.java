/*
 * ReceptorStateEvent.java
 *
 * Created on May 15, 2007, 9:48 PM
 *
 */

package eunomia.core.managers.event.state;

import eunomia.core.managers.event.ReceptorEvent;
import eunomia.core.receptor.Receptor;
import eunomia.core.receptor.ReceptorState;

/**
 *
 * @author Mikhail Sosonkin
 */
public abstract class ReceptorStateEvent extends ReceptorEvent {
    public ReceptorStateEvent(Receptor rec) {
        super(rec);
    }

    public ReceptorState getReceptorState() {
        return receptor.getState();
    }
    
    public Object getSource() {
        return receptor.getState();
    }
}