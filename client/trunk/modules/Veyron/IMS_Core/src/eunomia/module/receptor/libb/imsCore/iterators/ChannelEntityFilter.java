/*
 * ChannelEntityFilter.java
 *
 * Created on January 26, 2008, 10:45 PM
 *
 */

package eunomia.module.receptor.libb.imsCore.iterators;

import eunomia.module.receptor.libb.imsCore.net.NetworkChannel;
import eunomia.module.receptor.libb.imsCore.net.NetworkEntityHostKey;

/**
 *
 * @author Mikhail Sosonkin
 */
public class ChannelEntityFilter implements IteratorFilter {
    public static final int ENTITY1 = 1;
    public static final int ENTITY2 = 2;
    public static final int EITHER_ENTITY = 3;
    
    private NetworkEntityHostKey k1;
    private NetworkEntityHostKey k2;
    private int op;

    public ChannelEntityFilter(NetworkEntityHostKey k1, NetworkEntityHostKey k2, int op) {
        this.k1 = k1;
        this.k2 = k2;
        this.op = op;
    }
    
    public boolean allow(Object o) {
        if (o instanceof NetworkChannel) {
            NetworkChannel ch = (NetworkChannel)o;
            NetworkEntityHostKey h1 = ch.getChannelFlowID().getSourceEntity();
            NetworkEntityHostKey h2 = ch.getChannelFlowID().getDestinationEntity();
            
            switch(op) {
                case ENTITY1: 
                    return h1.equals(k1);
                case ENTITY2: 
                    return h2.equals(k2);
                case EITHER_ENTITY: 
                    return h1.equals(k1) || h2.equals(k2);
            }
        }
        
        return false;
    }
}