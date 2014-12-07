/*
 * ChannelEntitySubnetFilter.java
 *
 * Created on January 27, 2008, 10:28 AM
 *
 */

package eunomia.module.receptor.libb.imsCore.iterators;

import eunomia.module.receptor.libb.imsCore.net.NetworkChannel;
import eunomia.module.receptor.libb.imsCore.net.NetworkChannelFlowID;
import eunomia.module.receptor.libb.imsCore.net.NetworkDefinition;

/**
 *
 * @author Mikhail Sosonkin
 */
public class ChannelSubnetFilter implements IteratorFilter {
    private NetworkDefinition ndef;
    private boolean src;
    
    public ChannelSubnetFilter(NetworkDefinition def, boolean source) {
        ndef = def;
        src = source;
    }

    public boolean allow(Object o) {
        if(o instanceof NetworkChannel) {
            NetworkChannel ch = (NetworkChannel)o;
            NetworkChannelFlowID fid = ch.getChannelFlowID();
            
            long inSub = (src?fid.getSourceEntity().getIPv4():fid.getDestinationEntity().getIPv4());
            
            return ndef.isInNetowrk(inSub);
        }
        
        return false;
    }
}