/*
 * AlertItem.java
 *
 * Created on December 17, 2006, 12:04 PM
 *
 */

package eunomia.plugin.com.networkPolicy;

import eunomia.plugin.utils.networkPolicy.FlowId;
import eunomia.util.ResolveRequest;
import com.vivic.eunomia.sys.util.Util;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.net.InetAddress;

/**
 *
 * @author kulesh
 */
public class AlertItem implements Externalizable, ResolveRequest {
    public static final int NEW = 0;
    public static final int OPEN = 1;
    public static final int PENDING = 2;
    public static final int CLOSED = 3;
    public static final int UNKNOWN = 4;
    
    private long alertID;
    private int policyID;
    private long violations;
    private long firstSeen;
    private long lastSeen;
    
    private FlowId flowId;
    private int status;
    private String notes;
    
    byte changeCount;
    
    private transient PolicyItem policyItem;
    private transient String ipString;
    private transient String firstSeenString;
    private transient String lastSeenString;
    private transient String hostName;
    
    /** Creates a new instance of AlertItem */
    public AlertItem() {
        changeCount = 0;
    }
    
    public void updateFrom(AlertItem item) {
        changeCount = item.changeCount;
        violations = item.violations;
        lastSeen = item.lastSeen;
        lastSeenString = null;
        flowId = item.flowId;
        status = item.status;
        notes = item.notes;
    }
    
    public String getIpString() {
        if(ipString == null) {
            ipString = Util.ipToString(flowId.getSourceIP());
        }
        
        return ipString;
    }
    
    public String getFirstSeenString() {
        if(firstSeenString == null) {
            firstSeenString = Util.getTimeStamp(firstSeen, true, true);
        }
        
        return firstSeenString;
    }
    
    public String getLastSeenString() {
        if(lastSeenString == null) {
            lastSeenString = Util.getTimeStamp(lastSeen, true, true);
        }
        
        return lastSeenString;
    }

    public long getAlertID() {
        return alertID;
    }
    
    public void setAlertID(long alertID) {
        this.alertID = alertID;
    }
    
    public int getPolicyID() {
        return policyID;
    }
    
    public void setPolicyID(int policyID) {
        this.policyID = policyID;
    }
    
    public long getViolations() {
        return violations;
    }
    
    public void incrementViolations(){
        ++violations;
        ++changeCount;
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
        ++changeCount;
    }
    
    public FlowId getFlowId(){
        return flowId;
    }
    
    public void setFlowId(FlowId flowId){
        this.flowId = flowId;
        ++changeCount;
    }
    
    public int getStatus() {
        return status;
    }
    
    public void setStatus(int status) {
        this.status = status;
        ++changeCount;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
        ++changeCount;
    }
    
    public int getVersion() {
        return 0;
    }
    
    public void setVersion(int v) {
    }
    
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeByte(changeCount);
        out.writeLong(alertID);
        out.writeInt(policyID);
        out.writeLong(violations);
        out.writeLong(firstSeen);
        out.writeLong(lastSeen);
        
        out.writeLong(flowId.getSourceIP());
        out.writeLong(flowId.getDestinationIP());
        out.writeInt(flowId.getSourcePort());
        out.writeInt(flowId.getDestinationPort());
        long[] types = flowId.getByteTypes();
        out.writeInt(types.length);
        for (int i = 0; i < types.length; i++) {
            out.writeLong(types[i]);
        }
        out.writeLong(flowId.getBytes());
        
        out.writeInt(status);
        out.writeObject(notes);
    }
    
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        changeCount = in.readByte();
        alertID = in.readLong();
        policyID = in.readInt();
        violations = in.readLong();
        firstSeen = in.readLong();
        lastSeen = in.readLong();
        
        flowId = new FlowId();
        
        flowId.setSourceIP(in.readLong());
        flowId.setDestinationIP(in.readLong());
        flowId.setSourcePort(in.readInt());
        flowId.setDestinationPort(in.readInt());
        long[] types = new long[in.readInt()];
        for (int i = 0; i < types.length; i++) {
            types[i] = in.readLong();
        }
        flowId.setBytes(in.readLong(), types);
        
        status = in.readInt();

        notes = (String)in.readObject();
    }
    
    public String toString(){
        return ("[ AlertId: " + alertID + " Violations: " + violations +" ]" + flowId);
    }

    public PolicyItem getPolicyItem() {
        return policyItem;
    }

    public void setPolicyItem(PolicyItem policyItem) {
        this.policyItem = policyItem;
    }

    public InetAddress getAddress() {
        return Util.getInetAddress(flowId.getSourceIP());
    }

    public void setResolved(String hName) {
        hostName = hName;
    }
    
    public String getHostName() {
        return hostName;
    }

    public byte getChangeCount() {
        return changeCount;
    }

    public void incChangeCount() {
        ++changeCount;
    }
}