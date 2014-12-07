/*
 * ConTuple.java
 *
 * Created on September 4, 2006, 10:52 PM
 *
 */

package eunomia.managers.connectable;

import eunomia.flow.FlowProcessor;
import eunomia.receptor.FlowServer;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Mikhail Sosonkin
 */
public class ConnectTuple {
    private List listeners;
    private FlowProcessor flowProcessor;
    
    public ConnectTuple() {
        listeners = new LinkedList();
    }
    
    public void addServerLinkListener(ServerLinkListener l){
        listeners.add(l);
    }
    
    public void removeServerLinkListener(ServerLinkListener l){
        listeners.remove(l);
    }
    
    public void fireConnect(FlowServer serv){
        Iterator it = listeners.iterator();
        while (it.hasNext()) {
            ServerLinkListener l = (ServerLinkListener) it.next();
            l.connectedTo(serv);
        }
    }
    
    public void fireDisconnect(FlowServer serv){
        Iterator it = listeners.iterator();
        while (it.hasNext()) {
            ServerLinkListener l = (ServerLinkListener) it.next();
            l.disconnectedFrom(serv);
        }
    }

    public FlowProcessor getFlowProcessor() {
        return flowProcessor;
    }

    public void setFlowProcessor(FlowProcessor flowProcessor) {
        this.flowProcessor = flowProcessor;
    }
}