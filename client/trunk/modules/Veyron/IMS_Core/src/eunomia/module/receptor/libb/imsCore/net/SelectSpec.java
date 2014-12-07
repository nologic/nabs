/*
 * TimeSpec.java
 *
 * Created on March 1, 2008, 2:43 PM
 *
 */

package eunomia.module.receptor.libb.imsCore.net;

import eunomia.module.receptor.libb.imsCore.iterators.ChannelExistTimeFilter;
import eunomia.module.receptor.libb.imsCore.iterators.ChannelLastTimeFilter;
import eunomia.module.receptor.libb.imsCore.iterators.DarkAccessTimeFilter;
import eunomia.module.receptor.libb.imsCore.iterators.EntityExistTimeFilter;
import eunomia.module.receptor.libb.imsCore.iterators.EntityLastTimeFilter;
import eunomia.module.receptor.libb.imsCore.iterators.IteratorFilter;

/**
 *
 * @author Mikhail Sosonkin
 */
public class SelectSpec {
    public static final int LAST_ACTIVITY = 0;
    public static final int START_ACTIVITY = 1;
    
    private long startTime;
    private long endTime;
    private int type;
    
    public SelectSpec(long sTime, long eTime, int type) {
        startTime = sTime;
        endTime = eTime;
        this.setType(type);
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
    
    IteratorFilter getDarkAccessTimeFilter() {
        return new DarkAccessTimeFilter(startTime, endTime);
    }
    
    IteratorFilter getChannelTimeFilter() {
        switch(type) {
            case SelectSpec.START_ACTIVITY:
                return new ChannelExistTimeFilter(startTime, endTime);
            case SelectSpec.LAST_ACTIVITY:
                return new ChannelLastTimeFilter(startTime, endTime);
        }
        
        throw new RuntimeException("Invalid type: " + type);
    }
    
    IteratorFilter getEntityTimeFilter() {
        switch(type) {
            case SelectSpec.START_ACTIVITY:
                return new EntityExistTimeFilter(startTime, endTime);
            case SelectSpec.LAST_ACTIVITY:
                return new EntityLastTimeFilter(startTime, endTime);
        }
        
        throw new RuntimeException("Invalid type: " + type);
    }
}