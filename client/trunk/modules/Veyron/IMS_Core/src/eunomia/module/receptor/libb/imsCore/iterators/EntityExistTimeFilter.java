/*
 * EntityTimeFilter.java
 *
 * Created on January 24, 2008, 9:37 PM
 *
 */

package eunomia.module.receptor.libb.imsCore.iterators;

import eunomia.module.receptor.libb.imsCore.net.NetworkEntity;

/**
 *
 * @author Mikhail Sosonkin
 */
public class EntityExistTimeFilter implements IteratorFilter {
    
    private long start;
    private long end;
    
    public EntityExistTimeFilter(long startTimeSeconds, long endTimeSeconds) {
        start = startTimeSeconds;
        end = endTimeSeconds;
    }

    public boolean allow(Object o) {
        if(o instanceof NetworkEntity) {
            NetworkEntity ent = (NetworkEntity)o;
            
            long sTime = ent.getStartTime().getSeconds();
            long eTime = ent.getEndTime().getSeconds();
            
            return (sTime >= start && sTime < end) || (eTime >= start && eTime < end);
        }
        
        return false;
    }
}