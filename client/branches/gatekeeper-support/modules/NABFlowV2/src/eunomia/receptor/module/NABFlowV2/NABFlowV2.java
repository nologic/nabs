/*
 * Flow.java
 *
 * Created on June 1, 2005, 12:53 PM
 */

package eunomia.receptor.module.NABFlowV2;

import com.vivic.eunomia.module.Flow;
import eunomia.util.Util;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * This is the class that is passed around to all modules interesting in processing
 * it. The mothods within it must be highly optimized because they will dominate
 * the execution time. If they are not optimized then it has a good chance of
 * slowing down the whole system.
 * @author Mikhail Sosonkin
 */
public class NABFlowV2 implements Flow {
    public static final int PROTOCOL_TCP = 6;
    public static final int PROTOCOL_UDP = 17;

    /**
     * Each flow is 128 bytes of data on the network.
     */
    
    public static final int FLOW_BYTE_SIZE = 128;
    
    /**
     * There are 8 different data types
     */
    public static final int NUM_TYPES = 8;
    public static final int MAX_PAYLOAD = 16384;
    public static final int MAX_HISTOGRAM_INDEX = 10;
    
    /**
     * Each type is enumerated.
     */
    public static final int DT_Plain_Text = 0;
    public static final int DT_Image_BMP = 1;
    public static final int DT_Audio_WAV = 2;
    public static final int DT_Compressed = 3;
    public static final int DT_Image_JPG = 4;
    public static final int DT_Audio_MP3 = 5;
    public static final int DT_Video_MPG = 6;
    public static final int DT_Encrypted = 7;
    
    /**
     * A name is given to each type, the index is the number of the type.
     */
    public static final String[] typeNames = {"Plain-Text", "Image-BMP",
    "Audio-WAV", "Compressed", "Image-JPG", "Audio-MP3", "Video-MPG",
    "Encrypted"};
    /**
     * This is just for comfort, sometimes it's needed to itereate though all the
     * names.
     */
    public static final List typeNamesList = Collections.unmodifiableList(Arrays.asList(typeNames));
    
    private long srcIp;
    private long dstIp;
    private int srcPort;
    private int dstPort;
    private byte protocol;
    private short application;  /* Type of Application */
    private short majorVersion;  /* Major version */
    private short minorVersion;  /* Minor version */

    private long startTimeSeconds;
    private long startTimeMicroSeconds;
    private long endTimeSeconds;
    private long endTimeMicroSeconds;
    
    private int[] typeCount; /* content type mutliples of MAX_PAYLOAD */

    private int size;
    private int packets;

    private int max_packet_size;
    private int min_packet_size;
    private int tcp_syns;
    private int tcp_acks;
    private int tcp_fins;
    private int tcp_rsts;
    private int tcp_urgs;
    private int tcp_push;

    private short min_ttl;
    private short max_ttl;
    
    private int overSizedCount;
    private int[] histogram;

    /* to measure slowdown */
    private long synTimeSeconds;
    private long synTimeMicroSeconds;
    private long synackTimeSeconds;
    private long synackTimeMicroSeconds;
    private long finTimeSeconds;
    private long finTimeMicroSeconds;
    private long finackTimeSeconds;
    private long finackTimeMicroSeconds;
    
    /* SQL Create table Sequence
CREATE TABLE flowsV2 (
     src_ip BIGINT, dst_ip BIGINT, src_port SMALLINT UNSIGNED, dest_port SMALLINT UNSIGNED,
     protocol TINYINT UNSIGNED, application SMALLINT UNSIGNED, majorVersion SMALLINT UNSIGNED, minorVersion SMALLINT UNSIGNED,
     startTimeSeconds BIGINT, startTimeMicroSeconds BIGINT, endTimeSeconds BIGINT, endTimeMicroSeconds BIGINT,
     typeCount_0 INT, typeCount_1 INT, typeCount_2 INT, typeCount_3 INT, typeCount_4 INT, typeCount_5 INT, typeCount_6 INT, typeCount_7 INT,
     size INT, packets INT,
     max_packet_size INT, min_packet_size INT, tcp_syns INT, tcp_acks INT, tcp_fins INT, tcp_rsts INT, tcp_urgs INT, tcp_push INT,
     min_ttl TINYINT UNSIGNED, max_ttl TINYINT UNSIGNED, overSizedCount INT, 
     histogram_0 INT, histogram_1 INT, histogram_2 INT, histogram_3 INT, histogram_4 INT, 
     histogram_5 INT, histogram_6 INT, histogram_7 INT, histogram_8 INT, histogram_9 INT,
     synTimeSeconds BIGINT, synTimeMicroSeconds BIGINT, synackTimeSeconds BIGINT, synackTimeMicroSeconds BIGINT, 
     finTimeSeconds BIGINT, finTimeMicroSeconds BIGINT, finackTimeSeconds BIGINT, finackTimeMicroSeconds BIGINT
);
     */
    
    //working buffers
    /**
     * temporary storage buffer for parsing. Also useful when a raw byte array is
     * needed, in those cases it doesn't have to regenerated.
     */
    private byte[] buff;
    /**
     * User for parsing and generating IP's
     */
    private byte[] ipWorkBytes;
    
    /**
     * Creates a flow with default values.
     */
    public NABFlowV2(){
        typeCount = new int[NUM_TYPES];
        histogram = new int[MAX_HISTOGRAM_INDEX];
        // at the moment only IPv4 is supported.
        ipWorkBytes = new byte[4];
        buff = new byte[FLOW_BYTE_SIZE];
        // generally the size does not change, so we set it as the default value.
        //size = 16384;
    }
    
    public long getTime() {
        return (long)((startTimeSeconds * 1000.0) + (startTimeMicroSeconds / 1000.0));
    }
    
    public int getSize(){
        return size;
    }
    
    public void setSize(int size) {
        this.size = size;
    }

    public long getSourceIP(){
        return srcIp;
    }
    
    public void setSourcrIP(long srcIp) {
        this.srcIp = srcIp;
    }
    
    public int getSourcePort(){
        return srcPort;
    }

    public void setSourcePort(int srcPort) {
        this.srcPort = srcPort;
    }

    public long getDestinationIP(){
        return dstIp;
    }
    
    public void setDestinationIp(long dstIp) {
        this.dstIp = dstIp;
    }

    public int getDestinationPort(){
        return dstPort;
    }
    
    public void setDestinationPort(int dstPort) {
        this.dstPort = dstPort;
    }

    public byte getProtocol() {
        return protocol;
    }

    public void setProtocol(byte protocol) {
        this.protocol = protocol;
    }

    public short getApplication() {
        return application;
    }

    public void setApplication(short application) {
        this.application = application;
    }

    public short getMajorVersion() {
        return majorVersion;
    }

    public void setMajorVersion(short majorVersion) {
        this.majorVersion = majorVersion;
    }

    public short getMinorVersion() {
        return minorVersion;
    }

    public void setMinorVersion(short minorVersion) {
        this.minorVersion = minorVersion;
    }

    public long getStartTimeSeconds() {
        return startTimeSeconds;
    }

    public void setStartTimeSeconds(long startTimeSeconds) {
        this.startTimeSeconds = startTimeSeconds;
    }

    public long getStartTimeMicroSeconds() {
        return startTimeMicroSeconds;
    }

    public void setStartTimeMicroSeconds(long startTimeMicroSeconds) {
        this.startTimeMicroSeconds = startTimeMicroSeconds;
    }

    public long getEndTimeSeconds() {
        return endTimeSeconds;
    }

    public void setEndTimeSeconds(long endTimeSeconds) {
        this.endTimeSeconds = endTimeSeconds;
    }

    public long getEndTimeMicroSeconds() {
        return endTimeMicroSeconds;
    }

    public void setEndTimeMicroSeconds(long endTimeMicroSeconds) {
        this.endTimeMicroSeconds = endTimeMicroSeconds;
    }

    public int getTypeCount(int t) {
        return typeCount[t];
    }

    public void setTypeCount(int t, int typeCount) {
        this.typeCount[t] = typeCount;
    }
    
    public int[] getTypeCount() {
        return typeCount;
    }

    public int getPackets() {
        return packets;
    }

    public void setPackets(int packets) {
        this.packets = packets;
    }

    public int getMax_packet_size() {
        return max_packet_size;
    }

    public void setMax_packet_size(int max_packet_size) {
        this.max_packet_size = max_packet_size;
    }

    public int getMin_packet_size() {
        return min_packet_size;
    }

    public void setMin_packet_size(int min_packet_size) {
        this.min_packet_size = min_packet_size;
    }

    public short getMin_ttl() {
        return min_ttl;
    }

    public void setMin_ttl(short min_ttl) {
        this.min_ttl = min_ttl;
    }

    public short getMax_ttl() {
        return max_ttl;
    }

    public void setMax_ttl(short max_ttl) {
        this.max_ttl = max_ttl;
    }

    public int getTcp_syns() {
        return tcp_syns;
    }

    public void setTcp_syns(int tcp_syns) {
        this.tcp_syns = tcp_syns;
    }

    public int getTcp_acks() {
        return tcp_acks;
    }

    public void setTcp_acks(int tcp_acks) {
        this.tcp_acks = tcp_acks;
    }

    public int getTcp_fins() {
        return tcp_fins;
    }

    public void setTcp_fins(int tcp_fins) {
        this.tcp_fins = tcp_fins;
    }

    public int getTcp_rsts() {
        return tcp_rsts;
    }

    public void setTcp_rsts(int tcp_rsts) {
        this.tcp_rsts = tcp_rsts;
    }

    public int getTcp_urgs() {
        return tcp_urgs;
    }

    public void setTcp_urgs(int tcp_urgs) {
        this.tcp_urgs = tcp_urgs;
    }

    public int getTcp_push() {
        return tcp_push;
    }

    public void setTcp_push(int tcp_push) {
        this.tcp_push = tcp_push;
    }

    public long getSynTimeSeconds() {
        return synTimeSeconds;
    }

    public void setSynTimeSeconds(long synTimeSeconds) {
        this.synTimeSeconds = synTimeSeconds;
    }

    public long getSynTimeMicroSeconds() {
        return synTimeMicroSeconds;
    }

    public void setSynTimeMicroSeconds(long synTimeMicroSeconds) {
        this.synTimeMicroSeconds = synTimeMicroSeconds;
    }

    public long getSynackTimeSeconds() {
        return synackTimeSeconds;
    }

    public void setSynackTimeSeconds(long synackTimeSeconds) {
        this.synackTimeSeconds = synackTimeSeconds;
    }

    public long getSynackTimeMicroSeconds() {
        return synackTimeMicroSeconds;
    }

    public void setSynackTimeMicroSeconds(long synackTimeMicroSeconds) {
        this.synackTimeMicroSeconds = synackTimeMicroSeconds;
    }

    public long getFinTimeSeconds() {
        return finTimeSeconds;
    }

    public void setFinTimeSeconds(long finTimeSeconds) {
        this.finTimeSeconds = finTimeSeconds;
    }

    public long getFinTimeMicroSeconds() {
        return finTimeMicroSeconds;
    }

    public void setFinTimeMicroSeconds(long finTimeMicroSeconds) {
        this.finTimeMicroSeconds = finTimeMicroSeconds;
    }

    public long getFinackTimeSeconds() {
        return finackTimeSeconds;
    }

    public void setFinackTimeSeconds(long finackTimeSeconds) {
        this.finackTimeSeconds = finackTimeSeconds;
    }

    public long getFinackTimeMicroSeconds() {
        return finackTimeMicroSeconds;
    }

    public void setFinackTimeMicroSeconds(long finackTimeMicroSeconds) {
        this.finackTimeMicroSeconds = finackTimeMicroSeconds;
    }
    
    public int getOverSizedCount() {
        return overSizedCount;
    }

    public void setOverSizedCount(int overSizedCount) {
        this.overSizedCount = overSizedCount;
    }

    public int getHistogram(int index) {
        return histogram[index];
    }

    public void setHistogram(int index, int value) {
        this.histogram[index] = value;
    }
    
    /**
     * Used to get module specific inforation. For this module only the SQL string is
     * returned. The string contains all the fields in order. Currently this method is
     * used by the database collector.
     * @param format Possible parameters for retreiving only needed information.
     * @return the SQL String.
     */
    
    private String getSQLString() {
        StringBuilder b = new StringBuilder();
        
        b.append("(");
        b.append(srcIp).append(",");
        b.append(dstIp).append(",");
        b.append(srcPort).append(",");
        b.append(dstPort).append(",");
        b.append(protocol).append(",");
        
        b.append(application).append(",");
        b.append(majorVersion).append(",");
        b.append(minorVersion).append(",");
        
        b.append(startTimeSeconds).append(",");
        b.append(startTimeMicroSeconds).append(",");
        b.append(endTimeSeconds).append(",");
        b.append(endTimeMicroSeconds).append(",");
        
        for (int i = 0; i < NUM_TYPES; i++) {
            b.append(typeCount[i]).append(",");
        }
        
        b.append(size).append(",");
        b.append(packets).append(",");
        
        b.append(max_packet_size).append(",");
        b.append(min_packet_size).append(",");

        b.append(tcp_syns).append(",");
        b.append(tcp_acks).append(",");
        b.append(tcp_fins).append(",");
        b.append(tcp_rsts).append(",");
        b.append(tcp_urgs).append(",");
        b.append(tcp_push).append(",");

        b.append(min_ttl).append(",");
        b.append(max_ttl).append(",");
        
        b.append(overSizedCount).append(",");
        for (int i = 0; i < MAX_HISTOGRAM_INDEX; i++) {
            b.append(histogram[i]).append(",");
        }

        b.append(synTimeSeconds).append(",");
        b.append(synTimeMicroSeconds).append(",");
        b.append(synackTimeSeconds).append(",");
        b.append(synackTimeMicroSeconds).append(",");
        b.append(finTimeSeconds).append(",");
        b.append(finTimeMicroSeconds).append(",");
        b.append(finackTimeSeconds).append(",");
        b.append(finackTimeMicroSeconds).append(")");

        return b.toString();
    }
    
    public Object getSpecificInfo(Object format){
        if(format == null) {
            return getSQLString();
        }
        
        return null;
    }
    
    public void takeFrom(NABFlowV2 flow) {
        srcIp = flow.srcIp;
        dstIp = flow.dstIp;
        srcPort = flow.srcPort;
        dstPort = flow.dstPort;
        protocol = flow.protocol;
        
        application = flow.application;
        majorVersion = flow.majorVersion;
        minorVersion = flow.minorVersion;
        
        startTimeSeconds = flow.startTimeSeconds;
        startTimeMicroSeconds = flow.startTimeMicroSeconds;
        endTimeSeconds = flow.endTimeSeconds;
        endTimeMicroSeconds = flow.endTimeMicroSeconds;
        
        System.arraycopy(flow.typeCount, 0, typeCount, 0, NUM_TYPES);

        size = flow.size;
        packets = flow.packets;
        
        max_packet_size = flow.max_packet_size;
        min_packet_size = flow.min_packet_size;

        tcp_syns = flow.tcp_syns;
        tcp_acks = flow.tcp_acks;
        tcp_fins = flow.tcp_fins;
        tcp_rsts = flow.tcp_rsts;
        tcp_urgs = flow.tcp_urgs;
        tcp_push = flow.tcp_push;

        min_ttl = flow.min_ttl;
        max_ttl = flow.max_ttl;
        
        overSizedCount = flow.overSizedCount;
        System.arraycopy(flow.histogram, 0, histogram, 0, MAX_HISTOGRAM_INDEX);

        synTimeSeconds = flow.synTimeSeconds;
        synTimeMicroSeconds = flow.synTimeMicroSeconds;
        synackTimeSeconds = flow.synackTimeSeconds;
        synackTimeMicroSeconds = flow.synackTimeMicroSeconds;
        finTimeSeconds = flow.finTimeSeconds;
        finTimeMicroSeconds = flow.finackTimeMicroSeconds;
        finackTimeSeconds = flow.finackTimeSeconds;
        finackTimeMicroSeconds = flow.finackTimeMicroSeconds;
    }
    
    /**
     * Interface method for parsing data from the network. This is the method called
     * by the middlware.
     * @param bb Buffer that will contain at least one flow.
     */
    public void readFromByteBuffer(ByteBuffer bb){
        ByteOrder order = bb.order();
        
        bb.order(ByteOrder.BIG_ENDIAN);
        // FLOW ID
        srcIp = bb.getInt() & 0xFFFFFFFFL;
        dstIp = bb.getInt() & 0xFFFFFFFFL;
        srcPort = bb.getShort() & 0x0000FFFF;
        dstPort = bb.getShort() & 0x0000FFFF;
        protocol = bb.get();
        
        bb.order(ByteOrder.LITTLE_ENDIAN);
        application = (short)(bb.get() & 0x00FF);
        majorVersion = (short)(bb.get() & 0x00FF);
        minorVersion = (short)(bb.get() & 0x00FF);
        
        startTimeSeconds = bb.getInt() & 0xFFFFFFFFL;
        startTimeMicroSeconds = bb.getInt() & 0xFFFFFFFFL;
        endTimeSeconds = bb.getInt() & 0xFFFFFFFFL;
        endTimeMicroSeconds = bb.getInt() & 0xFFFFFFFFL;
        
        for (int i = 0; i < NUM_TYPES; i++) {
            typeCount[i] = bb.getShort() & 0x0000FFFF;
        }
        
        size = bb.getInt();
        packets = bb.getInt();
        
        max_packet_size = bb.getShort() & 0x0000FFFF;
        min_packet_size = bb.getShort() & 0x0000FFFF;

        tcp_syns = bb.getShort() & 0x0000FFFF;
        tcp_acks = bb.getShort() & 0x0000FFFF;
        tcp_fins = bb.getShort() & 0x0000FFFF;
        tcp_rsts = bb.getShort() & 0x0000FFFF;
        tcp_urgs = bb.getShort() & 0x0000FFFF;
        tcp_push = bb.getShort() & 0x0000FFFF;

        min_ttl = (short)(bb.get() & 0x00FF);
        max_ttl = (short)(bb.get() & 0x00FF);
        
        overSizedCount = bb.getShort() & 0x0000FFFF;
        for (int i = 0; i < MAX_HISTOGRAM_INDEX; i++) {
            histogram[i] = bb.getShort() & 0x0000FFFF;
        }

        synTimeSeconds = bb.getInt() & 0xFFFFFFFFL;
        synTimeMicroSeconds = bb.getInt() & 0xFFFFFFFFL;
        synackTimeSeconds = bb.getInt() & 0xFFFFFFFFL;
        synackTimeMicroSeconds = bb.getInt() & 0xFFFFFFFFL;
        finTimeSeconds = bb.getInt() & 0xFFFFFFFFL;
        finTimeMicroSeconds = bb.getInt() & 0xFFFFFFFFL;
        finackTimeSeconds = bb.getInt() & 0xFFFFFFFFL;
        finackTimeMicroSeconds = bb.getInt() & 0xFFFFFFFFL;
        bb.order(order);
    }
    
    public String toString() {
        StringBuilder b = new StringBuilder();
        
        Util.getTimeStamp(b, getTime(), true, true);
        b.append("\t");
        
        Util.ipToString(b, srcIp);
        b.append(":");
        b.append(srcPort);
        b.append(" -> ");
        Util.ipToString(b, dstIp);
        b.append(":");
        b.append(dstPort);
        b.append(" [");
        b.append(protocol);
        b.append("]");
        b.append("\t");
        
        b.append("(pkts: ");
        b.append(packets);
        b.append(" bytes: ");
        b.append(size);

        b.append(" size: ");
        b.append(getMax_packet_size());
        b.append("|");
        b.append(getMin_packet_size());
        b.append(" ttl:");
        b.append(getMax_ttl());
        b.append("|");
        b.append(getMin_ttl());
        b.append(" overSized: ");
        b.append(getOverSizedCount());
        b.append(" acks: ");
        b.append(getTcp_acks());
        b.append(" syns: ");
        b.append(getTcp_syns());
        b.append(" histogram: ");
        
        for(int i=0; i < MAX_HISTOGRAM_INDEX; ++i)
            b.append(getHistogram(i) + "|");
        
        b.append(" content: ");
        
        for(int i=0; i < NUM_TYPES; ++i)
            b.append(getTypeCount(i) + "|");
        
        b.append(")");
        
        return b.toString();
    }

    public void writeToDataStream(DataOutputStream dout) throws IOException {
        ByteBuffer b = ByteBuffer.allocate(FLOW_BYTE_SIZE);
        
        b.order(ByteOrder.BIG_ENDIAN);
        b.putInt((int)srcIp);
        b.putInt((int)dstIp);
        b.putShort((short)srcPort);
        b.putShort((short)dstIp);
        b.put(protocol);
        
        b.order(ByteOrder.LITTLE_ENDIAN);
        b.put((byte)application);
        b.put((byte)majorVersion);
        b.put((byte)minorVersion);
        b.putInt((int)startTimeSeconds);
        b.putInt((int)startTimeMicroSeconds);
        b.putInt((int)endTimeSeconds);
        b.putInt((int)endTimeMicroSeconds);
        
        for (int i = 0; i < NUM_TYPES; i++) {
            b.putShort((short)typeCount[i]);
        }
        
        b.putInt((int)size);
        b.putInt((int)packets);
        b.putShort((short)max_packet_size);
        b.putShort((short)min_packet_size);

        b.putShort((short)tcp_syns);
        b.putShort((short)tcp_acks);
        b.putShort((short)tcp_fins);
        b.putShort((short)tcp_rsts);
        b.putShort((short)tcp_urgs);
        b.putShort((short)tcp_push);
        
        b.put((byte)min_ttl);
        b.put((byte)max_ttl);
        
        b.putShort((short)overSizedCount);
        for (int i = 0; i < MAX_HISTOGRAM_INDEX; i++) {
            b.putShort((short)histogram[i]);
        }
        
        b.putInt((int)synTimeSeconds);
        b.putInt((int)synTimeMicroSeconds);
        b.putInt((int)synackTimeSeconds);
        b.putInt((int)synackTimeMicroSeconds);
        b.putInt((int)finTimeSeconds);
        b.putInt((int)finTimeMicroSeconds);
        b.putInt((int)finackTimeSeconds);
        b.putInt((int)finackTimeMicroSeconds);
        
        dout.write(b.array());
    }
}