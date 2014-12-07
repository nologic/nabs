/*
 * NetFlow.java
 *
 * Created on August 28, 2006, 11:37 PM
 */

package eunomia.receptor.module.netFlow;

import com.vivic.eunomia.module.Flow;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 *
 * @author Mikhail Sosonkin
 */
public class NetFlow implements Flow {
    private PacketHeader header;
    private NetFlowTemplate template;
    
    //buffering
    private long time;
    private long srcIp;
    private long dstIp;
    private int srcPort;
    private int dstPort;
    
    public NetFlow() {
    }

    public long getTime() {
        return time;
    }

    public long getSourceIP() {
        return srcIp;
    }

    public long getDestinationIP() {
        return dstIp;
    }

    public int getSourcePort() {
        return srcPort;
    }

    public int getDestinationPort() {
        return dstPort;
    }

    public Object getSpecificInfo(Object format) {
        return null;
    }

    public void writeToDataStream(DataOutputStream dout) throws IOException {
    }

    public void readFromByteBuffer(ByteBuffer buffer) {
    }

    public PacketHeader getHeader() {
        return header;
    }

    public void setHeader(PacketHeader header) {
        this.header = header;
        time = header.getUnixTime();
    }

    public NetFlowTemplate getTemplate() {
        return template;
    }

    public void setTemplate(NetFlowTemplate template) {
        this.template = template;
        
        srcIp = template.getIntByType(TypeConst.IPV4_SRC_ADDR);
        dstIp = template.getIntByType(TypeConst.IPV4_DST_ADDR);
        srcPort = template.getShortByType(TypeConst.L4_SRC_PORT) & 0xFFFF;
        dstPort = template.getShortByType(TypeConst.L4_DST_PORT) & 0xFFFF;
    }

    public int getSize() {
        return 0;
    }
    
}
