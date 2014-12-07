/*
 * DarkAccessSourceFilter.java
 *
 * Created on March 10, 2008, 7:44 PM
 *
 */

package eunomia.module.receptor.libb.imsCore.iterators;

import eunomia.module.receptor.libb.imsCore.net.DarkAccess;

/**
 *
 * @author Mikhail Sosonkin
 */
public class DarkAccessSourceFilter implements IteratorFilter {
    private long sip;
    
    // possibly needs to be expanded for a more general case (i.e. ranges)
    public DarkAccessSourceFilter(long ip) {
        sip = ip;
    }

    public boolean allow(Object o) {
        if(o instanceof DarkAccess) {
            DarkAccess da = (DarkAccess)o;
            
            return da.getFlowID().getSourceEntity().getIPv4() == sip;
        }
        
        return false;
    }
}