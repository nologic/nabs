/*
 * AddDatabaseTypeEvent.java
 *
 * Created on May 15, 2007, 10:33 PM
 *
 */

package eunomia.core.managers.event.state;

import eunomia.core.receptor.Receptor;

/**
 *
 * @author Mikhail Sosonkin
 */
public class AddDatabaseTypeEvent extends ReceptorStateEvent {
    private String type;
    
    public AddDatabaseTypeEvent(Receptor r) {
        super(r);
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
    
}
