/*
 * EntityTimeFilter.java
 *
 * Created on January 24, 2008, 9:37 PM
 *
 */

package eunomia.module.receptor.libb.imsCore.iterators;

import com.vivic.eunomia.sys.util.Util;
import eunomia.module.receptor.libb.imsCore.net.NetworkEntity;

/**
 *
 * @author Mikhail Sosonkin
 */
public class EntityLastTimeFilter implements IteratorFilter {
    private long start;
    private long end;
    
    public EntityLastTimeFilter(long startTimeSeconds, long endTimeSeconds) {
        start = startTimeSeconds;
        end = endTimeSeconds;
    }

    public boolean allow(Object o) {
        if(o instanceof NetworkEntity) {
            NetworkEntity ent = (NetworkEntity)o;
            
            long time = ent.getEndTime().getSeconds();
            
            return time >= start && time < end;
        }
        
        return false;
    }
}