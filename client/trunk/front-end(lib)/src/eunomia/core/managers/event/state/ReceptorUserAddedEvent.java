/*
 * ReceptorUserAddedEvent.java
 *
 * Created on May 15, 2007, 10:57 PM
 *
 */

package eunomia.core.managers.event.state;

import eunomia.core.receptor.Receptor;

/**
 *
 * @author Mikhail Sosonkin
 */
public class ReceptorUserAddedEvent extends ReceptorStateEvent {
    private String user;
    
    public ReceptorUserAddedEvent(Receptor r) {
        super(r);
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }
    
}