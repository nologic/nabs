/*
 * Channel.java
 *
 * Created on January 12, 2008, 1:09 PM
 *
 */

package eunomia.module.receptor.libb.imsCore.net;

import com.vivic.eunomia.sys.util.Util;
import eunomia.module.receptor.libb.imsCore.EnvironmentEntry;
import eunomia.module.receptor.libb.imsCore.EnvironmentKey;
import eunomia.module.receptor.libb.imsCore.StoreEnvironment;
import eunomia.module.receptor.libb.imsCore.bind.BoundObject;
import eunomia.module.receptor.libb.imsCore.db.NetEnv;
import eunomia.module.receptor.libb.imsCore.bind.ByteUtils;
import eunomia.module.receptor.libb.imsCore.util.MicroTime;

/**
 * This class represents a network connection as defined by the 
 * NetworkChannelFlowID object. It contains the data sent/received between 2 hosts.
 * 
 * The data is buffered until the connection is determined to be closed or timed
 * out. This decision is based on how many flows are received from the sensor. If
 * there are no flows for 6 minutes or (in case of TCP) there is a FIN or an RST
 * packet. In case of UDP, it is entirely based on the timeout.
 * 
 * This class contains a ChnContent subclass that stores the directional data. This
 * data is accessible through methods src() and dst().
 * @author Mikhail Sosonkin
 */
public class NetworkChannel extends EnvironmentEntry {
    public static transient final int DBG_OUTPUT_KEY             = 1 << 0;
    public static transient final int DBG_OUTPUT_FLOW_ID         = 1 << 1;
    public static transient final int DBG_OUTPUT_START_ACT       = 1 << 2;
    public static transient final int DBG_OUTPUT_END_ACT         = 1 << 3;
    public static transient final int DBG_OUTPUT_BYTE_COUNT      = 1 << 4;
    public static transient final int DBG_OUTPUT_MAX_PACKET_SIZE = 1 << 5;
    public static transient final int DBG_OUTPUT_MIN_PACKET_SIZE = 1 << 6;
    public static transient final int DBG_OUTPUT_OCCURENCES      = 1 << 7;
    public static transient final int DBG_OUTPUT_PACKET_COUNT    = 1 << 8;
            
    public static transient final int NUM_TYPES     = 8;
    public static transient final int NUM_TCP_FLAGS = 6;
    public static transient final int NUM_HISTOGRAM = 8;

    public static transient final int DT_Plain_Text = 0;
    public static transient final int DT_Image_BMP  = 1;
    public static transient final int DT_Audio_WAV  = 2;
    public static transient final int DT_Compressed = 3;
    public static transient final int DT_Image_JPG  = 4;
    public static transient final int DT_Audio_MP3  = 5;
    public static transient final int DT_Video_MPG  = 6;
    public static transient final int DT_Encrypted  = 7;

    public static transient final int TCP_SYN = 0;
    public static transient final int TCP_ACK = 1;
    public static transient final int TCP_FIN = 2;
    public static transient final int TCP_RST = 3;
    public static transient final int TCP_URG = 4;
    public static transient final int TCP_PSH = 5;
    
    public static transient final int HIST_0_256 = 0;
    public static transient final int HIST_257_512 = 1;
    public static transient final int HIST_513_768 = 2;
    public static transient final int HIST_769_1024 = 3;
    public static transient final int HIST_1025_1280 = 4;
    public static transient final int HIST_1281_1536 = 5;
    public static transient final int HIST_1537_1792 = 6;
    public static transient final int HIST_1792_2048 = 7;

    public static transient int dbg_output = DBG_OUTPUT_FLOW_ID | DBG_OUTPUT_BYTE_COUNT;
    
    private NetworkEntryKey key;
    private NetworkChannelFlowID flowId;
    
    // common
    private byte tos;
    private int occurences;
    private MicroTime startTime;
    private MicroTime endTime;
    
    // directional
    private ChnContent srcContent;
    private ChnContent dstContent;

    /**
     * 
     * @param env null can be passes. Parameter is deprecated.
     */
    public NetworkChannel(StoreEnvironment env) {
        super(env);
        
        key = new NetworkEntryKey();
        flowId = new NetworkChannelFlowID();
        srcContent = new ChnContent();
        dstContent = new ChnContent();

        startTime = new MicroTime(false);
        endTime = new MicroTime(true);
    }
    
    /**
     * This method flips the direction of the Channel. Basically swaps source and
     * destination information.
     */
    public void flip() {
        flowId.flip();
        
        ChnContent tmp;
        tmp = srcContent;
        srcContent = dstContent;
        dstContent = tmp;
    }
    
    /**
     * 
     * @return Returns the source contents.
     */
    public ChnContent src() {
        return srcContent;
    }
    
    /**
     * 
     * @return Returns the source contents.
     */
    public ChnContent dst() {
        return dstContent;
    }
    
    /**
     * 
     * @return Type of service (IPv4 field).
     */
    public byte getTos() {
        return tos;
    }
    
    public void setTos(byte tos) {
        this.tos = tos;
    }
    
    /**
     * 
     * @return Returns the number of flows seen with this Flow ID (Unidirectionally)
     */
    public int getOccurrences() {
        return occurences;
    }

    public void flow_addOccurrences(int count) {
        occurences += count;
    }
    
    /**
     * 
     * @return Returns time this flow was first seen. Usually it is when the initiator
     * created the connection. Generally when the first packet with the Flow ID
     * was seen by the sensor.
     */
    public MicroTime getStartTime() {
        return startTime;
    }
    
    /**
     * 
     * @return Returns the time this flow was last seen. Usually the FIN or RST
     * packet for TCP or the last flow for this channel.
     */
    public MicroTime getEndTime() {
        return endTime;
    }
    
    public void flow_setTimes(MicroTime sTime, MicroTime eTime) {
        if(startTime.compareTo(sTime) == 1) {
            startTime.set(sTime);
        }
        
        if(endTime.compareTo(eTime) == -1) {
            endTime.set(eTime);
        }
    }

    /**
     * Reinitialized the data to its default value.
     */
    public void clearData() {
        srcContent.clear();
        dstContent.clear();
        
        tos = 0;
        occurences = 0;
        
        startTime.setMinMax(false);
        endTime.setMinMax(true);
    }
    
    public NetworkEntryKey getEntryKey() {
        return key;
    }
    
    public EnvironmentKey getKey() {
        return getEntryKey();
    }
    
    /**
     * 
     * @return Returns the channel's Flow ID object.
     */
    public NetworkChannelFlowID getChannelFlowID() {
        return flowId;
    }
    
    public void setKey(NetworkEntryKey k) {
        key = k.clone();
    }
    
    public void setFlowID(NetworkChannelFlowID id) {
        flowId = id.clone();
    }
    
    public NetworkChannel clone() {
        NetworkChannel chan = new NetworkChannel(env);
        
        chan.startTime = startTime.clone();
        chan.endTime = endTime.clone();
        chan.srcContent = srcContent.clone();
        chan.dstContent = dstContent.clone();
        chan.occurences = occurences;
        
        chan.key = key.clone();
        chan.flowId = flowId.clone();
        
        return chan;
    }

    public int getByteSize() {
        return 1 + // tos
               4 + // occurences
               startTime.getByteSize() + 
               endTime.getByteSize() +
               srcContent.getByteSize() +
               dstContent.getByteSize() +
               key.getByteSize() + flowId.getByteSize();
    }

    public void serialize(byte[] arr, int offset) {
        srcContent.serialize(arr, offset);
        offset += srcContent.getByteSize();
        
        dstContent.serialize(arr, offset);
        offset += dstContent.getByteSize();
        
        startTime.serialize(arr, offset);
        offset += startTime.getByteSize();
        
        endTime.serialize(arr, offset);
        offset += endTime.getByteSize();
        
        key.serialize(arr, offset);
        offset += key.getByteSize();
        
        flowId.serialize(arr, offset);
    }

    public void unserialize(byte[] arr, int offset) {
        srcContent.unserialize(arr, offset);
        offset += srcContent.getByteSize();
        
        dstContent.unserialize(arr, offset);
        offset += dstContent.getByteSize();
        
        startTime.unserialize(arr, offset);
        offset += startTime.getByteSize();
        
        endTime.unserialize(arr, offset);
        offset += endTime.getByteSize();

        key.unserialize(arr, offset);
        offset += key.getByteSize();
        
        flowId.unserialize(arr, offset);
    }
    
    public String toString() {
        StringBuilder b = new StringBuilder();
        if( (dbg_output & DBG_OUTPUT_KEY) != 0) {
            b.append(key.toString());
        }
                
        if( (dbg_output & DBG_OUTPUT_FLOW_ID) != 0) {
            b.append(" ");
            b.append(flowId.toString());
        }

        if( (dbg_output & DBG_OUTPUT_START_ACT) != 0) {
            b.append(" Start Time (" + startTime + "): ");
            Util.getTimeStamp(b, startTime.getSeconds() * 1000L, true, true);
        }
        
        if( (dbg_output & DBG_OUTPUT_END_ACT) != 0) {
            b.append(" End Time (" + endTime + "): ");
            Util.getTimeStamp(b, endTime.getSeconds() * 1000L, true, true);
        }
        
        /*if( (dbg_output & DBG_OUTPUT_BYTE_COUNT) != 0) {
            b.append(" Bytes: ");
            b.append(byteCount);
        }
        
        if( (dbg_output & DBG_OUTPUT_MAX_PACKET_SIZE) != 0) {
            b.append(" Max Pack: ");
            b.append(max_packet_size);
        }
        
        if( (dbg_output & DBG_OUTPUT_MIN_PACKET_SIZE) != 0) {
            b.append(" Min Pack: ");
            b.append(min_packet_size);
        }
        
        if( (dbg_output & DBG_OUTPUT_OCCURENCES) != 0) {
            b.append(" Occurences: ");
            b.append(occurences);
        }
        
        if( (dbg_output & DBG_OUTPUT_PACKET_COUNT) != 0) {
            b.append(" Pack Count: ");
            b.append(packetCount);
        }*/
        
        return b.toString();
    }
    
    /**
     * This class represents information about one side of the Channel. This includes
     * everything from timing information to the content of the data.
     */
    public class ChnContent implements BoundObject {
        private transient int objSize;
        
        private MicroTime minInterArrivalTime;
        private MicroTime maxInterArrivalTime;
        private MicroTime firstSYNpackTime;
        private MicroTime firstSYNACKpackTime;
        private MicroTime firstACKpackTime;
    
        private long size;
        private int packets;
        private int max_packet_size;
        private int min_packet_size;
        private int fragCount;
    
        private short min_ttl;
        private short max_ttl;
    
        private long[] content;
        private int[] histogram;
        private int[] tcp_flags;
        
        public ChnContent() {
            minInterArrivalTime = new MicroTime(false);
            maxInterArrivalTime = new MicroTime(true);
            firstSYNpackTime = new MicroTime(false);
            firstSYNACKpackTime = new MicroTime(false);
            firstACKpackTime = new MicroTime(false);

            content = new long[NUM_TYPES];
            tcp_flags = new int[NUM_TCP_FLAGS];
            histogram = new int[NUM_HISTOGRAM];
            
            // Compute size
            objSize = 
                minInterArrivalTime.getByteSize() * 5 + // times.
                ByteUtils.LONG_SIZE + //private long size;
                ByteUtils.INT_SIZE + //private int packets;
                ByteUtils.INT_SIZE + //private int max_packet_size;
                ByteUtils.INT_SIZE + //private int min_packet_size;
                ByteUtils.INT_SIZE + //private int fragCount;

                ByteUtils.SHORT_SIZE + //private short min_ttl;
                ByteUtils.SHORT_SIZE + //private short max_ttl;

                ByteUtils.LONG_SIZE * NUM_TYPES + //private long[] content;
                ByteUtils.INT_SIZE * NUM_HISTOGRAM + //private int[] histogram;
                ByteUtils.INT_SIZE * NUM_TCP_FLAGS;//private int[] tcp_flags;
        }
        
        public ChnContent clone() {
            ChnContent c = new ChnContent();
            
            c.minInterArrivalTime = minInterArrivalTime.clone();
            c.maxInterArrivalTime = maxInterArrivalTime.clone();
            c.firstSYNpackTime = firstSYNpackTime.clone();
            c.firstSYNACKpackTime = firstSYNACKpackTime.clone();
            c.firstACKpackTime = firstACKpackTime.clone();

            c.size = size;
            c.packets = packets;
            c.max_packet_size = max_packet_size;
            c.min_packet_size = min_packet_size;
            c.fragCount = fragCount;

            c.min_ttl = min_ttl;
            c.max_ttl = max_ttl;
            
            c.content = new long[content.length];
            c.histogram = new int[histogram.length];
            c.tcp_flags = new int[tcp_flags.length];
            
            System.arraycopy(content, 0, c.content, 0, content.length);
            System.arraycopy(histogram, 0, c.histogram, 0, histogram.length);
            System.arraycopy(tcp_flags, 0, c.tcp_flags, 0, tcp_flags.length);
            
            return c;
        }

        public int getByteSize() {
            return objSize;
        }

        public void serialize(byte[] arr, int offset) {
            int timeSize = minInterArrivalTime.getByteSize();
            
            minInterArrivalTime.serialize(arr, offset);
            offset += timeSize;
            
            maxInterArrivalTime.serialize(arr, offset);
            offset += timeSize;
            
            firstSYNpackTime.serialize(arr, offset);
            offset += timeSize;
            
            firstSYNACKpackTime.serialize(arr, offset);
            offset += timeSize;
            
            firstACKpackTime.serialize(arr, offset);
            offset += timeSize;
            
            offset += ByteUtils.longToBytes(arr, offset, size);
            offset += ByteUtils.intToBytes(arr, offset, packets);
            offset += ByteUtils.intToBytes(arr, offset, max_packet_size);
            offset += ByteUtils.intToBytes(arr, offset, min_packet_size);
            offset += ByteUtils.intToBytes(arr, offset, fragCount);
            
            offset += ByteUtils.longArrToBytes(arr, offset, content, 0, NUM_TYPES);
            offset += ByteUtils.intArrToBytes(arr, offset, histogram, 0, NUM_HISTOGRAM);
            offset += ByteUtils.intArrToBytes(arr, offset, tcp_flags, 0, NUM_TCP_FLAGS);
        }

        public void unserialize(byte[] arr, int offset) {
            int timeSize = minInterArrivalTime.getByteSize();
            
            minInterArrivalTime.unserialize(arr, offset);
            offset += timeSize;
            
            maxInterArrivalTime.unserialize(arr, offset);
            offset += timeSize;
            
            firstSYNpackTime.unserialize(arr, offset);
            offset += timeSize;
            
            firstSYNACKpackTime.unserialize(arr, offset);
            offset += timeSize;
            
            firstACKpackTime.unserialize(arr, offset);
            offset += timeSize;
            
            size = ByteUtils.bytesToLong(arr, offset);
            offset += ByteUtils.LONG_SIZE;
            
            packets = ByteUtils.bytesToInt(arr, offset);
            offset += ByteUtils.INT_SIZE;

            max_packet_size = ByteUtils.bytesToInt(arr, offset);
            offset += ByteUtils.INT_SIZE;

            min_packet_size = ByteUtils.bytesToInt(arr, offset);
            offset += ByteUtils.INT_SIZE;

            fragCount = ByteUtils.bytesToInt(arr, offset);
            offset += ByteUtils.INT_SIZE;
            
            content = ByteUtils.bytesToLongArr(arr, offset, content, 0, NUM_TYPES);
            offset += NUM_TYPES * ByteUtils.LONG_SIZE;
            
            histogram = ByteUtils.bytesToIntArr(arr, offset, histogram, 0, NUM_HISTOGRAM);
            offset += NUM_HISTOGRAM * ByteUtils.INT_SIZE;
            
            tcp_flags = ByteUtils.bytesToIntArr(arr, offset, tcp_flags, 0, NUM_TCP_FLAGS);
            offset += NUM_TCP_FLAGS * ByteUtils.INT_SIZE;
        }
        
        public void clear() {
            minInterArrivalTime.setMinMax(false);
            maxInterArrivalTime.setMinMax(true);
            firstSYNpackTime.setMinMax(false);
            firstSYNACKpackTime.setMinMax(false);
            firstACKpackTime.setMinMax(false);

            size = 0L;
            packets = 0;
            max_packet_size = 0;
            min_packet_size = 0;
            fragCount = 0;

            min_ttl = Short.MAX_VALUE;
            max_ttl = Short.MIN_VALUE;

            for (int i = 0; i < NUM_TYPES; ++i) {
                content[i] = 0;
            }
            
            for (int i = 0; i < NUM_HISTOGRAM; ++i) {
                histogram[i] = 0;
            }
            
            for (int i = 0; i < NUM_TCP_FLAGS; ++i) {
                tcp_flags[i] = 0;
            }
        }
        
        // This is primarily for NetCollector!
        public void flow_addData(long size, int packets, long[] cont, int[] flags, int[] hist,
                                 int max_packet_size, int min_packet_size, int fragCount) {
            this.size += size;
            this.packets += packets;
            this.fragCount += fragCount;
            
            if(this.max_packet_size < max_packet_size) {
                this.max_packet_size = max_packet_size;
            }
            
            if(this.min_packet_size > min_packet_size) {
                this.min_packet_size = min_packet_size;
            }
            
            long[] tmp_l = content;
            for (int i = 0; i < NUM_TYPES; ++i) {
                tmp_l[i] += cont[i];
            }
            
            int[] tmp_i = tcp_flags;
            for (int i = 0; i < NUM_TCP_FLAGS; ++i) {
                tmp_i[i] += flags[i];
            }
            
            tmp_i = histogram;
            for (int i = 0; i < NUM_HISTOGRAM; ++i) {
                tmp_i[i] += hist[i];
            }
        }

        public void flow_setTimes(MicroTime minInterArrivalTime, MicroTime maxInterArrivalTime, 
                                  MicroTime firstSYNpackTime, MicroTime firstSYNACKpackTime, 
                                  MicroTime firstACKpackTime) {
            if(minInterArrivalTime.compareTo(this.minInterArrivalTime) == 1) {
                this.minInterArrivalTime.set(minInterArrivalTime);
            }
            
            if(this.maxInterArrivalTime.compareTo(maxInterArrivalTime) == -1) {
                this.maxInterArrivalTime.set(maxInterArrivalTime);
            }
            
            if(this.firstSYNpackTime.compareTo(firstSYNpackTime) == -1) {
                this.firstSYNpackTime.set(firstSYNpackTime);
            }
            
            if(this.firstSYNACKpackTime.compareTo(firstSYNACKpackTime) == -1) {
                this.firstSYNACKpackTime.set(firstSYNACKpackTime);
            }
            
            if(this.firstACKpackTime.compareTo(firstACKpackTime) == -1) {
                this.firstACKpackTime.set(firstACKpackTime);
            }
        }

        public void flow_setTtls(short min_ttl, short max_ttl) {
            this.min_ttl = min_ttl;
            this.max_ttl = max_ttl;
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

        public long getSize() {
            return size;
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

        public int getFragCount() {
            return fragCount;
        }

        public short getMin_ttl() {
            return min_ttl;
        }

        public short getMax_ttl() {
            return max_ttl;
        }

        public long[] getContent() {
            return content;
        }

        public int[] getHistogram() {
            return histogram;
        }

        public int[] getTcp_flags() {
            return tcp_flags;
        }
    }
}