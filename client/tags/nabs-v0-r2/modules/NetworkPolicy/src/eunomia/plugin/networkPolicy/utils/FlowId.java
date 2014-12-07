/*
 * FlowId.java
 *
 * Created on December 16, 2006, 12:26 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package eunomia.plugin.networkPolicy.utils;

import eunomia.flow.Flow;

/**
 *
 * @author kulesh
 */
public class FlowId {
    
    private long sourceIP;
    private long destinationIP;
    private int  sourcePort;
    private int destinationPort;

    /** Creates a new instance of FlowId */
    public FlowId() {
    }

    public void initializeFlowId(Flow f){
        destinationIP=f.getDestinationIP();
        sourceIP= f.getSourceIP();
        destinationPort= f.getDestinationPort();
        sourcePort= f.getSourcePort();
    }
    
    public long getSourceIP() {
        return sourceIP;
    }

    public void setSourceIP(long sourceIP) {
        this.sourceIP = sourceIP;
    }

    public long getDestinationIP() {
        return destinationIP;
    }

    public void setDestinationIP(long destinationIP) {
        this.destinationIP = destinationIP;
    }

    public int getSourcePort() {
        return sourcePort;
    }

    public void setSourcePort(int sourcePort) {
        this.sourcePort = sourcePort;
    }

    public int getDestinationPort() {
        return destinationPort;
    }

    public void setDestinationPort(int destinationPort) {
        this.destinationPort = destinationPort;
    }
    
    public boolean equals(Object o){
        FlowId f= (FlowId) o;
        return ((sourceIP == f.sourceIP) && (destinationIP == f.destinationIP) && 
                (sourcePort == f.sourcePort) && (destinationPort == f.destinationPort));
    }
    
    public int hashCode(){
        //TODO: need to do hashcode in a better way. (See eunomia.utils for examples)
        return ((int) (sourceIP ^ sourcePort));
    }
    
    public String toString(){
        return (Long.toString(sourceIP) + ":" + Integer.toString(sourcePort) + " --> " +
                Long.toString(destinationIP) + ":" + Integer.toString(destinationPort));
    }
}