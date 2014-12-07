/*
 * NetworkEntityKey.java
 *
 * Created on December 6, 2007, 9:10 PM
 *
 */

package eunomia.module.receptor.libb.imsCore.net;

import com.vivic.eunomia.sys.util.Util;
import eunomia.module.receptor.libb.imsCore.EnvironmentKey;
import eunomia.module.receptor.libb.imsCore.bind.ByteUtils;
import java.util.Arrays;

/**
 *
 * @author Mikhail Sosonkin
 */
public class NetworkEntityHostKey extends EnvironmentKey {
    public static final byte ID_IPV4 = 0;
    
    private static final int[] TYPE_SIZE = {4};
    
    private volatile int eId;
    
    private byte idType;
    private int[] identifier;
    
    public NetworkEntityHostKey(int ipv4) {
        setKey(ipv4);
    }
    
    public NetworkEntityHostKey(int[] id) {
        setKey(id);
    }
    
    public NetworkEntityHostKey() {
        setKey(0);
    }
    
    public long getIPv4() {
        return identifier[0] & 0xFFFFFFFFL;
    }
    
    public void setIPv4(long ip) {
        identifier[0] = (int)ip;
    }
    
    public void setKey(int id) {
        identifier = new int[]{id};
    }
    
    public void setKey(int[] id) {
        if(id != null) {
            identifier = new int[id.length];
            System.arraycopy(id, 0, identifier, 0, id.length);
        }
    }
    
    public int hashCode() {
        return identifier[0];
    }
    
    public boolean equals(Object o) {
        NetworkEntityHostKey key = (NetworkEntityHostKey)o;
        
        return Arrays.equals(key.identifier, identifier);
    }
    
    public NetworkEntityHostKey clone() {
        return new NetworkEntityHostKey(identifier);
    }
    
    public String toString() {
        return Util.ipToString((long)identifier[0] & 0xFFFFFFFF);
    }

    public int getByteSize() {
        return identifier.length * 4 + 1;
    }

    public void serialize(byte[] arr, int offset) {
        arr[offset++] = idType;

        for (int i = 0; i < identifier.length; i++) {
            offset += ByteUtils.intToBytes(arr, offset, identifier[i]);
        }
    }

    public void unserialize(byte[] arr, int offset) {
        idType = arr[offset++];
        
        identifier = new int[TYPE_SIZE[idType] / 4];
        
        for (int i = 0; i < identifier.length; i++) {
            identifier[i] = ByteUtils.bytesToInt(arr, offset);
            offset += 4;
        }
    }

    public int getEnvID() {
        // keep it positive
        return identifier[0] & 0x0000FFFF;
    }
    
    public static NetworkEntityHostKey wrapIPv4(long ip) {
        return new NetworkEntityHostKey((int)ip);
    }
}