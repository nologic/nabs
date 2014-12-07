/*
 * DarkAccessKey.java
 *
 * Created on January 26, 2008, 12:15 PM
 *
 */

package eunomia.module.receptor.libb.imsCore.net;

import eunomia.module.receptor.libb.imsCore.EnvironmentKey;
import eunomia.module.receptor.libb.imsCore.bind.ByteUtils;

/**
 *
 * @author Mikhail Sosonkin
 */
public class NetworkEntryKey extends EnvironmentKey {
    private long num;
    
    public NetworkEntryKey() {
    }

    public NetworkEntryKey(long num) {
        this.num = num;
    }

    public long getNum() {
        return num;
    }

    public void setNum(long num) {
        this.num = num;
    }
    
    public boolean equals(Object o) {
        if(o instanceof NetworkEntryKey) {
            return num == ((NetworkEntryKey)o).num;
        }
        
        return false;
    }
    
    public NetworkEntryKey clone() {
        NetworkEntryKey key = new NetworkEntryKey();
        key.num = num;
        
        return key;
    }

    public int getByteSize() {
        return 8;
    }

    public void serialize(byte[] arr, int offset) {
        ByteUtils.longToBytes(arr, offset, num);
    }

    public void unserialize(byte[] arr, int offset) {
        num = ByteUtils.bytesToLong(arr, offset);
    }

    public int getEnvID() {
        return (int)num;
    }
    
    public String toString() {
        return "nek: " + num;
    }
}