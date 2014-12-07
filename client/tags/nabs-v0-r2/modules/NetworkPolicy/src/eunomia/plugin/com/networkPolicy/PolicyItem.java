/*
 * PolicyItem.java
 *
 * Created on December 17, 2006, 1:04 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package eunomia.plugin.com.networkPolicy;

import eunomia.messages.FilterEntryMessage;
import eunomia.messages.Message;
import eunomia.receptor.module.NABFlow.NABFilterEntry;
import java.io.DataOutputStream;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.Comparator;

/**
 *
 * @author kulesh
 */
public class PolicyItem implements Externalizable{
    public static final int POLICY_MARKER = 0xFEED;
    
    private long policyID;
    private String description;
    private NABFilterEntry filter;
    private int rate; //traffic rate in Kbps
    private int timeout; //timeout (in seconds) of idle flows

    /** Creates a new instance of PolicyItem */
    public PolicyItem() {
    }

    public long getPolicyID() {
        return policyID;
    }

    public void setPolicyID(long policyID) {
        this.policyID = policyID;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public NABFilterEntry getFilter() {
        return filter;
    }

    public void setFilter(NABFilterEntry filter) {
        this.filter = filter;
    }

    public int getRate() {
        return rate;
    }

    public void setRate(int rate) {
        this.rate = rate;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public int getVersion() {
        return 0;
    }

    public void setVersion(int v) {
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeLong(policyID);
        
        out.writeInt(rate);
        out.writeInt(timeout);
        
        out.writeObject(description);
        out.writeObject(filter.getFilterEntryMessage());
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        policyID= in.readLong();

        rate= in.readInt();
        timeout= in.readInt();
        
        description= (String)in.readObject();
        filter= new NABFilterEntry((FilterEntryMessage)in.readObject());
    }
    
    public String toString(){
        return ("[ PolicyId: " + policyID + "] " + description);
    }
}
