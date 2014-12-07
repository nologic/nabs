/*
 * ChannelKey.java
 *
 * Created on January 12, 2008, 1:09 PM
 *
 */

package eunomia.module.receptor.libb.imsCore.net;

import eunomia.module.receptor.libb.imsCore.bind.BoundObject;
import eunomia.module.receptor.libb.imsCore.bind.ByteUtils;
import java.io.Serializable;

/**
 *
 * @author Mikhail Sosonkin
 */
public class NetworkChannelFlowID implements BoundObject {
    public static final int PROTOCOL_TCP = 6;
    public static final int PROTOCOL_UDP = 17;
    
    private NetworkEntityHostKey ne1;
    private NetworkEntityHostKey ne2;
    private int port1;
    private int port2;
    private int protocol;
    
    public NetworkChannelFlowID() {
        ne1 = new NetworkEntityHostKey();
        ne2 = new NetworkEntityHostKey();
    }
    
    public void setKey(NetworkEntityHostKey n1, NetworkEntityHostKey n2, int p1, int p2, int proto) {
        ne1 = n1;
        ne2 = n2;
        port1 = p1;
        port2 = p2;
        protocol = proto;
    }
    
    public void setPortsProto(int p1, int p2, int proto) {
        port1 = p1;
        port2 = p2;
        protocol = proto;
    }
    
    public NetworkChannelFlowID flip() {
        NetworkEntityHostKey ne1_tmp = ne1;
        int port1_tmp = port1;
        
        ne1 = ne2;
        port1 = port2;
        ne2 = ne1_tmp;
        port2 = port1_tmp;
        
        return this;
    }
    
    public int hashCode() {
        return ne1.hashCode() & ne2.hashCode();
    }
    
    public boolean equals(Object o) {
        if(o instanceof NetworkChannelFlowID) {
            NetworkChannelFlowID k = (NetworkChannelFlowID)o;
            
            return ne1.equals(k.ne1) && ne2.equals(k.ne2) && port1 == k.port1 && port2 == k.port2 && protocol == k.protocol;
        }
        
        return false;
    }

    public NetworkEntityHostKey getSourceEntity() {
        return ne1;
    }

    public NetworkEntityHostKey getDestinationEntity() {
        return ne2;
    }

    public int getSourcePort() {
        return port1;
    }

    public int getDestinationPort() {
        return port2;
    }
    
    public int getProtocol() {
        return protocol;
    }
    
    public NetworkChannelFlowID clone() {
        NetworkChannelFlowID key = new NetworkChannelFlowID();
        
        key.setKey(ne1.clone(), ne2.clone(), port1, port2, protocol);
        
        return key;
    }
    
    public String toString() {
        return ne1 + ":" + port1 + " and " + ne2 + ":" + port2;
    }

    public int getByteSize() {
        return ne1.getByteSize() + ne2.getByteSize() + 12;
    }

    public void serialize(byte[] arr, int offset) {
        offset += ByteUtils.intToBytes(arr, offset, port1);
        offset += ByteUtils.intToBytes(arr, offset, port2);
        offset += ByteUtils.intToBytes(arr, offset, protocol);
        
        ne1.serialize(arr, offset);
        offset += ne1.getByteSize();
        
        ne2.serialize(arr, offset);
    }

    public void unserialize(byte[] arr, int offset) {
        port1 = ByteUtils.bytesToInt(arr, offset);
        offset += 4;
        
        port2 = ByteUtils.bytesToInt(arr, offset);
        offset += 4;
        
        protocol = ByteUtils.bytesToInt(arr, offset);
        offset += 4;

        if(ne1 == null) 
            ne1 = new NetworkEntityHostKey();
        ne1.unserialize(arr, offset);
        offset += ne1.getByteSize();
        
        if(ne2 == null) 
            ne2 = new NetworkEntityHostKey();
        ne2.unserialize(arr, offset);
    }
}