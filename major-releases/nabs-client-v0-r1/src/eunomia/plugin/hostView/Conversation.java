/*
 * Conversation.java
 *
 * Created on August 23, 2005, 2:37 PM
 *
 */

package eunomia.plugin.hostView;

import eunomia.util.*;
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
    
    private InetAddress rmtAddress;
    private String rmtHost;
    private String rmtPort;
    private String lclPort;
    private boolean sentForResolution;

    public Conversation(long rmt_ip, int lcl_port, int rmt_port) {
        this.rmt_ip = rmt_ip;
        this.lcl_port = lcl_port;
        this.rmt_port = rmt_port;
        sentForResolution = false;
        
        lastActive = System.currentTimeMillis();
    }
    
    public void incoming(long size, int type){
        inBytes += size;
        
        updateBidirectional(size, type);
    }
    
    public void outgoing(long size, int type){
        outBytes += size;
        
        updateBidirectional(size, type);
    }
    
    private void updateBidirectional(long size, int type){
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
}