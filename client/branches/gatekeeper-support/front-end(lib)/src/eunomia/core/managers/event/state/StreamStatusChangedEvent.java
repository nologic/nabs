/*
 * StreamStatusChangedEvent.java
 *
 * Created on May 15, 2007, 10:50 PM
 *
 */

package eunomia.core.managers.event.state;

import eunomia.core.receptor.Receptor;
import eunomia.core.receptor.StreamServerDesc;

/**
 *
 * @author Mikhail Sosonkin
 */
public class StreamStatusChangedEvent extends ReceptorStateEvent {
    private StreamServerDesc server;
    private int oldStatus;
    private int newStatus;
    
    public StreamStatusChangedEvent(Receptor r) {
        super(r);
    }

    public StreamServerDesc getServer() {
        return server;
    }

    public void setServer(StreamServerDesc server) {
        this.server = server;
    }

    public int getOldStatus() {
        return oldStatus;
    }

    public void setOldStatus(int oldStatus) {
        this.oldStatus = oldStatus;
    }

    public int getNewStatus() {
        return newStatus;
    }

    public void setNewStatus(int newStatus) {
        this.newStatus = newStatus;
    }
    
}
