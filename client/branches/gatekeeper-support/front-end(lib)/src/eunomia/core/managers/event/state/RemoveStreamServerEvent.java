/*
 * RemoveStreamServerEvent.java
 *
 * Created on May 15, 2007, 10:53 PM
 *
 */

package eunomia.core.managers.event.state;

import eunomia.core.receptor.Receptor;
import eunomia.core.receptor.StreamServerDesc;

/**
 *
 * @author Mikhail Sosonkin
 */
public class RemoveStreamServerEvent extends ReceptorStateEvent {
    private StreamServerDesc server;
    
    public RemoveStreamServerEvent(Receptor r) {
        super(r);
    }

    public StreamServerDesc getServer() {
        return server;
    }

    public void setServer(StreamServerDesc server) {
        this.server = server;
    }
    
}
