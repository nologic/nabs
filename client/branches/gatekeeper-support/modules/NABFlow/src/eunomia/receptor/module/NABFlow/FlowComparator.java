/*
 * FlowComparator.java
 *
 * Created on July 6, 2005, 7:40 PM
 *
 */

package eunomia.receptor.module.NABFlow;

import com.vivic.eunomia.module.Flow;

/**
 * This class used to preform comparrisons between two module flow objects. The 
 * current implementation can be used for any flow object because it only utilizes
 * the generel information. However, it is designed compating specific information.
 * @author Mikhail Sosonkin
 */

public class FlowComparator {
    /**
     * If set then only the source IP's of the flows are compared.
     */
    private boolean srcIp;
    
    /**
     * if set then only the destination IP's of the flows are compared.
     */
    private boolean dstIp;
    
    /**
     * if set then only the source ports of the flows are compared.
     */
    private boolean srcPort;
    
    /**
     * if set then only the destination ports of the flows are compared.
     */
    private boolean dstPort;
    
    /**
     * If set then only the IP's of the two flows are compared. The comparisson is
     * done is such a way that it is direction independent. So, any flows between
     * hosts A and B are equivalent. If this flag is set then none of the others are
     * affective.
     */
    private boolean interHost;
    
    private boolean singleHost;
    /**
     * If set then the flows are compared on connection basis. This means that any 
     * flows on the same TCP connection (or UDP equivalent, by looking strictly at
     * the ports and IP's. NABFlows do not make the destintion) are the same. If this
     * flag is set then none of the others (with 'interHost' as exception) are 
     * checked.
     */
    private boolean connection;
    
    /**
     * Constructor that creates a comparator for only source IP comparisson.
     */
    public FlowComparator() {
        this(true, false, false, false);
    }
    
    /**
     * Allows the developer to set the flags at the constructor level.
     * @param sIp Source IP
     * @param dIp Destination IP
     * @param sPort Source Port
     * @param dPort Destination port
     */
    public FlowComparator(boolean sIp, boolean dIp, boolean sPort, boolean dPort){
        srcIp = sIp;
        dstIp = dIp;
        srcPort = sPort;
        dstPort = dPort;
    }
    
    /**
     * Order should not matter
     * @param f1 Flow 1
     * @param f2 Flow 2
     * @return true is the flows are equal, false otherwise
     */
    public boolean areEqual(Flow f1, Flow f2){
        boolean rsIp = true;
        boolean rdIp = true;
        boolean rsPort = true;
        boolean rdPort = true;
        
        if(singleHost) {
            // we just care about the source IP. LC implementation will double count 
            // to make sure both hosts are countes
            return f1.getSourceIP() == f2.getSourceIP();
        }
        
        if(interHost){
            // ignore the other flags, they won't make sense
            return (f1.getSourceIP()      == f2.getSourceIP() && 
                    f1.getDestinationIP() == f2.getDestinationIP()) ||
                   (f1.getDestinationIP() == f2.getSourceIP() && 
                    f1.getSourceIP()      == f2.getDestinationIP());
        }
        
        if(connection){
            // ignore the other flags, they won't make sense
            return (f1.getSourceIP()        == f2.getSourceIP() && 
                    f1.getDestinationIP()   == f2.getDestinationIP() && 
                    f1.getSourcePort()      == f2.getSourcePort() && 
                    f1.getDestinationPort() == f2.getDestinationPort()) ||
                   (f1.getDestinationIP()   == f2.getSourceIP() && 
                    f1.getSourceIP()        == f2.getDestinationIP() &&
                    f1.getSourcePort()      == f2.getDestinationPort() && 
                    f1.getDestinationPort() == f2.getSourcePort());
        }

        if(srcIp){
            rsIp = (f1.getSourceIP() == f2.getSourceIP());
        }
        
        if(dstIp){
            rdIp = (f1.getDestinationIP() == f2.getDestinationIP());
        }
        
        if(srcPort){
            rsPort = (f1.getSourcePort() == f2.getSourcePort());
        }
        
        if(dstPort){
            rdPort = (f1.getDestinationPort() == f2.getDestinationPort());
        }
        
        return rsIp && rdIp && rsPort && rdPort;
    }

    /**
     * A helper method that resets all the flags to false.
     */
    public void allFalse(){
        srcIp = false;
        dstIp = false;
        srcPort = false;
        dstPort = false;
        interHost = false;
        connection = false;
        singleHost = false;
    }
    
    // Setters and getters for the flags.
    public boolean isSrcIp() {
        return srcIp;
    }

    public void setSrcIp(boolean srcIp) {
        this.srcIp = srcIp;
    }

    public boolean isDstIp() {
        return dstIp;
    }

    public void setDstIp(boolean dstIp) {
        this.dstIp = dstIp;
    }

    public boolean isSrcPort() {
        return srcPort;
    }

    public void setSrcPort(boolean srcPort) {
        this.srcPort = srcPort;
    }

    public boolean isDstPort() {
        return dstPort;
    }

    public void setDstPort(boolean dstPort) {
        this.dstPort = dstPort;
    }

    public boolean isInterHost() {
        return interHost;
    }

    public void setInterHost(boolean interHost) {
        this.interHost = interHost;
    }

    public boolean isConnection() {
        return connection;
    }

    public void setConnection(boolean connection) {
        this.connection = connection;
    }

    public boolean isSingleHost() {
        return singleHost;
    }

    public void setSingleHost(boolean singleHost) {
        this.singleHost = singleHost;
    }
}