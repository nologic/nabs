/*
 * TimeStamp.java
 *
 * Created on February 29, 2008, 3:00 PM
 *
 */

package eunomia.receptor.module.NEOFlow;

import java.nio.ByteBuffer;

/**
 *
 * @author Mikhail Sosonkin
 */
public class TimeStamp {
    private long seconds;
    private long microSeconds;
    
    public TimeStamp() {
    }
    
    public TimeStamp clone() {
        TimeStamp stamp = new TimeStamp();
        
        stamp.seconds = seconds;
        stamp.microSeconds = microSeconds;
        
        return stamp;
    }
    
    public static TimeStamp readTimeStamp(TimeStamp stamp, ByteBuffer b) {
        if(stamp == null) {
            stamp = new TimeStamp();
        }
        
        stamp.seconds = (long)(b.getInt() & 0xFFFFFFFFL);
        stamp.microSeconds = (long)(b.getInt() & 0xFFFFFFFFL);
        
        return stamp;
    }
    
    public long getMilliSeconds() {
        return seconds * 1000L + microSeconds / 1000L;
    }

    public long getSeconds() {
        return seconds;
    }

    public void setSeconds(long seconds) {
        this.seconds = seconds;
    }

    public long getMicroSeconds() {
        return microSeconds;
    }

    public void setMicroSeconds(long microSeconds) {
        this.microSeconds = microSeconds;
    }
    
}