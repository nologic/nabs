/*
 * AlertItem.java
 *
 * Created on December 17, 2006, 12:04 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package eunomia.plugin.com.networkPolicy;

import eunomia.messages.Message;
import eunomia.plugin.networkPolicy.utils.FlowId;
import java.io.DataOutputStream;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;

/**
 *
 * @author kulesh
 */
public class AlertItem implements Externalizable{
    public static final int ALERT_MARKER= 0xBEEF;
    
    enum AlertStatus {NEW, OPEN, PENDING, CLOSED, UNKNOWN};
    
    private long alertID;
    private long policyID;
    private long violations;
    private long firstSeen;
    private long lastSeen;
    
    private FlowId flowId=null;
    private AlertStatus status;
    private String notes;
    
    /** Creates a new instance of AlertItem */
    public AlertItem() {
    }

    public long getAlertID() {
        return alertID;
    }

    public void setAlertID(long alertID) {
        this.alertID = alertID;
    }

    public long getPolicyID() {
        return policyID;
    }

    public void setPolicyID(long policyID) {
        this.policyID = policyID;
    }

    public long getViolations() {
        return violations;
    }

    public void incrementViolations(){
        ++violations;
    }
    
    public long getFirstSeen() {
        return firstSeen;
    }

    public void setFirstSeen(long firstSeen) {
        this.firstSeen = firstSeen;
    }

    public long getLastSeen() {
        return lastSeen;
    }

    public void setLastSeen(long lastSeen) {
        this.lastSeen = lastSeen;
    }

    public FlowId getFlowId(){
        return flowId;
    }
    
    public void setFlowId(FlowId flowId){
        this.flowId= flowId;
    }
    
    public AlertStatus getStatus() {
        return status;
    }

    public void setStatus(AlertStatus status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public int getVersion() {
        return 0;
    }

    public void setVersion(int v) {
    }
    
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeLong(alertID);
        out.writeLong(policyID);
        out.writeLong(violations);
        out.writeLong(firstSeen);
        out.writeLong(lastSeen);
        out.writeLong(flowId.getSourceIP());
        out.writeLong(flowId.getDestinationIP());
        
        out.writeInt(flowId.getSourcePort());
        out.writeInt(flowId.getDestinationPort());
        
        out.writeObject(status);
        out.writeObject(notes);    
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        alertID = in.readLong();
        policyID= in.readLong();
        violations= in.readLong();
        firstSeen= in.readLong();
        lastSeen= in.readLong();
        
        flowId= new FlowId();
        
        flowId.setSourceIP(in.readLong());
        flowId.setDestinationIP(in.readLong());
        
        flowId.setSourcePort(in.readInt());
        flowId.setDestinationPort(in.readInt());
        
        status= (AlertStatus)in.readObject();
        notes= (String)in.readObject();
    }
    
    public String toString(){
        return ("[ AlertId: " + alertID + " Violations: " + violations +" ]" + flowId);
    }
}