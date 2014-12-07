/*
 * Flow.java
 *
 * Created on June 1, 2005, 12:53 PM
 */

package eunomia.receptor.module.NEOFlow;

import com.vivic.eunomia.module.flow.Flow;
import com.vivic.eunomia.sys.util.Util;
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
public class NEOFlow implements Flow {
    public static final int TEMPLATE_ID = 1;
    
    public static final int PROTOCOL_TCP = 6;
    public static final int PROTOCOL_UDP = 17;

    /**
     * Each flow is 128 bytes of data on the network.
     */
    
    public static final int FLOW_BYTE_SIZE = 183;
    
    /**
     * There are 8 different data types
     */
    public static final int NUM_TYPES = 8;
    public static final int MAX_PAYLOAD = 16384;
    public static final int MAX_HISTOGRAM_INDEX = 8;
    public static final int NUM_TCP_FLAGS = 6;
    
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
     * Each Tcp flag
     */
    public static final int TCP_URG = 0;
    public static final int TCP_ACK = 1;
    public static final int TCP_PSH = 2;
    public static final int TCP_RST = 3;
    public static final int TCP_SYN = 4;
    public static final int TCP_FIN = 5;
    
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

    private TimeStamp startTime;
    private TimeStamp endTime;
    private TimeStamp minInterArrivalTime;
    private TimeStamp maxInterArrivalTime;
    private TimeStamp firstSYNpackTime;
    private TimeStamp firstSYNACKpackTime;
    private TimeStamp firstACKpackTime;
 
    private int[] typeCount; /* content type mutliples of MAX_PAYLOAD */

    private int size;
    private int packets;

    private int max_packet_size;
    private int min_packet_size;
    
    private int[] tcp_flags;

    private short min_ttl;
    private short max_ttl;
    
    private int[] histogram;

    private int fragCount;
    private byte tos;
           
    /**
     * Creates a flow with default values.
     */
    public NEOFlow(){
        typeCount = new int[NUM_TYPES];
        histogram = new int[MAX_HISTOGRAM_INDEX];
        tcp_flags = new int[NUM_TCP_FLAGS];
    }
    
    public long getTime() {
        return getStartTime().getMilliSeconds();
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
    
    public int getTcpFlag(int flag) {
        return tcp_flags[flag];
    }
    
    public void setTcpFlag(int flag, int count) {
        tcp_flags[flag] = count;
    }
    
    public int[] getHistogram() {
        return histogram;
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
        
        /*b.append(startTimeSeconds).append(",");
        b.append(startTimeMicroSeconds).append(",");
        b.append(endTimeSeconds).append(",");
        b.append(endTimeMicroSeconds).append(",");*/
        
        for (int i = 0; i < NUM_TYPES; i++) {
            b.append(typeCount[i]).append(",");
        }
        
        b.append(size).append(",");
        b.append(packets).append(",");
        
        b.append(max_packet_size).append(",");
        b.append(min_packet_size).append(",");

        for(int i = 0; i < NUM_TCP_FLAGS; i++) {
            b.append(tcp_flags[i]).append(",");
        }

        b.append(min_ttl).append(",");
        b.append(max_ttl).append(",");
        
        for (int i = 0; i < MAX_HISTOGRAM_INDEX; i++) {
            b.append(histogram[i]).append(",");
        }

/*        b.append(synTimeSeconds).append(",");
        b.append(synTimeMicroSeconds).append(",");
        b.append(synackTimeSeconds).append(",");
        b.append(synackTimeMicroSeconds).append(",");
        b.append(finTimeSeconds).append(",");
        b.append(finTimeMicroSeconds).append(",");
        b.append(finackTimeSeconds).append(",");
        b.append(finackTimeMicroSeconds).append(")");*/

        return b.toString();
    }
    
    public Object getSpecificInfo(Object format){
        if(format == null) {
            return getSQLString();
        }
        
        return null;
    }
    
    public void takeFrom(NEOFlow flow) {
        srcIp = flow.srcIp;
        dstIp = flow.dstIp;
        srcPort = flow.srcPort;
        dstPort = flow.dstPort;
        protocol = flow.protocol;
                
/*        startTimeSeconds = flow.startTimeSeconds;
        startTimeMicroSeconds = flow.startTimeMicroSeconds;
        endTimeSeconds = flow.endTimeSeconds;
        endTimeMicroSeconds = flow.endTimeMicroSeconds;*/
        
        System.arraycopy(flow.typeCount, 0, typeCount, 0, NUM_TYPES);

        size = flow.size;
        packets = flow.packets;
        
        max_packet_size = flow.max_packet_size;
        min_packet_size = flow.min_packet_size;

        System.arraycopy(flow.tcp_flags, 0, tcp_flags, 0, NUM_TCP_FLAGS);

        min_ttl = flow.min_ttl;
        max_ttl = flow.max_ttl;
        
        System.arraycopy(flow.histogram, 0, histogram, 0, MAX_HISTOGRAM_INDEX);

/*        synTimeSeconds = flow.synTimeSeconds;
        synTimeMicroSeconds = flow.synTimeMicroSeconds;
        synackTimeSeconds = flow.synackTimeSeconds;
        synackTimeMicroSeconds = flow.synackTimeMicroSeconds;
        finTimeSeconds = flow.finTimeSeconds;
        finTimeMicroSeconds = flow.finackTimeMicroSeconds;
        finackTimeSeconds = flow.finackTimeSeconds;
        finackTimeMicroSeconds = flow.finackTimeMicroSeconds;*/
    }
    
    /**
     * Interface method for parsing data from the network. This is the method called
     * by the middlware.
     * @param bb Buffer that will contain at least one flow.
     */
    public void readFromByteBuffer(ByteBuffer bb){
        ByteOrder order = bb.order();
        
        bb.order(ByteOrder.BIG_ENDIAN);
        
        //read Flow Header
        byte template = bb.get();
        int bodyLen = bb.getShort() & 0x0000FFFF;
        protocol = bb.get();

        srcPort = bb.getShort() & 0x0000FFFF;
        dstPort = bb.getShort() & 0x0000FFFF;
        srcIp = bb.getInt() & 0xFFFFFFFFL;
        dstIp = bb.getInt() & 0xFFFFFFFFL;
        
        startTime = TimeStamp.readTimeStamp(startTime, bb);
        endTime = TimeStamp.readTimeStamp(endTime, bb);

        // NEO Flow Specific
        packets = bb.getInt();
        size = bb.getInt();

        min_packet_size = bb.getInt();
        max_packet_size = bb.getInt();

        for (int i = 0; i < MAX_HISTOGRAM_INDEX; i++) {
            histogram[i] = bb.getInt();
        }
        
        minInterArrivalTime = TimeStamp.readTimeStamp(minInterArrivalTime, bb);
        maxInterArrivalTime = TimeStamp.readTimeStamp(maxInterArrivalTime, bb);
        
        tos = bb.get();
        fragCount = bb.getInt();
        min_ttl = (short)(bb.get() & 0x00FF);
        max_ttl = (short)(bb.get() & 0x00FF);
        
        for(int i = 0; i < NUM_TCP_FLAGS; i++) {
            tcp_flags[i] = bb.getInt();
        }
        
        firstSYNpackTime = TimeStamp.readTimeStamp(firstSYNpackTime, bb);
        firstSYNACKpackTime = TimeStamp.readTimeStamp(firstSYNACKpackTime, bb);
        firstACKpackTime = TimeStamp.readTimeStamp(firstACKpackTime, bb);
        
        for (int i = 0; i < NUM_TYPES; i++) {
            typeCount[i] = bb.getInt();
        }
        
        //System.out.println(this);

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

        /*b.append(" size: ");
        b.append(getMax_packet_size());
        b.append("|");
        b.append(getMin_packet_size());
        b.append(" ttl:");
        b.append(getMax_ttl());
        b.append("|");
        b.append(getMin_ttl());
        b.append(" histogram: ");
        
        for(int i=0; i < MAX_HISTOGRAM_INDEX; ++i)
            b.append(getHistogram(i) + "|");
        
        b.append(" content: ");
        
        for(int i = 0; i < NUM_TYPES; ++i)
            b.append(getTypeCount(i) + "|");
        
        b.append(")");*/
        
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
        /*b.putInt((int)startTimeSeconds);
        b.putInt((int)startTimeMicroSeconds);
        b.putInt((int)endTimeSeconds);
        b.putInt((int)endTimeMicroSeconds*/
        
        for (int i = 0; i < NUM_TYPES; i++) {
            b.putShort((short)typeCount[i]);
        }
        
        b.putInt((int)size);
        b.putInt((int)packets);
        b.putShort((short)max_packet_size);
        b.putShort((short)min_packet_size);

        for(int i = 0; i < NUM_TCP_FLAGS; i++) {
            b.putShort((short)tcp_flags[i]);
        }
        
        b.put((byte)min_ttl);
        b.put((byte)max_ttl);
        
        for (int i = 0; i < MAX_HISTOGRAM_INDEX; i++) {
            b.putShort((short)histogram[i]);
        }
        
        /*b.putInt((int)synTimeSeconds);
        b.putInt((int)synTimeMicroSeconds);
        b.putInt((int)synackTimeSeconds);
        b.putInt((int)synackTimeMicroSeconds);
        b.putInt((int)finTimeSeconds);
        b.putInt((int)finTimeMicroSeconds);
        b.putInt((int)finackTimeSeconds);
        b.putInt((int)finackTimeMicroSeconds);*/
        
        dout.write(b.array());
    }

    public int getFragCount() {
        return fragCount;
    }

    public void setFragCount(int fragCount) {
        this.fragCount = fragCount;
    }

    public byte getTos() {
        return tos;
    }

    public void setTos(byte tos) {
        this.tos = tos;
    }

    public TimeStamp getStartTime() {
        return startTime;
    }

    public void setStartTime(TimeStamp startTime) {
        this.startTime = startTime;
    }

    public TimeStamp getEndTime() {
        return endTime;
    }

    public void setEndTime(TimeStamp endTime) {
        this.endTime = endTime;
    }

    public TimeStamp getMinInterArrivalTime() {
        return minInterArrivalTime;
    }

    public void setMinInterArrivalTime(TimeStamp minInterArrivalTime) {
        this.minInterArrivalTime = minInterArrivalTime;
    }

    public TimeStamp getMaxInterArrivalTime() {
        return maxInterArrivalTime;
    }

    public void setMaxInterArrivalTime(TimeStamp maxInterArrivalTime) {
        this.maxInterArrivalTime = maxInterArrivalTime;
    }

    public TimeStamp getFirstSYNpackTime() {
        return firstSYNpackTime;
    }

    public void setFirstSYNpackTime(TimeStamp firstSYNpackTime) {
        this.firstSYNpackTime = firstSYNpackTime;
    }

    public TimeStamp getFirstSYNACKpackTime() {
        return firstSYNACKpackTime;
    }

    public void setFirstSYNACKpackTime(TimeStamp firstSYNACKpackTime) {
        this.firstSYNACKpackTime = firstSYNACKpackTime;
    }

    public TimeStamp getFirstACKpackTime() {
        return firstACKpackTime;
    }

    public void setFirstACKpackTime(TimeStamp firstACKpackTime) {
        this.firstACKpackTime = firstACKpackTime;
    }
}