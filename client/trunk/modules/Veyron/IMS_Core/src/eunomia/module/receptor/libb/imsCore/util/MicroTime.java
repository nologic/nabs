/*
 * NanoTime.java
 *
 * Created on July 29, 2008, 8:14 PM
 *
 */

package eunomia.module.receptor.libb.imsCore.util;

import eunomia.module.receptor.libb.imsCore.bind.BoundObject;
import eunomia.module.receptor.libb.imsCore.bind.ByteUtils;

/**
 *
 * @author Mikhail Sosonkin
 */
public class MicroTime implements Comparable, BoundObject {
    public static final int MICRO_TIME_SIZE = ByteUtils.INT_SIZE * 2;
    
    private long seconds;
    private long microSeconds;
    
    public MicroTime(boolean setMin) {
        setMinMax(setMin);
    }
    
    public MicroTime(long s, long ms) {
        seconds = s;
        microSeconds = ms;
    }
    
    public MicroTime clone() {
        MicroTime stamp = new MicroTime(seconds, microSeconds);
        
        return stamp;
    }
    
    public long getMilliSeconds() {
        return seconds * 1000L + microSeconds / 1000L;
    }

    public long getSeconds() {
        return seconds;
    }

    public long getMicroSeconds() {
        return microSeconds;
    }

    public void set(long seconds, long microSeconds) {
        this.microSeconds = microSeconds;
        this.seconds = seconds;
    }
    
    public void set(MicroTime time) {
        this.seconds = time.seconds;
        this.microSeconds = time.microSeconds;
    }
    
    public void setMinMax(boolean min) {
        if(min) {
            set(Long.MIN_VALUE, Long.MIN_VALUE);
        } else {
            set(Long.MAX_VALUE, Long.MAX_VALUE);
        }
    }

    public int compareTo(Object o) {
        return compareTo((MicroTime)o);
    }
    
    public int compareTo(MicroTime mTime) {
        long ts1 = seconds;
        long tm1 = microSeconds;
        long ts2 = mTime.seconds;
        long tm2 = mTime.microSeconds;
        
        if(ts1 == ts2) {
            if(tm1 == tm2) {
                return 0;
            } else if(tm1 < tm2) {
                return -1;
            } else {
                return 1;
            }
        } else if(ts1 < ts2) {
            return -1;
        } else {
            return 1;
        }
    }

    public int getByteSize() {
        return MICRO_TIME_SIZE;
    }

    public void serialize(byte[] arr, int offset) {
        offset += ByteUtils.intToBytes(arr, offset, (int)(seconds & 0xFFFFFFFF));
        
        ByteUtils.intToBytes(arr, offset, (int)(microSeconds & 0xFFFFFFFF));
    }

    public void unserialize(byte[] arr, int offset) {
        seconds = ByteUtils.bytesToInt(arr, offset);
        microSeconds = ByteUtils.bytesToInt(arr, offset + 4);
    }
    
    public String toString() {
        return seconds + "." + microSeconds;
    }
}