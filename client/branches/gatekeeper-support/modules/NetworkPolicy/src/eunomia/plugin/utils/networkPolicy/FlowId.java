/*
 * FlowId.java
 *
 * Created on December 16, 2006, 12:26 PM
 *
 */

package eunomia.plugin.utils.networkPolicy;

import com.vivic.eunomia.module.Flow;
import eunomia.receptor.module.NABFlow.NABFlow;
import eunomia.util.Util;
import java.util.Arrays;

/**
 *
 * @author kulesh
 */
public class FlowId {
    private long sourceIP;
    private long destinationIP;
    private int sourcePort;
    private int destinationPort;

    private long startTime;
    private long lastUpdate;
    private long bytes;
    private long[] byte_types;

    /** Creates a new instance of FlowId */
    public FlowId() {
        this(0L, 0L, 0, 0);
    }

    public FlowId(long sip, long dip, int sp, int dp) {
        byte_types = new long[NABFlow.NUM_TYPES];
        destinationIP = dip;
        sourceIP = sip;
        destinationPort = dp;
        sourcePort = sp;
        lastUpdate = -1;
    }

    public void initializeFlowId(Flow f){
        destinationIP = f.getDestinationIP();
        sourceIP = f.getSourceIP();
        destinationPort = f.getDestinationPort();
        sourcePort = f.getSourcePort();
    }
    
    public void setAll(long sip, long dip, int sp, int dp) {
        Arrays.fill(byte_types, 0);
        bytes = 0;
        destinationIP = dip;
        sourceIP = sip;
        destinationPort = dp;
        sourcePort = sp;
        lastUpdate = -1;
    }
    
    public Object clone() {
        FlowId id = new FlowId();
        
        id.sourceIP = sourceIP;
        id.destinationIP = destinationIP;
        id.sourcePort = sourcePort;
        id.destinationPort = destinationPort;

        id.startTime = startTime;
        id.lastUpdate = lastUpdate;
        id.bytes = bytes;
        id.byte_types = Arrays.copyOfRange(byte_types, 0, byte_types.length);
        
        return id;
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
    
    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(long lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public long getBytes() {
        return bytes;
    }
    
    public long getBytes(boolean[] list) {
        long ret = 0;
        long[] b = byte_types;
        
        for(int i = b.length - 1; i != -1; --i) {
            ret += (list[i]?b[i]:0L);
        }
        
        return ret;
    }

    public void resetBytes() {
        this.bytes = 0;
        Arrays.fill(byte_types, 0);
    }
    
    public void incrementBytes(long bytes, int type){
        this.bytes += bytes;
        byte_types[type] += bytes;
    }
    
    public long[] getByteTypes() {
        return byte_types;
    }
    
    public void setBytes(long tot, long[] types) {
        bytes = tot;
        System.arraycopy(types, 0, byte_types, 0, byte_types.length);
    }
    
    public boolean equals(Object o){
        FlowId f = (FlowId) o;
        return ((sourceIP == f.sourceIP) && (destinationIP == f.destinationIP) && 
                (sourcePort == f.sourcePort) && (destinationPort == f.destinationPort));
    }
    
    public int hashCode(){
        //This is how bigInteger class does it.
        int hashCode = 0;
        
        hashCode = (int)(31 * hashCode + sourceIP & 0xFFFFFFFFL);
        hashCode = (int)(31 * hashCode + destinationIP & 0xFFFFFFFFL);
        hashCode = (int)(31 * hashCode + sourcePort & 0xFFFFFFFFL);
        hashCode = (int)(31 * hashCode + destinationPort & 0xFFFFFFFFL);
        
        return hashCode;
    }
    
    public String toString(){
        if(destinationIP == 0) {
            return Util.ipToString(sourceIP);
        }
        
        return (Util.ipToString(sourceIP) + ":" + Integer.toString(sourcePort) + " --> " +
                Util.ipToString(destinationIP) + ":" + Integer.toString(destinationPort));
    }
}