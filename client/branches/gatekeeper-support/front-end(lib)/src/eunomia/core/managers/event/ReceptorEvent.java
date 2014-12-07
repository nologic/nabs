/*
 * ReceptorEvent.java
 *
 * Created on May 15, 2007, 9:43 PM
 *
 */

package eunomia.core.managers.event;

import eunomia.core.receptor.Receptor;

/**
 *
 * @author Mikhail Sosonkin
 */
public abstract class ReceptorEvent extends ConsoleEvent {
    protected Receptor receptor;
    
    public ReceptorEvent(Receptor rec) {
        super(rec);
        receptor = rec;
    }

    public Receptor getReceptor() {
        return receptor;
    }
}