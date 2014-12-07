/*
 * EntityTimeFilter.java
 *
 * Created on January 24, 2008, 9:37 PM
 *
 */

package eunomia.module.receptor.libb.imsCore.iterators;

import eunomia.module.receptor.libb.imsCore.net.NetworkChannel;

/**
 *
 * @author Mikhail Sosonkin
 */
public class ChannelLastTimeFilter implements IteratorFilter {
    
    private long start;
    private long end;
    
    public ChannelLastTimeFilter(long startTimeSeconds, long endTimeSeconds) {
        start = startTimeSeconds;
        end = endTimeSeconds;
    }

    public boolean allow(Object o) {
        if(o instanceof NetworkChannel) {
            NetworkChannel c = (NetworkChannel)o;
            
            long time = c.getEndTime().getSeconds();

            return time >= start && time < end;
        }
        
        return false;
    }
}