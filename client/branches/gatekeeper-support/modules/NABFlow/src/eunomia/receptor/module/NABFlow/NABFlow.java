/*
 * Flow.java
 *
 * Created on June 1, 2005, 12:53 PM
 */

package eunomia.receptor.module.NABFlow;

import com.vivic.eunomia.module.Flow;
import eunomia.util.Util;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * This is the class that is passed around to all modules interesting in processing
 * it. The mothods within it must be highly optimized because they will dominate
 * the execution time. If they are not optimized then it has a good chance of
 * slowing down the whole system.
 * @author Mikhail Sosonkin
 */
public class NABFlow implements Flow {
    /**
     * Each flow is 21 bytes of data on the network.
     */
    
    public static final int FLOW_BYTE_SIZE = 21;
    
    /**
     * There are 8 different data types
     */
    public static final int NUM_TYPES = 8;
    
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
    
    /**
     * Server's time when the flow was generated.
     */
    private long time;
    
    /**
     * Source IP of the data for this flow.
     */
    private long srcIp;
    
    /**
     * destination IP
     */
    private long dstIp;
    
    /**
     * source port
     */
    private int srcPort;
    
    /**
     * destination port
     */
    private int dstPort;
    
    /**
     * data type as determined by the sensor.
     */
    private int type;
    
    /**
     * amount of bytes analyzed to produce this flow record.
     */
    private int size;
    
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
    public NABFlow(){
        // at the moment only IPv4 is supported.
        ipWorkBytes = new byte[4];
        buff = new byte[FLOW_BYTE_SIZE];
        // generally the size does not change, so we set it as the default value.
        size = 16384;
    }
    
    public long getTime(){
        return time;
    }
    
    public int getSize(){
        return size;
    }
    
    public long getSourceIP(){
        return srcIp;
    }
    
    public int getSourcePort(){
        return srcPort;
    }
    
    public long getDestinationIP(){
        return dstIp;
    }
    
    public int getDestinationPort(){
        return dstPort;
    }
    
    public int getType(){
        return type;
    }
    
    public void setTime(long time) {
        this.time = time;
    }

    public void setSrcIp(long srcIp) {
        this.srcIp = srcIp;
    }

    public void setDstIp(long dstIp) {
        this.dstIp = dstIp;
    }

    public void setSrcPort(int srcPort) {
        this.srcPort = srcPort;
    }

    public void setDstPort(int dstPort) {
        this.dstPort = dstPort;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setSize(int size) {
        this.size = size;
    }
    
    /**
     * Copies all the values from a different NABFlow flow instance. Useful when a
     * seperate (static) flow object is needed, for example to compare with the next
     * flow from the sensor.
     * @param flow Flow to copy.
     */
    public void takeFrom(NABFlow flow, boolean copy){
        time = flow.time;
        srcIp = flow.srcIp;
        dstIp = flow.dstIp;
        srcPort = flow.srcPort;
        dstPort = flow.dstPort;
        type = flow.type;
        // copy everything including the byte buffer.
        if(copy) {
            System.arraycopy(flow.buff, 0, buff, 0, buff.length);
        } else {
            buff = flow.buff;
        }
    }
    
    /**
     * returns a string with all the data, for display to the user. This method should
     * not be used unless absolutely neccessary (i.e. for debugging).
     */
    public String toString(){
        // performance hazard.
        StringBuffer buff = new StringBuffer();
        buff.append(Util.getTimeStamp(time, true, true));
        buff.append("> ");
        buff.append(Util.ipToString(srcIp));
        buff.append(":" + srcPort);
        buff.append(" to ");
        buff.append(Util.ipToString(dstIp));
        buff.append(":" + dstPort);
        buff.append(" | ");
        buff.append(typeNames[type]);
        buff.append(" |");
        
        return buff.toString();
    }
    
    /**
     * Used to get module specific inforation. For this module only the SQL string is
     * returned. The string contains all the fields in order. Currently this method is
     * used by the database collector.
     * @param format Possible parameters for retreiving only needed information.
     * @return the SQL String.
     */
    public Object getSpecificInfo(Object format){
        return getSQLInsertString();
    }
    
    /**
     * This creates a lot of intermediate string objects, so it's not the most 
     * efficient method of generating the String.
     * @return SQL String
     */
    public String getSQLInsertString(){
        // performance hazard
        StringBuffer buff = new StringBuffer();
        buff.append("(" + time);
        buff.append("," + srcIp);
        buff.append("," + srcPort);
        buff.append("," + dstIp);
        buff.append("," + dstPort);
        buff.append(",");
        buff.append(type + ")");
        
        return buff.toString();
    }
    
    /**
     * Write the data to the output stream.
     * @param dout destination stream
     * @throws java.io.IOException 
     */
    public void writeToDataStream(DataOutputStream dout) throws IOException {
        /*
         * Local shifting and type deconstruction is used because it
         * improves performace by not making method calls. The buffer
         * array is not always available to field at the time is done.
         */
        int count = 0;
        
        long workLong;
        int workInt;
        
        workLong = time;
        buff[count++] = (byte)(workLong >> 24);
        buff[count++] = (byte)(workLong >> 16);
        buff[count++] = (byte)(workLong >> 8 );
        buff[count++] = (byte)(workLong      );
        
        workInt = size;
        buff[count++] = (byte)(workInt >> 24);
        buff[count++] = (byte)(workInt >> 16);
        buff[count++] = (byte)(workInt >> 8 );
        buff[count++] = (byte)(workInt      );
        
        workLong = srcIp;
        buff[count++] = (byte)(workLong >> 24);
        buff[count++] = (byte)(workLong >> 16);
        buff[count++] = (byte)(workLong >> 8 );
        buff[count++] = (byte)(workLong      );
        
        workLong = dstIp;
        buff[count++] = (byte)(workLong >> 24);
        buff[count++] = (byte)(workLong >> 16);
        buff[count++] = (byte)(workLong >> 8 );
        buff[count++] = (byte)(workLong      );
        
        workInt = srcPort;
        buff[count++] = (byte)(workInt >> 8 );
        buff[count++] = (byte)(workInt      );
        
        workInt = dstPort;
        buff[count++] = (byte)(workInt >> 8 );
        buff[count++] = (byte)(workInt      );
        
        buff[count++] = (byte)(type);
        
        dout.write(buff);
    }
    
    /**
     * populates the fields with the data from the byte array.
     * @param bytes Bytes that have the flow record
     * @param copy The bytes arn't always needed so, they are copies only if requested.
     */
    public void takeFromBytes(byte[] bytes, boolean copy){
        int count = 0;
        int read = 0;
        
        long workLong1 = 0;
        long workLong2 = 0;
        long workLong3 = 0;
        long workLong4 = 0;
        
        int workInt1 = 0;
        int workInt2 = 0;
        int workInt3 = 0;
        int workInt4 = 0;
        
        if(copy){
            System.arraycopy(bytes, 0, buff, 0, bytes.length);
        }
        
        workLong1 = buff[count++] & 0xFFL;
        workLong1 = workLong1 << 24;
        workLong2 = buff[count++] & 0xFFL;
        workLong2 = workLong2 << 16;
        workLong3 = buff[count++] & 0xFFL;
        workLong3 = workLong3 << 8;
        workLong4 = buff[count++] & 0xFFL;
        time = workLong1 | workLong2 | workLong3 | workLong4;
        
        workInt1 = buff[count++] & 0xFF;
        workInt1 = workInt1 << 24;
        workInt2 = buff[count++] & 0xFF;
        workInt2 = workInt2 << 16;
        workInt3 = buff[count++] & 0xFF;
        workInt3 = workInt3 << 8;
        workInt4 = buff[count++] & 0xFF;
        size = workInt1 | workInt2 | workInt3 | workInt4;
        
        workLong1 = buff[count++] & 0xFFL;
        workLong1 = workLong1 << 24;
        workLong2 = buff[count++] & 0xFFL;
        workLong2 = workLong2 << 16;
        workLong3 = buff[count++] & 0xFFL;
        workLong3 = workLong3 << 8;
        workLong4 = buff[count++] & 0xFFL;
        srcIp = workLong1 | workLong2 | workLong3 | workLong4;
        
        workLong1 = buff[count++] & 0xFFL;
        workLong1 = workLong1 << 24;
        workLong2 = buff[count++] & 0xFFL;
        workLong2 = workLong2 << 16;
        workLong3 = buff[count++] & 0xFFL;
        workLong3 = workLong3 << 8;
        workLong4 = buff[count++] & 0xFFL;
        dstIp = workLong1 | workLong2 | workLong3 | workLong4;
        
        workInt1 = buff[count++] & 0xFF;
        workInt1 = workInt1 << 8;
        workInt2 = buff[count++] & 0xFF;
        srcPort = workInt1 | workInt2;
        
        workInt1 = buff[count++] & 0xFF;
        workInt1 = workInt1 << 8;
        workInt2 = buff[count++] & 0xFF;
        dstPort = workInt1 | workInt2;
        
        // 0x07 to prevent it from producing an index of greater than 7
        type = buff[count++] & 0x7;
    }
    
    /**
     * Interface method for parsing data from the network. This is the method called
     * by the middlware.
     * @param bb Buffer that will contain at least one flow.
     */
    public void readFromByteBuffer(ByteBuffer bb){
        int tmpInt;
        short tmpShort;
        
        tmpInt = bb.getInt();
        time = tmpInt & 0xFFFFFFFFL;
        time *= 1000;
        
        size = bb.getInt();
        
        tmpInt = bb.getInt();
        srcIp = tmpInt & 0xFFFFFFFFL;
        
        tmpInt = bb.getInt();
        dstIp = tmpInt & 0xFFFFFFFFL;
        
        tmpShort = bb.getShort();
        srcPort = tmpShort & 0x0000FFFF;
        
        tmpShort = bb.getShort();
        dstPort = tmpShort & 0x0000FFFF;
        
        // 0x07 to prevent it from producing an index of greater than 7
        type = bb.get() & 0x7;
    }
    
    /**
     * Legacy support.
     */
    public void readFromDataStream(DataInputStream din) throws IOException {
        din.readFully(buff);
        readFromDataStream(buff);
    }
    
    public void readFromDataStream(byte[] buf){
        this.buff = buf;
        
        int count = 0;
        int read = 0;
        
        long workLong1 = 0;
        long workLong2 = 0;
        long workLong3 = 0;
        long workLong4 = 0;
        
        int workInt1 = 0;
        int workInt2 = 0;
        int workInt3 = 0;
        int workInt4 = 0;
        
        workLong1 = buf[count++] & 0xFFL;
        workLong1 = workLong1 << 24;
        workLong2 = buf[count++] & 0xFFL;
        workLong2 = workLong2 << 16;
        workLong3 = buf[count++] & 0xFFL;
        workLong3 = workLong3 << 8;
        workLong4 = buf[count++] & 0xFFL;
        time = workLong1 | workLong2 | workLong3 | workLong4;
        
        workInt1 = buf[count++] & 0xFF;
        workInt1 = workInt1 << 24;
        workInt2 = buf[count++] & 0xFF;
        workInt2 = workInt2 << 16;
        workInt3 = buf[count++] & 0xFF;
        workInt3 = workInt3 << 8;
        workInt4 = buf[count++] & 0xFF;
        size = workInt1 | workInt2 | workInt3 | workInt4;
        
        workLong1 = buf[count++] & 0xFFL;
        workLong1 = workLong1 << 24;
        workLong2 = buf[count++] & 0xFFL;
        workLong2 = workLong2 << 16;
        workLong3 = buf[count++] & 0xFFL;
        workLong3 = workLong3 << 8;
        workLong4 = buf[count++] & 0xFFL;
        srcIp = workLong1 | workLong2 | workLong3 | workLong4;
        
        workLong1 = buf[count++] & 0xFFL;
        workLong1 = workLong1 << 24;
        workLong2 = buf[count++] & 0xFFL;
        workLong2 = workLong2 << 16;
        workLong3 = buf[count++] & 0xFFL;
        workLong3 = workLong3 << 8;
        workLong4 = buf[count++] & 0xFFL;
        dstIp = workLong1 | workLong2 | workLong3 | workLong4;
        
        workInt1 = buf[count++] & 0xFF;
        workInt1 = workInt1 << 8;
        workInt2 = buf[count++] & 0xFF;
        srcPort = workInt1 | workInt2;
        
        workInt1 = buf[count++] & 0xFF;
        workInt1 = workInt1 << 8;
        workInt2 = buf[count++] & 0xFF;
        dstPort = workInt1 | workInt2;
        
        // 0x07 to prevent it from producing an index of greater than 7
        type = buf[count++] & 0x7;
    }
}