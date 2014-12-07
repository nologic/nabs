/*
 * PacketHeader.java
 *
 * Created on September 2, 2006, 2:45 PM
 */

package eunomia.receptor.module.netFlow;

import java.nio.ByteBuffer;

/**
 *
 * @author Mikhail Sosonkin
 */
public class PacketHeader {
    private int version;
    private int count;
    private long systemUptime;
    private long unixTime;
    private long seqNumber;
    private long sourceID;
    
    public PacketHeader() {
    }
    
    public void parseHeader(ByteBuffer buffer){
        version = buffer.getShort() & 0xFFFF;
        count = buffer.getShort() & 0xFFFF;
        systemUptime = buffer.getInt() & 0xFFFFFFFFL;
        unixTime = buffer.getInt() & 0xFFFFFFFFL;
        seqNumber = buffer.getInt() & 0xFFFFFFFFL;
        sourceID = buffer.getInt() & 0xFFFFFFFFL;
    }
    
    public int getSize(){
        return 20;
    }

    public int getVersion() {
        return version;
    }

    public int getCount() {
        return count;
    }

    public long getSystemUptime() {
        return systemUptime;
    }

    public long getUnixTime() {
        return unixTime;
    }

    public long getSeqNumber() {
        return seqNumber;
    }

    public long getSourceID() {
        return sourceID;
    }
}