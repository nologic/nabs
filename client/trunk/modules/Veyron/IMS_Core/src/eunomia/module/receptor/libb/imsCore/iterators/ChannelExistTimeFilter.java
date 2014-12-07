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
public class ChannelExistTimeFilter implements IteratorFilter {
    
    private long start;
    private long end;
    
    public ChannelExistTimeFilter(long startTimeSeconds, long endTimeSeconds) {
        start = startTimeSeconds;
        end = endTimeSeconds;
    }

    public boolean allow(Object o) {
        if(o instanceof NetworkChannel) {
            NetworkChannel c = (NetworkChannel)o;
            
            long sTime = c.getStartTime().getSeconds();
            long eTime = c.getEndTime().getSeconds();
            
            return (sTime >= start && sTime < end) || (eTime >= start && eTime < end);
        }
        
        return false;
    }
}