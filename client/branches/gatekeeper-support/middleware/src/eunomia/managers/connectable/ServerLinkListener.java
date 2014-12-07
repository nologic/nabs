/*
 * ServerLinkListener.java
 *
 * Created on September 4, 2006, 10:54 PM
 *
 */

package eunomia.managers.connectable;

import eunomia.receptor.FlowServer;

/**
 *
 * @author Mikhail Sosonkin
 */
public interface ServerLinkListener {
    public static final int UNACCEPTABLE_MODULE = 0;
    
    public void connectedTo(FlowServer serv);
    public void disconnectedFrom(FlowServer serv);
    public void connectError(FlowServer serv, int err);
}