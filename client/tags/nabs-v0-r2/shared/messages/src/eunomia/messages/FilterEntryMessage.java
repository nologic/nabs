/*
 * FilterMessage.java
 *
 * Created on December 29, 2005, 8:57 PM
 *
 */

package eunomia.messages;

import java.io.*;

/**
 *
 * @author Mikhail Sosonkin
 */
public class FilterEntryMessage implements Externalizable {
    private int filterID;
    
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
    
    private Message specific;
    private String flowModule;
    
    private static final long serialVersionUID = 5729261614510849941L;
    
    public FilterEntryMessage() {
        isSrcIPSet = false;
        isSrcPortSet = false;
        isDstIPSet = false;
        isDstPortSet = false;
        
        src_lip = new int[4];
        src_uip = new int[4];
        dst_lip = new int[4];
        dst_uip = new int[4];
    }
    
    public void setIPs(int[] slip, int[] suip, int[] dlip, int[] duid){
        for(int i = 3; i != -1; --i){
            src_lip[i] = slip[i];
            src_uip[i] = suip[i];
            dst_lip[i] = dlip[i];
            dst_uip[i] = duid[i];
        }
    }
    
    public void setPorts(int slp, int sup, int dlp, int dup){
        src_lport = slp;
        src_uport = sup;
        dst_lport = dlp;
        dst_uport = dup;
    }
    
    public void setBools(boolean si, boolean sp, boolean di, boolean dp){
        isSrcIPSet = si;
        isSrcPortSet = sp;
        isDstIPSet = di;
        isDstPortSet = dp;
    }
    
    public void getIPs(int[] slip, int[] suip, int[] dlip, int[] duid){
        for(int i = 3; i != -1; --i){
            slip[i] = src_lip[i];
            suip[i] = src_uip[i];
            dlip[i] = dst_lip[i];
            duid[i] = dst_uip[i];
        }
    }
    
    public int getSrc_lport() {
        return src_lport;
    }

    public int getSrc_uport() {
        return src_uport;
    }

    public int getDst_lport() {
        return dst_lport;
    }

    public int getDst_uport() {
        return dst_uport;
    }

    public boolean isIsSrcIPSet() {
        return isSrcIPSet;
    }

    public boolean isIsSrcPortSet() {
        return isSrcPortSet;
    }

    public boolean isIsDstIPSet() {
        return isDstIPSet;
    }

    public boolean isIsDstPortSet() {
        return isDstPortSet;
    }
    
    public int getFilterID() {
        return filterID;
    }

    public void setFilterID(int filterID) {
        this.filterID = filterID;
    }

    public String getFlowModule() {
        return flowModule;
    }

    public void setFlowModule(String flowModule) {
        this.flowModule = flowModule;
    }
    
    public Message getSpecific() {
        return specific;
    }

    public void setSpecific(Message specific) {
        this.specific = specific;
    }
    
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeInt(filterID);
        
        for(int i = 3; i != -1; --i){
            out.write((byte)(src_lip[i] & 0xFF));
            out.write((byte)(src_uip[i] & 0xFF));
            out.write((byte)(dst_lip[i] & 0xFF));
            out.write((byte)(dst_uip[i] & 0xFF));
        }

        out.writeInt(src_lport);
        out.writeInt(src_uport);
        out.writeInt(dst_lport);
        out.writeInt(dst_uport);
        
        out.writeBoolean(isSrcIPSet);
        out.writeBoolean(isSrcPortSet);
        out.writeBoolean(isDstIPSet);
        out.writeBoolean(isDstPortSet);
        
        out.writeObject(flowModule);
        out.writeBoolean(specific != null);
        if(specific != null){
            out.writeObject(specific);
        }
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        filterID = in.readInt();
        
        for(int i = 3; i != -1; --i){
            src_lip[i] = in.readByte() & 0xFF;
            src_uip[i] = in.readByte() & 0xFF;
            dst_lip[i] = in.readByte() & 0xFF;
            dst_uip[i] = in.readByte() & 0xFF;
        }
        
        src_lport = in.readInt();
        src_uport = in.readInt();
        dst_lport = in.readInt();
        dst_uport = in.readInt();
        
        isSrcIPSet = in.readBoolean();
        isSrcPortSet = in.readBoolean();
        isDstIPSet = in.readBoolean();
        isDstPortSet = in.readBoolean();
        
        flowModule = in.readObject().toString();
        if(in.readBoolean()){
            specific = (Message)in.readObject();
        }
    }
}