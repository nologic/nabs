/*
 * FlowComparator.java
 *
 * Created on July 6, 2005, 7:40 PM
 *
 */

package eunomia.core.data.flow;

import java.util.Comparator;


/**
 *
 * @author Mikhail Sosonkin
 */

public class FlowComparator {
    private boolean srcIp;
    private boolean dstIp;
    private boolean srcPort;
    private boolean dstPort;
    private boolean interHost;
    private boolean connection;
    
    public FlowComparator() {
        this(true, false, false, false);
    }
    
    public FlowComparator(boolean sIp, boolean dIp, boolean sPort, boolean dPort){
        srcIp = sIp;
        dstIp = dIp;
        srcPort = sPort;
        dstPort = dPort;
    }
    
    public boolean areEqual(Flow f1, Flow f2){
        boolean rsIp = true;
        boolean rdIp = true;
        boolean rsPort = true;
        boolean rdPort = true;
        
        if(interHost){
            return (f1.getSourceIp()      == f2.getSourceIp() && 
                    f1.getDestinationIp() == f2.getDestinationIp()) ||
                   (f1.getDestinationIp() == f2.getSourceIp() && 
                    f1.getSourceIp()      == f2.getDestinationIp());
        }
        
        if(connection){
            return (f1.getSourceIp()        == f2.getSourceIp() && 
                    f1.getDestinationIp()   == f2.getDestinationIp() && 
                    f1.getSourcePort()      == f2.getSourcePort() && 
                    f1.getDestinationPort() == f2.getDestinationPort()) ||
                   (f1.getDestinationIp()   == f2.getSourceIp() && 
                    f1.getSourceIp()        == f2.getDestinationIp() &&
                    f1.getSourcePort()      == f2.getDestinationPort() && 
                    f1.getDestinationPort() == f2.getSourcePort());
        }

        if(srcIp){
            rsIp = (f1.getSourceIp() == f2.getSourceIp());
        }
        
        if(dstIp){
            rdIp = (f1.getDestinationIp() == f2.getDestinationIp());
        }
        
        if(srcPort){
            rsPort = (f1.getSourcePort() == f2.getSourcePort());
        }
        
        if(dstPort){
            rdPort = (f1.getDestinationPort() == f2.getDestinationPort());
        }
        
        return rsIp && rdIp && rsPort && rdPort;
    }

    public void allFalse(){
        srcIp = false;
        dstIp = false;
        srcPort = false;
        dstPort = false;
        interHost = false;
        connection = false;
    }
    
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
}