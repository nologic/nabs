package eunomia.module.receptor.libb.imsCore.dns;

import eunomia.module.receptor.libb.imsCore.EnvironmentKey;
import eunomia.module.receptor.libb.imsCore.bind.ByteUtils;

/**
 *
 * @author Justin Stallard
 */
public class DNSFlowKey extends EnvironmentKey {
    private int key;
    
    public DNSFlowKey() {
        key = 0;
    }
    
    public DNSFlowKey(int key) {
        this.key = key;
    }
    
    public void setKey(int key) {
        this.key = key;
    }
    
    public int getKey() {
        return key;
    }

    @Override
    public int getEnvID() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public EnvironmentKey clone() {
        return new DNSFlowKey(key);
    }

    public int getByteSize() {
        return 4;
    }

    public void serialize(byte[] arr, int offset) {
        ByteUtils.intToBytes(arr, offset, key);
    }

    public void unserialize(byte[] arr, int offset) {
        key = ByteUtils.bytesToInt(arr, offset);
    }

}
