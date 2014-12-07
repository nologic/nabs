/*
 * Flow.java
 *
 * Created on June 1, 2005, 12:53 PM
 */

package eunomia.core.data.flow;

import eunomia.core.data.streamData.client.*;

import java.io.*;
import java.util.*;
import java.net.*;
import eunomia.core.data.*;

/**
 *
 * @author  Mikhail Sosonkin
 */
public class Flow {
    public static final int NUM_TYPES = 8;
    public static final int DT_Plain_Text = 0;
    public static final int DT_Image_BMP = 1;
    public static final int DT_Audio_WAV = 2;
    public static final int DT_Compressed = 3;
    public static final int DT_Image_JPG = 4;
    public static final int DT_Audio_MP3 = 5;
    public static final int DT_Video_MPG = 6;
    public static final int DT_Encrypted = 7;
    public static final String[] typeNames = {"Plain-Text", "Image-BMP", 
        "Audio-WAV", "Compressed", "Image-JPG", "Audio-MP3", "Video-MPG", 
        "Encrypted"};
    public static final List typeNamesList = Collections.unmodifiableList(Arrays.asList(typeNames));
    
    private NabsClient nabsClient;
    
    private long time;
    private InetAddress srcIpAdd;
    private InetAddress dstIpAdd;
    private long srcIp;
    private long dstIp;
    private int srcPort;
    private int dstPort;
    private int type;
    private int size;
    
    //working buff
    private byte[] buff;
    private byte[] ipWorkBytes;
    
    //elimenate loops in the broadcast graph
    private Object[] presence;
    
    public Flow(NabsClient nc){
        nabsClient = nc;
        ipWorkBytes = new byte[4];
        buff = new byte[21];
        size = 16384;
        presence = new Object[10];
    }
    
    public void addPresence(Object o){
        for(int i = presence.length - 1; i != -1; --i){
            if(presence[i] == null){
                presence[i] = o;
                return;
            }
        }
        
        Object[] newP = new Object[presence.length * 2];
        System.arraycopy(presence, 0, newP, 0, presence.length);
        presence = newP;
        addPresence(o);
    }
    
    public void removePresence(Object o){
        for(int i = presence.length - 1; i != -1; --i){
            if(presence[i] == o){
                presence[i] = null;
                return;
            }
        }
    }
    
    public boolean isPresent(Object o){
        for(int i = presence.length - 1; i != -1; --i){
            if(presence[i] == o){
                return true;
            }
        }
        
        return false;
    }
    
    public long getTime(){
        return time;
    }
    
    public int getSize(){
        return size;
    }
    
    public long getSourceIp(){
        return srcIp;
    }
    
    public InetAddress getSourceAddress(){
        if(srcIpAdd == null){
            synchronized(ipWorkBytes){
                byte[] ipBytes = ipWorkBytes;
                long ipAdd = srcIp;

                ipBytes[0] = (byte)(ipAdd >> 24);
                ipBytes[1] = (byte)(ipAdd >> 16);
                ipBytes[2] = (byte)(ipAdd >> 8 );
                ipBytes[3] = (byte)(ipAdd      );
                try {
                    srcIpAdd = InetAddress.getByAddress(ipBytes);
                } catch(Exception e){
                    srcIpAdd = null;
                }
            }
        }
        
        return srcIpAdd;
    }
    
    public int getSourcePort(){
        return srcPort;
    }
    
    public long getDestinationIp(){
        return dstIp;
    }
    
    public InetAddress getDestinationAddress(){
        if(dstIpAdd == null){
            synchronized(ipWorkBytes){
                byte[] ipBytes = ipWorkBytes;
                long ipAdd = dstIp;

                ipBytes[0] = (byte)(ipAdd >> 24);
                ipBytes[1] = (byte)(ipAdd >> 16);
                ipBytes[2] = (byte)(ipAdd >> 8 );
                ipBytes[3] = (byte)(ipAdd      );
                try {
                    dstIpAdd = InetAddress.getByAddress(ipBytes);
                } catch(Exception e){
                    dstIpAdd = null;
                }
            }
        }
        
        return dstIpAdd;
    }
        
    public int getDestinationPort(){
        return dstPort;
    }
    
    public int getType(){
        return type;
    }
    
    public NabsClient getNabsClient() {
        return nabsClient;
    }

    public void takeFrom(Flow flow){
        time = flow.time;
        srcIp = flow.srcIp;
        dstIp = flow.dstIp;
        srcPort = flow.srcPort;
        dstPort = flow.dstPort;
        type = flow.type;
        
        srcIpAdd = null;
        dstIpAdd = null;
    }
    
    public String toString(){
        // performance hazard.
        StringBuffer buff = new StringBuffer();
        buff.append(new Date(time).toString());
        buff.append("> ");
        buff.append(getSourceAddress().getHostAddress());
        buff.append(":" + srcPort);
        buff.append(" to ");
        buff.append(getDestinationAddress().getHostAddress());
        buff.append(":" + dstPort);
        buff.append(" | ");
        buff.append(typeNames[type]);
        buff.append(" |");
        
        return buff.toString();
    }
    
    public String getSQLInsertString(){
        // performance hazard
        StringBuffer buff = new StringBuffer();
        buff.append("(" + time);
        buff.append("," + srcIp);
        buff.append("," + srcPort);
        buff.append("," + dstIp);
        buff.append("," + dstPort);
        buff.append(",");
        buff.append(type + ")");
        
        return buff.toString();
    }
    
    public void writeToDataStream(DataOutputStream dout) throws IOException {
        int count = 0;
        
        long workLong;
        int workInt; 
        
        workLong = time;
        buff[count++] = (byte)(workLong >> 24);
        buff[count++] = (byte)(workLong >> 16);
        buff[count++] = (byte)(workLong >> 8 );
        buff[count++] = (byte)(workLong      );
        
        workInt = size;
        buff[count++] = (byte)(workInt >> 24);
        buff[count++] = (byte)(workInt >> 16);
        buff[count++] = (byte)(workInt >> 8 );
        buff[count++] = (byte)(workInt      );
        
        workLong = srcIp;
        buff[count++] = (byte)(workLong >> 24);
        buff[count++] = (byte)(workLong >> 16);
        buff[count++] = (byte)(workLong >> 8 );
        buff[count++] = (byte)(workLong      );

        workLong = dstIp;
        buff[count++] = (byte)(workLong >> 24);
        buff[count++] = (byte)(workLong >> 16);
        buff[count++] = (byte)(workLong >> 8 );
        buff[count++] = (byte)(workLong      );

        workInt = srcPort;
        buff[count++] = (byte)(workInt >> 8 );
        buff[count++] = (byte)(workInt      );

        workInt = dstPort;
        buff[count++] = (byte)(workInt >> 8 );
        buff[count++] = (byte)(workInt      );
        
        buff[count++] = (byte)(type);

        dout.write(buff);
    }

    public void readFromDataStream(DataInputStream din) throws IOException {
        int count = 0;
        int read = 0;
        
        long workLong1 = 0;
        long workLong2 = 0;
        long workLong3 = 0;
        long workLong4 = 0;
        
        int workInt1 = 0;
        int workInt2 = 0;
        int workInt3 = 0;
        int workInt4 = 0;
        
        din.readFully(buff);
        
        workLong1 = buff[count++] & 0xFFL;
        workLong1 = workLong1 << 24;
        workLong2 = buff[count++] & 0xFFL;
        workLong2 = workLong2 << 16;
        workLong3 = buff[count++] & 0xFFL;
        workLong3 = workLong3 << 8;
        workLong4 = buff[count++] & 0xFFL;
        time = workLong1 | workLong2 | workLong3 | workLong4;
        
        workInt1 = buff[count++] & 0xFF;
        workInt1 = workInt1 << 24;
        workInt2 = buff[count++] & 0xFF;
        workInt2 = workInt2 << 16;
        workInt3 = buff[count++] & 0xFF;
        workInt3 = workInt3 << 8;
        workInt4 = buff[count++] & 0xFF;
        size = workInt1 | workInt2 | workInt3 | workInt4;

        workLong1 = buff[count++] & 0xFFL;
        workLong1 = workLong1 << 24;
        workLong2 = buff[count++] & 0xFFL;
        workLong2 = workLong2 << 16;
        workLong3 = buff[count++] & 0xFFL;
        workLong3 = workLong3 << 8;
        workLong4 = buff[count++] & 0xFFL;
        srcIp = workLong1 | workLong2 | workLong3 | workLong4;

        workLong1 = buff[count++] & 0xFFL;
        workLong1 = workLong1 << 24;
        workLong2 = buff[count++] & 0xFFL;
        workLong2 = workLong2 << 16;
        workLong3 = buff[count++] & 0xFFL;
        workLong3 = workLong3 << 8;
        workLong4 = buff[count++] & 0xFFL;
        dstIp = workLong1 | workLong2 | workLong3 | workLong4;

        workInt1 = buff[count++] & 0xFF;
        workInt1 = workInt1 << 8;
        workInt2 = buff[count++] & 0xFF;
        srcPort = workInt1 | workInt2;

        workInt1 = buff[count++] & 0xFF;
        workInt1 = workInt1 << 8;
        workInt2 = buff[count++] & 0xFF;
        dstPort = workInt1 | workInt2;
        
        // 0x07 to prevent it from producing an index of greater than 7
        type = buff[count++] & 0x7;

        srcIpAdd = null;
        dstIpAdd = null;       
    }
}