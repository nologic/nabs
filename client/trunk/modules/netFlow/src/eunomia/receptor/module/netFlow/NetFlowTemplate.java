/*
 * NetFlowTemplate.java
 *
 * Created on August 28, 2006, 11:40 PM
 *
 */

package eunomia.receptor.module.netFlow;

import com.vivic.eunomia.sys.util.Util;
import java.nio.ByteBuffer;

/**
 *
 * @author Mikhail Sosonkin
 */
public class NetFlowTemplate {
    // possibly need to perform caching on locations of IP's Port's to speed up the Flow interfaces.
    private int templateID;
    private short[] fieldType;
    private int[] fieldLength;
    private byte[][] fieldValue;
    private int fieldCount;
    private int dataSetLength;
    
    public NetFlowTemplate(int id) {
        templateID = id;
    }
    
    public int hashCode(){
        return templateID;
    }
    
    public boolean equals(Object o){
        return o.hashCode() == hashCode();
    }
    
    public int getDataSetLength(){
        return dataSetLength;
    }
    
    public int getInt(int i){
        return Util.bytesToInt(fieldValue[i], true);
    }
    
    public short getShort(int i){
        return Util.bytesToShort(fieldValue[i], true);
    }
    
    public byte[] getField(int i){
        return fieldValue[i];
    }
    
    public int getIntByType(short type){
        int i = getTypeIndex(type);
        if(i == -1){
            return getInt(i);
        }
        
        return 0;
    }
    
    public short getShortByType(short type){
        int i = getTypeIndex(type);
        if(i == -1){
            return getShort(i);
        }
        
        return 0;
    }
    
    public int getTypeIndex(short type){
        //is search the best choice?
        for (int i = 0; i < fieldType.length; i++) {
            if(fieldType[i] == type){
                return i;
            }
        }
        
        return -1;
    }
    
    public byte[] getFieldByType(short type){
        int i = getTypeIndex(type);
        if(i != -1){
            return fieldValue[i];
        }
        
        return null;
    }
    
    public void updateTemplate(ByteBuffer buff){
        //templateID = buff.getShort() & 0xFFFF;
        fieldCount = buff.getShort() & 0xFFFF;
        dataSetLength = 0;
        
        if(fieldType == null || fieldType.length != fieldCount){ // is this enough of a check?
            fieldType = new short[fieldCount];
            fieldLength = new int[fieldCount];
            fieldValue = new byte[fieldCount][];
        }
        
        short type;
        int length;
        for (int i = 0; i < fieldCount; i++) {
            type = buff.getShort();
            length = buff.getShort() & 0xFFFF;
            
            fieldType[i] = type;
            fieldLength[i] = length;
            dataSetLength += length;
            
            if(fieldValue[i] == null || fieldValue[i].length != length){
                fieldValue[i] = new byte[length];
            }
        }
    }
    
    public void parseData(ByteBuffer buff){
        for (int i = 0; i < fieldCount; i++) {
            buff.get(fieldValue[i]);
        }
    }
}