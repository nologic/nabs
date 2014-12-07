package eunomia.module.receptor.flow.DNSFlow;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author justin
 */

public class DNSFlowResponse {
    private String name;
    private char responseType;
    private int resourceDataLength;
    private byte[] resourceData;
    private int ttl;
    
    public DNSFlowResponse() {
        resourceData = new byte[0xFFFF];
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public char getResponseType() {
        return responseType;
    }
    
    public void setResponseType(char responseType) {
        this.responseType = responseType;
    }
    
    public long getResourceDataIP() {
        long ip = 0;
        
        ip |= (long) (resourceData[0] & 0xFFL);
        for (int i = 1; i < 4; ++i) {
            ip <<= 8;
            ip |= (long) (resourceData[i] & 0xFFL);
        }
        
        return ip;
    }
    
    public String getResourceDataName() {
        try {
            return new String(resourceData, 0, (int) resourceDataLength, "US-ASCII");
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(DNSFlowResponse.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
    
    public int getResourceDataMXPref() {
        int ret = 0;
        ret |= resourceData[0];
        ret <<= 8;
        ret |= resourceData[1];
        return ret;
    }
    
    public String getResourceDataMXName() {
        try {
            return new String(resourceData, 2, (int) resourceDataLength - 2, "US-ASCII");
        } catch (UnsupportedEncodingException ex) {
            ex.printStackTrace();
            return null;
        }
    }
    
    public byte[] getResourceData() {
        return resourceData;
    }
    
    public int getResourceDataLength() {
        return resourceDataLength;
    }
    
    public void setResourceData(byte[] resourceData, int length) {
        System.arraycopy(resourceData, 0, this.resourceData, 0, length);
        resourceDataLength = length;
    }
    
    public int getTTL() {
        return ttl;
    }
    
    public void setTTL(int ttl) {
        this.ttl = ttl;
    }
    
    public void readFromBuffer(ByteBuffer buff) throws UnsupportedEncodingException {
        ByteOrder order = buff.order();
        
        buff.order(ByteOrder.BIG_ENDIAN);
        
        resourceDataLength = buff.get() & 0x00FF;
        
        buff.get(resourceData, 0, resourceDataLength);
        name = new String(resourceData, 0, resourceDataLength, "US-ASCII");
        responseType = buff.getChar();
        
        // TODO (maybe) don't store resource data in a byte array
        resourceDataLength = buff.getShort() & 0x0000FFFF;
        buff.get(resourceData, 0, resourceDataLength);
        ttl = buff.getInt();
        
        buff.order(order);
    }
    
    public void writeToBuffer(ByteBuffer buff) {
        try {
            buff.put((byte) name.length());
            buff.put(name.getBytes("US-ASCII"));
            buff.putChar(responseType);
            buff.put((byte) resourceDataLength);
            buff.put(resourceData, 0, resourceDataLength);
            buff.putInt(ttl);
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(DNSFlowResponse.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}