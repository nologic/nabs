/*
 * ChannelSharedEntityFilter.java
 *
 * Created on January 24, 2008, 11:58 PM
 *
 */

package eunomia.module.receptor.libb.imsCore.iterators;

import eunomia.module.receptor.libb.imsCore.net.NetworkChannel;
import eunomia.module.receptor.libb.imsCore.net.NetworkEntityHostKey;

/**
 *
 * @author Mikhail Sosonkin
 */
public class ChannelSharedEntityFilter implements IteratorFilter {
    private NetworkEntityHostKey k1;
    private NetworkEntityHostKey k2;
    
    public ChannelSharedEntityFilter(NetworkEntityHostKey k1, NetworkEntityHostKey k2) {
        this.k1 = k1;
        this.k2 = k2;
    }

    public boolean allow(Object o) {
        if(o instanceof NetworkChannel) {
            NetworkChannel chan = (NetworkChannel)o;
            
            NetworkEntityHostKey h1 = chan.getChannelFlowID().getSourceEntity();
            NetworkEntityHostKey h2 = chan.getChannelFlowID().getDestinationEntity();
            
            return (h1.equals(k1) && h2.equals(k2)) || (h1.equals(k2) && h2.equals(k1));
        }
        
        return false;
    }
    
}
