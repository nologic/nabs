/*
 * DarkAccess.java
 *
 * Created on January 26, 2008, 11:31 AM
 *
 */

package eunomia.module.receptor.libb.imsCore.net;

import eunomia.module.receptor.libb.imsCore.EnvironmentEntry;
import eunomia.module.receptor.libb.imsCore.EnvironmentKey;
import eunomia.module.receptor.libb.imsCore.StoreEnvironment;
import eunomia.module.receptor.libb.imsCore.bind.ByteUtils;
import eunomia.module.receptor.libb.imsCore.util.MicroTime;

/**
 *
 * @author Mikhail Sosonkin
 */
public class DarkAccess extends EnvironmentEntry {
    private NetworkEntryKey key;
    private NetworkChannelFlowID flowID;
    
    private MicroTime startTime;
    private MicroTime endTime;
    private MicroTime minInterArrivalTime;
    private MicroTime maxInterArrivalTime;
    private MicroTime firstSYNpackTime;
    private MicroTime firstSYNACKpackTime;
    private MicroTime firstACKpackTime;
 
    private int packets;

    private int max_packet_size;
    private int min_packet_size;
    
    private int[] tcp_flags;

    private short min_ttl;
    private short max_ttl;
    
    private int fragCount;
    private byte tos;
    
    private int byteSize;
    
    public DarkAccess(StoreEnvironment env) {
        super(env);
        
        key = new NetworkEntryKey();
        flowID = new NetworkChannelFlowID();
        
        startTime = new MicroTime(0, 0);
        endTime = new MicroTime(0, 0);
        minInterArrivalTime = new MicroTime(0, 0);
        maxInterArrivalTime = new MicroTime(0, 0);
        firstSYNpackTime = new MicroTime(0, 0);
        firstSYNACKpackTime = new MicroTime(0, 0);
        firstACKpackTime = new MicroTime(0, 0);
        
        tcp_flags = new int[NetworkChannel.NUM_TCP_FLAGS];
        
        byteSize = key.getByteSize() +              //DarkAccessKey key;
               flowID.getByteSize() +           //NetworkChannelFlowID flowID;
               startTime.getByteSize() * 7 +    //MicroTime startTime, endTime, minInterArrivalTime, maxInterArrivalTime, firstSYNpackTime
                                                //firstSYNACKpackTime, firstACKpackTime
               ByteUtils.INT_SIZE * 4 +         //int packets, max_packet_size, min_packet_size, fragCount;
               ByteUtils.INT_SIZE * NetworkChannel.NUM_TCP_FLAGS +     //int[] tcp_flags;
               ByteUtils.SHORT_SIZE * 2 +       // short min_ttl, max_ttl;
               1;                               // private byte tos;
    }
    
    public void setFlowID(NetworkChannelFlowID flowID) {
        this.flowID = flowID.clone();
    }
    
    public NetworkChannelFlowID getFlowID() {
        return flowID;
    }

    public void setKey(NetworkEntryKey key) {
        this.key = key.clone();
    }

    public EnvironmentKey getKey() {
        return key;
    }
    
    public NetworkEntryKey getEntryKey() {
        return key;
    }
    
    public MicroTime getStartTime() {
        return startTime;
    }

    public MicroTime getEndTime() {
        return endTime;
    }

    public MicroTime getMinInterArrivalTime() {
        return minInterArrivalTime;
    }

    public MicroTime getMaxInterArrivalTime() {
        return maxInterArrivalTime;
    }

    public MicroTime getFirstSYNpackTime() {
        return firstSYNpackTime;
    }

    public MicroTime getFirstSYNACKpackTime() {
        return firstSYNACKpackTime;
    }

    public MicroTime getFirstACKpackTime() {
        return firstACKpackTime;
    }

    public int getPackets() {
        return packets;
    }

    public int getMax_packet_size() {
        return max_packet_size;
    }

    public int getMin_packet_size() {
        return min_packet_size;
    }

    public int[] getTcp_flags() {
        return tcp_flags;
    }

    public short getMin_ttl() {
        return min_ttl;
    }

    public short getMax_ttl() {
        return max_ttl;
    }

    public int getFragCount() {
        return fragCount;
    }

    public byte getTos() {
        return tos;
    }

    public DarkAccess clone() {
        DarkAccess da = new DarkAccess(env);

        da.key = key.clone();
        da.flowID = flowID.clone();
        
        da.startTime = startTime.clone();
        da.endTime = endTime.clone();
        da.minInterArrivalTime = minInterArrivalTime.clone();
        da.maxInterArrivalTime = maxInterArrivalTime.clone();
        da.firstACKpackTime = firstACKpackTime.clone();
        da.firstSYNACKpackTime = firstSYNACKpackTime.clone();
        da.firstSYNpackTime = firstSYNpackTime.clone();
        
        da.packets = packets;
        da.max_packet_size = max_packet_size;
        da.min_packet_size = min_packet_size;
        da.min_ttl = min_ttl;
        da.max_ttl = max_ttl;
        da.fragCount = fragCount;
        da.tos = tos;
        
        da.tcp_flags = new int[NetworkChannel.NUM_TCP_FLAGS];
        System.arraycopy(tcp_flags, 0, da.tcp_flags, 0, NetworkChannel.NUM_TCP_FLAGS);
        
        return da;
    }
    
    public void flow_setTimes(MicroTime startTime, MicroTime endTime, MicroTime minInterArrivalTime, MicroTime maxInterArrivalTime,
                              MicroTime firstSYNpackTime, MicroTime firstSYNACKpackTime, MicroTime firstACKpackTime) {
        this.startTime.set(startTime);
        this.endTime.set(endTime);
        this.minInterArrivalTime.set(minInterArrivalTime);
        this.maxInterArrivalTime.set(maxInterArrivalTime);
        this.firstSYNpackTime.set(firstSYNpackTime);
        this.firstSYNACKpackTime.set(firstSYNACKpackTime);
        this.firstACKpackTime.set(firstACKpackTime);
    }
    
    public void flow_setData(int packets, int max_packet_size, int min_packet_size, short min_ttl, 
                             short max_ttl, int fragCount, byte tos, int[] tcp_flags) {
        this.packets = packets;
        this.max_packet_size = max_packet_size;
        this.min_packet_size = min_packet_size;
        this.min_ttl = min_ttl;
        this.max_ttl = max_ttl;
        this.fragCount = fragCount;
        this.tos = tos;
        
        System.arraycopy(tcp_flags, 0, this.tcp_flags, 0, NetworkChannel.NUM_TCP_FLAGS);
    }

    public int getByteSize() {
        return byteSize;
    }

    public void serialize(byte[] arr, int offset) {
        key.serialize(arr, offset);
        offset += key.getByteSize();
        
        flowID.serialize(arr, offset);
        offset += flowID.getByteSize();
        
        startTime.serialize(arr, offset);
        offset += MicroTime.MICRO_TIME_SIZE;
        
        endTime.serialize(arr, offset);
        offset += MicroTime.MICRO_TIME_SIZE;

        minInterArrivalTime.serialize(arr, offset);
        offset += MicroTime.MICRO_TIME_SIZE;

        maxInterArrivalTime.serialize(arr, offset);
        offset += MicroTime.MICRO_TIME_SIZE;

        firstSYNpackTime.serialize(arr, offset);
        offset += MicroTime.MICRO_TIME_SIZE;

        firstSYNACKpackTime.serialize(arr, offset);
        offset += MicroTime.MICRO_TIME_SIZE;

        firstACKpackTime.serialize(arr, offset);
        offset += MicroTime.MICRO_TIME_SIZE;
        
        offset += ByteUtils.intToBytes(arr, offset, packets);
        offset += ByteUtils.intToBytes(arr, offset, max_packet_size);
        offset += ByteUtils.intToBytes(arr, offset, min_packet_size);
        offset += ByteUtils.intToBytes(arr, offset, fragCount);
        offset += ByteUtils.intArrToBytes(arr, offset, tcp_flags, 0, NetworkChannel.NUM_TCP_FLAGS);
        
        offset += ByteUtils.shortToBytes(arr, offset, min_ttl);
        offset += ByteUtils.shortToBytes(arr, offset, max_ttl);

        arr[offset] = tos;
        ++offset;
    }

    public void unserialize(byte[] arr, int offset) {
        key.unserialize(arr, offset);
        offset += key.getByteSize();
        
        flowID.unserialize(arr, offset);
        offset += flowID.getByteSize();
        
        startTime.unserialize(arr, offset);
        offset += MicroTime.MICRO_TIME_SIZE;
        
        endTime.unserialize(arr, offset);
        offset += MicroTime.MICRO_TIME_SIZE;

        minInterArrivalTime.unserialize(arr, offset);
        offset += MicroTime.MICRO_TIME_SIZE;

        maxInterArrivalTime.unserialize(arr, offset);
        offset += MicroTime.MICRO_TIME_SIZE;

        firstSYNpackTime.unserialize(arr, offset);
        offset += MicroTime.MICRO_TIME_SIZE;

        firstSYNACKpackTime.unserialize(arr, offset);
        offset += MicroTime.MICRO_TIME_SIZE;

        firstACKpackTime.unserialize(arr, offset);
        offset += MicroTime.MICRO_TIME_SIZE;
        
        packets = ByteUtils.bytesToInt(arr, offset);
        offset += ByteUtils.INT_SIZE;
        
        max_packet_size = ByteUtils.bytesToInt(arr, offset);
        offset += ByteUtils.INT_SIZE;
        
        min_packet_size = ByteUtils.bytesToInt(arr, offset);
        offset += ByteUtils.INT_SIZE;
        
        fragCount = ByteUtils.bytesToInt(arr, offset);
        offset += ByteUtils.INT_SIZE;
        
        tcp_flags = ByteUtils.bytesToIntArr(arr, offset, tcp_flags, 0, NetworkChannel.NUM_TCP_FLAGS);
        offset += ByteUtils.INT_SIZE * NetworkChannel.NUM_TCP_FLAGS;
        
        min_ttl = ByteUtils.bytesToShort(arr, offset);
        offset += ByteUtils.SHORT_SIZE;
        
        max_ttl = ByteUtils.bytesToShort(arr, offset);
        offset += ByteUtils.SHORT_SIZE;

        tos = arr[offset];
    }
}