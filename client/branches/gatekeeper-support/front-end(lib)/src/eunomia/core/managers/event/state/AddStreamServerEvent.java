/*
 * AddStreamEvent.java
 *
 * Created on May 15, 2007, 9:52 PM
 *
 */

package eunomia.core.managers.event.state;

import eunomia.core.receptor.Receptor;
import eunomia.core.receptor.StreamServerDesc;

/**
 *
 * @author Mikhail Sosonkin
 */
public class AddStreamServerEvent extends ReceptorStateEvent {
    private StreamServerDesc stream;
    
    public AddStreamServerEvent(Receptor rec) {
        super(rec);
    }
    
    public void setServer(StreamServerDesc stream){
        this.stream = stream;
    }

    public StreamServerDesc getServer() {
        return stream;
    }
}