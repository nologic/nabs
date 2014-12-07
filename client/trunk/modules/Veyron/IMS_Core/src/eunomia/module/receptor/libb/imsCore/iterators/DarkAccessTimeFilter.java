/*
 * DarkAccessTimeFilter.java
 *
 * Created on January 26, 2008, 4:13 PM
 *
 */

package eunomia.module.receptor.libb.imsCore.iterators;

import eunomia.module.receptor.libb.imsCore.net.DarkAccess;

/**
 *
 * @author Mikhail Sosonkin
 */
public class DarkAccessTimeFilter implements IteratorFilter {
    private long start;
    private long end;
    
    public DarkAccessTimeFilter(long startTimeSeconds, long endTimeSeconds) {
        start = startTimeSeconds;
        end = endTimeSeconds;
    }

    public boolean allow(Object o) {
        if(o instanceof DarkAccess) {
            DarkAccess ds = (DarkAccess)o;
            
            long time = ds.getStartTime().getSeconds();
            
            return time >= start && time < end;
        }
        
        return false;
    }
}