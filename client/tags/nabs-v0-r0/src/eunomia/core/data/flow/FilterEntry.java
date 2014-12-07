/*
 * FilterEntry.java
 *
 * Created on July 31, 2005, 2:50 PM
 */

package eunomia.core.data.flow;

import java.net.*;

/**
 *
 * @author Mikhail Sosonkin
 */
public class FilterEntry {
    private int[] src_lip;
    private int[] src_uip;
    private int src_lport;
    private int src_uport;
    
    private int[] dst_lip;
    private int[] dst_uip;
    private int dst_lport;
    private int dst_uport;
    
    private boolean isSrcIPSet;
    private boolean isSrcPortSet;
    private boolean isDstIPSet;
    private boolean isDstPortSet;
    
    private int[] workBytes;
    
    private long inRange;
    private long outRange;
    
    public FilterEntry() {
        isSrcIPSet = false;
        isSrcPortSet = false;
        isDstIPSet = false;
        isDstPortSet = false;
        
        workBytes = new int[4];
        src_lip = new int[4];
        src_uip = new int[4];
        dst_lip = new int[4];
        dst_uip = new int[4];
        
        inRange = 0;
        outRange = 0;
    }
    
    public boolean inRange(long sIp, long dIp, int sPort, int dPort){
        int[] lByte;
        int[] uByte;
        int[] wByte = workBytes;
        int i;
        
        if(isSrcIPSet){
            lByte = src_lip;
            uByte = src_uip;
            wByte[0] = (int)(sIp >> 24) & 0xFF;
            wByte[1] = (int)(sIp >> 16) & 0xFF;
            wByte[2] = (int)(sIp >> 8 ) & 0xFF;
            wByte[3] = (int)(sIp      ) & 0xFF;
            
            for(i = 3; i != -1; --i){
                int w = wByte[i];
                if(w < lByte[i] || w > uByte[i]){
                    ++outRange;
                    return false;
                }
            }
        }
        
        if(isDstIPSet){
            lByte = dst_lip;
            uByte = dst_uip;
            wByte[0] = (int)(dIp >> 24) & 0xFF;
            wByte[1] = (int)(dIp >> 16) & 0xFF;
            wByte[2] = (int)(dIp >> 8 ) & 0xFF;
            wByte[3] = (int)(dIp      ) & 0xFF;
            
            for(i = 3; i != -1; --i){
                int w = wByte[i];
                if(w < lByte[i] || w > uByte[i]){
                    ++outRange;
                    return false;
                }
            }
        }

        if(isSrcPortSet && (sPort < src_lport || sPort > src_uport)){
            ++outRange;
            return false;
        }

        if(isDstPortSet && (dPort < dst_lport || dPort > dst_uport)){
            ++outRange;
            return false;
        }

        ++inRange;
        return true;
    }
    
    public long getInRange(){
        return inRange;
    }
    
    public long getOutRange(){
        return outRange;
    }
    
    public void setSourceIpRange(long ip1, long ip2){
        src_lip[0] = ((int)(ip1 >> 24)) & 0xFF;
        src_lip[1] = ((int)(ip1 >> 16)) & 0xFF;
        src_lip[2] = ((int)(ip1 >> 8 )) & 0xFF;
        src_lip[3] = ((int)(ip1      )) & 0xFF;
        
        src_uip[0] = ((int)(ip2 >> 24)) & 0xFF;
        src_uip[1] = ((int)(ip2 >> 16)) & 0xFF;
        src_uip[2] = ((int)(ip2 >> 8 )) & 0xFF;
        src_uip[3] = ((int)(ip2      )) & 0xFF;
        
        isSrcIPSet = true;
    }
    
    public void setSourceIpRange(InetAddress ip1, InetAddress ip2){
        byte[] tmpByte = ip1.getAddress();
        for(int i = 0; i < tmpByte.length; i++){
            src_lip[i] = tmpByte[i] & 0xFF;
        }
        tmpByte = ip2.getAddress();
        for(int i = 0; i < tmpByte.length; i++){
            src_uip[i] = tmpByte[i] & 0xFF;
        }

        isSrcIPSet = true;
    }
    
    public void setSourceIpRange(int[] ip1, int[] ip2){
        for(int i = 0; i < ip1.length; ++i){
            src_lip[i] = ip1[i];
            src_uip[i] = ip2[i];
        }
        
        isSrcIPSet = true;
    }
    
    public void setSourcePortRange(int port1, int port2){
        src_lport = port1;
        src_uport = port2;
        isSrcPortSet = true;
    }

    public void setDestinationIpRange(long ip1, long ip2){
        dst_lip[0] = ((int)(ip1 >> 24)) & 0xFF;
        dst_lip[1] = ((int)(ip1 >> 16)) & 0xFF;
        dst_lip[2] = ((int)(ip1 >> 8 )) & 0xFF;
        dst_lip[3] = ((int)(ip1      )) & 0xFF;
        
        dst_uip[0] = ((int)(ip2 >> 24)) & 0xFF;
        dst_uip[1] = ((int)(ip2 >> 16)) & 0xFF;
        dst_uip[2] = ((int)(ip2 >> 8 )) & 0xFF;
        dst_uip[3] = ((int)(ip2      )) & 0xFF;
        
        isDstIPSet = true;
    }

    public void setDestinationIpRange(InetAddress ip1, InetAddress ip2){
        byte[] tmpByte = ip1.getAddress();
        for(int i = 0; i < tmpByte.length; i++){
            dst_lip[i] = tmpByte[i] & 0xFF;
        }
        tmpByte = ip2.getAddress();
        for(int i = 0; i < tmpByte.length; i++){
            dst_uip[i] = tmpByte[i] & 0xFF;
        }
        
        isDstIPSet = true;
    }
    
    public void setDestinationIpRange(int[] ip1, int[] ip2){
        for(int i = 0; i < ip1.length; ++i){
            dst_lip[i] = ip1[i];
            dst_uip[i] = ip2[i];
        }
        
        isDstIPSet = true;
    }
    
    public void setDestinationPortRange(int port1, int port2){
        dst_lport = port1;
        dst_uport = port2;
        isDstPortSet = true;
    }

    public int[] getSrc_lip() {
        return src_lip;
    }

    public int[] getSrc_uip() {
        return src_uip;
    }

    public int getSrc_lport() {
        return src_lport;
    }

    public int getSrc_uport() {
        return src_uport;
    }

    public int[] getDst_lip() {
        return dst_lip;
    }

    public int[] getDst_uip() {
        return dst_uip;
    }

    public int getDst_lport() {
        return dst_lport;
    }

    public int getDst_uport() {
        return dst_uport;
    }
    
    public void setIsSrcIPSet(boolean b){
        isSrcIPSet = b;
    }

    public void setIsSrcPortSet(boolean b){
        isSrcPortSet = b;
    }

    public void setIsDstIPSet(boolean b){
        isDstIPSet = b;
    }
    
    public void setIsDstPortSet(boolean b){
        isDstPortSet = b;
    }
    
    public boolean isSrcIPSet() {
        return isSrcIPSet;
    }

    public boolean isSrcPortSet() {
        return isSrcPortSet;
    }

    public boolean isDstIPSet() {
        return isDstIPSet;
    }

    public boolean isDstPortSet() {
        return isDstPortSet;
    }

}