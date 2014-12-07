/*
 * LossyFlow.java
 *
 * Created on April 28, 2007, 2:14 PM
 *
 */

package eunomia.plugin.alg;

import com.vivic.eunomia.module.Flow;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 *
 * @author Mikhail Sosonkin
 */
public class LossyFlow implements Flow {
    private long srcIp;
    private long dstIp;
    private int srcPort;
    private int dstPort;
    
    public LossyFlow() {
        this(0, 0, 0, 0);
    }
    
    public LossyFlow(long sip, long dip, int sp, int dp) {
        assign(sip, dip, sp, dp);
    }
    
    public void assign(long sip, long dip, int sp, int dp) {
        srcIp = sip;
        dstIp = dip;
        srcPort = sp;
        dstPort = dp;
    }
    
    public void assign(LossyFlow lf) {
        assign(lf.srcIp, lf.dstIp, lf.srcPort, lf.dstPort);
    }
    
    public void assign(Flow flow) {
        assign(flow.getSourceIP(), flow.getDestinationIP(), flow.getSourcePort(), flow.getDestinationPort());
    }

    public long getSourceIP() {
        return srcIp;
    }

    public void setSourceIP(long srcIp) {
        this.srcIp = srcIp;
    }

    public long getDestinationIP() {
        return dstIp;
    }

    public void setDestinationIP(long dstIp) {
        this.dstIp = dstIp;
    }

    public int getSourcePort() {
        return srcPort;
    }

    public void setSourcePort(int srcPort) {
        this.srcPort = srcPort;
    }

    public int getDestinationPort() {
        return dstPort;
    }

    public void setDestinationPort(int dstPort) {
        this.dstPort = dstPort;
    }

    public long getTime() {
        return 0;
    }

    public Object getSpecificInfo(Object format) {
        return null;
    }

    public void writeToDataStream(DataOutputStream dout) throws IOException {
    }

    public void readFromByteBuffer(ByteBuffer buffer) {
    }

    public int getSize() {
        return 0;
    }
}