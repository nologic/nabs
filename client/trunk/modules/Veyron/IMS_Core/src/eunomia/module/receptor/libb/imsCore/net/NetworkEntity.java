/*
 * NetworkEntity.java
 *
 * Created on December 6, 2007, 6:56 PM
 *
 */

package eunomia.module.receptor.libb.imsCore.net;

import com.vivic.eunomia.sys.util.Util;
import eunomia.module.receptor.libb.imsCore.*;
import eunomia.module.receptor.libb.imsCore.EnvironmentEntry;
import eunomia.module.receptor.libb.imsCore.bind.BoundObject;
import eunomia.module.receptor.libb.imsCore.bind.ByteUtils;
import eunomia.module.receptor.libb.imsCore.db.NetEnv;
import eunomia.module.receptor.libb.imsCore.util.MicroTime;

/**
 *
 * @author Mikhail Sosonkin
 */
public class NetworkEntity extends EnvironmentEntry {
    public static transient final int DBG_OUTPUT_KEY             = 1 << 0;
    public static transient final int DBG_OUTPUT_HOST_KEY        = 1 << 1;
    public static transient final int DBG_OUTPUT_START_ACT       = 1 << 2;
    public static transient final int DBG_OUTPUT_END_ACT         = 1 << 3;
    public static transient final int DBG_OUTPUT_DATA_SENT       = 1 << 2;
    public static transient final int DBG_OUTPUT_DATA_RECEIVED   = 1 << 3;
    
    public static int dbg_output = DBG_OUTPUT_HOST_KEY | DBG_OUTPUT_DATA_SENT | DBG_OUTPUT_DATA_RECEIVED;
    
    private NetworkEntryKey key;
    private NetworkEntityHostKey hostKey;
    private MicroTime endTime; // in Seconds.
    private MicroTime startTime; // in Seconds.
    
    // in bytes
    private EntContent src;
    private EntContent dst;
    
    public NetworkEntity(StoreEnvironment env) {
        super(env);
        
        key = new NetworkEntryKey();
        hostKey = new NetworkEntityHostKey();
        
        src = new EntContent();
        dst = new EntContent();
        
        startTime = new MicroTime(false);
        endTime = new MicroTime(true);
    }
    
    public MicroTime getStartTime() {
        return startTime;
    }
    
    public MicroTime getEndTime() {
        return endTime;
    }
    
    public EntContent src() {
        return src;
    }
    
    public EntContent dst() {
        return dst;
    }
    
    public void clearData() {
        src.clear();
        dst.clear();
        
        startTime.setMinMax(false);
        endTime.setMinMax(true);
    }
    
    public boolean equals(Object o) {
        if(o instanceof NetworkEntityHostKey) {
            return hostKey.equals(o);
        } else if(o instanceof NetworkEntity) {
            return hostKey.equals(((NetworkEntity)o).getKey());
        }
        
        return false;
    }
    
    public void setHostKey(NetworkEntityHostKey key) {
        this.hostKey = key.clone();
    }
    
    public NetworkEntityHostKey getHostKey() {
        return hostKey;
    }
    
    public void setKey(NetworkEntryKey key) {
        this.key = key.clone();
    }
    
    public NetworkEntryKey getEntryKey() {
        return key;
    }
    
    public EnvironmentKey getKey() {
        return getEntryKey();
    }
    
    public void setTimes(MicroTime sTime, MicroTime eTime) {
        if(startTime.compareTo(sTime) == 1) {
            startTime.set(sTime);
        }
        
        if(endTime.compareTo(eTime) == -1) {
            endTime.set(eTime);
        }
    }
    
    public NetworkEntity clone() {
        NetworkEntity ent = new NetworkEntity(env);
        
        if(hostKey != null)
            ent.hostKey = hostKey.clone();
        
        if(key != null)
            ent.key = key.clone();
        
        ent.src = src.clone();
        ent.dst = dst.clone();
        ent.startTime = startTime.clone();
        ent.endTime = endTime.clone();
        
        return ent;
    }

    public int getByteSize() {
        return src.getByteSize() +
               dst.getByteSize() +
               startTime.getByteSize() +
               endTime.getByteSize() +
               hostKey.getByteSize() +         //NetworkEntityHostKey hostKey;
               key.getByteSize();              //NetworkEntryKey key
    }

    public void serialize(byte[] arr, int offset) {
        hostKey.serialize(arr, offset);
        offset += hostKey.getByteSize();
        
        key.serialize(arr, offset);
        offset += key.getByteSize();

        startTime.serialize(arr, offset);
        offset += startTime.getByteSize();
        
        endTime.serialize(arr, offset);
        offset += endTime.getByteSize();
        
        src.serialize(arr, offset);
        offset += src.getByteSize();
        
        dst.serialize(arr, offset);
        offset += dst.getByteSize();
    }

    public void unserialize(byte[] arr, int offset) {
        hostKey.unserialize(arr, offset);
        offset += hostKey.getByteSize();

        key.unserialize(arr, offset);
        offset += key.getByteSize();
        
        startTime.unserialize(arr, offset);
        offset += startTime.getByteSize();
        
        endTime.unserialize(arr, offset);
        offset += endTime.getByteSize();
        
        src.unserialize(arr, offset);
        offset += src.getByteSize();
        
        dst.unserialize(arr, offset);
        offset += dst.getByteSize();
    }
    
    public String toString() {
        StringBuilder b = new StringBuilder();
        if( (dbg_output & DBG_OUTPUT_KEY) != 0) {
            b.append(key.toString());
        }
        
        if( (dbg_output & DBG_OUTPUT_HOST_KEY) != 0) {
            b.append(" ");
            b.append(hostKey.toString());
        }
        
        /*if( (dbg_output & DBG_OUTPUT_START_ACT) != 0) {
            b.append(" Start Time: ");
            Util.getTimeStamp(b, startTime * 1000L, true, true);
        }
        
        if( (dbg_output & DBG_OUTPUT_END_ACT) != 0) {
            b.append(" End Time: ");
            Util.getTimeStamp(b, endTime * 1000L, true, true);
        }
        
        if( (dbg_output & DBG_OUTPUT_DATA_SENT) != 0) {
            b.append(" Sent: ");
            b.append(dataSent);
        }

        if( (dbg_output & DBG_OUTPUT_DATA_RECEIVED) != 0) {
            b.append(" Received: ");
            b.append(dataReceived);
        }*/

        return b.toString();
    }
    
    public class EntContent implements BoundObject {
        private transient int objSize;
        
        private long total;
        private long[] content;

        public EntContent() {
            content = new long[NetworkChannel.NUM_TYPES];
            
            objSize =
                    ByteUtils.LONG_SIZE + // dataSent
                    ByteUtils.LONG_SIZE * NetworkChannel.NUM_TYPES; // contentSent
        }
        
        public int getByteSize() {
            return objSize;
        }

        public void serialize(byte[] arr, int offset) {
            offset += ByteUtils.longToBytes(arr, offset, getDataSent());
            offset += ByteUtils.longArrToBytes(arr, offset, getContentSent(), 0, NetworkChannel.NUM_TYPES);
        }

        public void unserialize(byte[] arr, int offset) {
            total = ByteUtils.bytesToLong(arr, offset);
            offset += ByteUtils.LONG_SIZE;
            
            content = ByteUtils.bytesToLongArr(arr, offset, getContentSent(), 0, NetworkChannel.NUM_TYPES);
        }
        
        public EntContent clone() {
            EntContent c = new EntContent();
            
            c.total = this.getDataSent();
            c.content = new long[NetworkChannel.NUM_TYPES];
            
            System.arraycopy(getContentSent(), 0, c.getContentSent(), 0, NetworkChannel.NUM_TYPES);
            
            return c;
        }
        
        public void clear() {
            total = 0;
            
            for (int i = 0; i < content.length; ++i) {
                content[i] = 0;
            }
        }

        public long getDataSent() {
            return total;
        }

        public long[] getContentSent() {
            return content;
        }
        
        public void flow_addData(long data, long[] con) {
            total += data;
            for (int i = 0; i < con.length; ++i) {
                content[i] += con[i];
            }
        }
    }
}