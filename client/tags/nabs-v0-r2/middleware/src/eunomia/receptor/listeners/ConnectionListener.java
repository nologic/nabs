/*
 * ConnectionListener.java
 *
 * Created on July 6, 2006, 9:37 PM
 *
 */

package eunomia.receptor.listeners;

import eunomia.receptor.FlowServer;

/**
 *
 * @author Mikhail Sosonkin
 */
public interface ConnectionListener {
    public void connectionSuccessful(FlowServer server);
    public void connectionFailure(FlowServer server);
    public void connectionDropped(FlowServer server);
    public void connectionClosed(FlowServer server);
}
