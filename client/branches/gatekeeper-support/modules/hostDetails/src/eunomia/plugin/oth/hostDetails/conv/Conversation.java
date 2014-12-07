/*
 * Conversation.java
 *
 * Created on August 23, 2005, 2:37 PM
 *
 */

package eunomia.plugin.oth.hostDetails.conv;

import eunomia.receptor.module.NABFlow.NABFlow;
import eunomia.util.*;
import java.io.*;
import java.net.*;

/**
 *
 * @author Mikhail Sosonkin
 */
public class Conversation implements ResolveRequest {
    private long lastActive;
    private long inBytes;
    private long outBytes;
    private long total;
    private long rmt_ip;
    private int lcl_port;
    private int rmt_port;
    
    private int fCount;
    private int[] tfCount;
    private double[] tfPercent;
    
    private InetAddress rmtAddress;
    private String rmtHost;
    private String rmtPort;
    private String lclPort;
    private boolean sentForResolution;
    private boolean isUsed;

    public Conversation(){
        tfPercent = new double[NABFlow.NUM_TYPES];
        tfCount = new int[NABFlow.NUM_TYPES];
    }
    
    public Conversation(long rmt_ip, int lcl_port, int rmt_port) {
        this();
        this.rmt_ip = rmt_ip;
        this.lcl_port = lcl_port;
        this.rmt_port = rmt_port;
        sentForResolution = false;
        
        lastActive = System.currentTimeMillis();
    }
    
    public void incoming(int[] types, long size, long time){
        inBytes += size;
        
        updateBidirectional(types, size);
    }
    
    public void outgoing(int[] types, long size, long time){
        outBytes += size;
        
        updateBidirectional(types, size);
    }
    
    private void updateBidirectional(int[] types, long size){
        for (int i = 0; i < types.length; i++) {
            int t = types[i];
            tfCount[i] += t;
            fCount += t;
        }
        total += size;
        lastActive = System.currentTimeMillis();
    }
    
    public long getLastActive(){
        return lastActive;
    }
    
    public long getTotalBytes(){
        return total;
    }

    public long getOutBytes() {
        return outBytes;
    }
    
    public long getInBytes(){
        return inBytes;
    }

    public long getRmt_ip() {
        return rmt_ip;
    }

    public int getLcl_port() {
        return lcl_port;
    }

    public int getRmt_port() {
        return rmt_port;
    }

    public InetAddress getRmtAddress() {
        if(rmtAddress == null){
            rmtAddress = Util.getInetAddress(rmt_ip);
        }
        
        return rmtAddress;
    }

    public void setRmtAddress(InetAddress rmtAddress) {
        this.rmtAddress = rmtAddress;
    }

    public void setRmtHost(String str) {
        rmtHost = str;
    }
    
    public String getRmtHost() {
        if(rmtHost == null){
            if(rmtAddress != null && !sentForResolution){
                sentForResolution = true;
                HostResolver.addRequest(this);
            }
            
            return "(Resolving)";
        }
        
        return rmtHost;
    }

    public String getRmtPort() {
        if(rmtPort == null){
            rmtPort = Integer.toString(rmt_port);
        }
        
        return rmtPort;
    }

    public String getLclPort() {
        if(lclPort == null){
            lclPort = Integer.toString(lcl_port);
        }

        return lclPort;
    }
    
    public InetAddress getAddress(){
        return rmtAddress;
    }
    
    public void setResolved(String hName){
        rmtHost = hName;
    }
    
    public double[] getPercentTypes(){
        return tfPercent;
    }
    
    public void writeOut(DataOutputStream dout) throws IOException {
        for(int i = tfCount.length - 1; i != -1; --i){
            tfPercent[i] = ((double)tfCount[i])/((double)fCount);
        }
        dout.writeLong(lastActive);
        dout.writeLong(inBytes);
        dout.writeLong(outBytes);
        dout.writeLong(total);
        dout.writeLong(rmt_ip);
        dout.writeInt(lcl_port);
        dout.writeInt(rmt_port);
        
        for(int i = 0; i < tfPercent.length; ++i){
            int num = (int)(tfPercent[i] * 100.0);
            dout.writeByte((int)(num & 0xFF));
        }
    }
    
    public void readIn(DataInputStream din) throws IOException {
        lastActive = din.readLong();
        inBytes = din.readLong();
        outBytes = din.readLong();
        total = din.readLong();
        rmt_ip = din.readLong();
        lcl_port = din.readInt();
        rmt_port = din.readInt();

        for(int i = 0; i < tfPercent.length; ++i){
            int num = (int)(din.readByte() & 0xFF);
            tfPercent[i] = ((double)num)/100.0;
        }
    }

    public boolean isUsed() {
        return isUsed;
    }

    public void setUsed(boolean isUsed) {
        this.isUsed = isUsed;
    }
    
    void setInfo(Conversation conv){
        //Beware Text Buffers are not reset!!!!
        lastActive = conv.lastActive;
        inBytes = conv.inBytes;
        outBytes = conv.outBytes;
        total = conv.total;
        rmt_ip = conv.rmt_ip;
        lcl_port = conv.lcl_port;
        rmt_port = conv.rmt_port;
        
        for(int i = 0; i < tfPercent.length; ++i){
            tfPercent[i] = conv.tfPercent[i];
        }
    }
}