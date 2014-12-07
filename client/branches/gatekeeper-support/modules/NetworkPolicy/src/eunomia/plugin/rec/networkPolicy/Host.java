/*
 * Host.java
 *
 * Created on March 22, 2007, 8:45 PM
 *
 */

package eunomia.plugin.rec.networkPolicy;

import eunomia.plugin.com.networkPolicy.PolicyItem;
import eunomia.receptor.module.NABFlow.NABFlow;
import eunomia.util.Util;
import java.util.Arrays;

/**
 *
 * @author Mikhail Sosonkin
 */
public class Host {
    private static int REC_SIZE = 10;
    
    private long lastUpdateTime;
    private long ip;
    
    private Record[] records;
    
    private long life_total;
    private long[] life_content;
    
    public Host(long ip, long initT) {
        this.ip = ip;
        
        life_total = 0;
        life_content = new long[NABFlow.NUM_TYPES];
        records = new Record[REC_SIZE];
    }
    
    public String toString() {
        return Util.ipToString(ip);
    }
    
    public void resetPolicyData(int pID, long time){
        Record r = records[pID];
        if(r != null) {
            r.reset(time);
        }
    }
    
    public long getPolicyDataLastReset(int pID) {
        return records[pID].getLastReset();
    }
    
    public void setIp(long ip) {
        this.ip = ip;
    }
    
    public void accountGlobalData(int t, int size, long time) {
        lastUpdateTime = time;
        life_total += size;
        life_content[t] += size;
    }
    
    public void accountPolicyData(int size, long time, int pID, PolicyItem item) {
        Record r = null;
        
        if(pID >= records.length) {
            REC_SIZE += 10;
            records = (Record[])Arrays.copyOf(records, REC_SIZE);
            
            r = records[pID] = new Record(time, item);
        } else {
            r = records[pID];
            if(r == null) {
                r = records[pID] = new Record(time, item);
            } else if(r.getPolicyItem() != item) {
                // This means that a policy was removed and another one added with the same ID.
                r.setPolicyItem(item, time);
            } else if((lastUpdateTime - r.getLastReset()) > r.getPolicyItem().getTimeInterval()) {
                //prume, check if we need to reset record
                r.reset(time);
            }
        }
        
        r.addBytes(size);
    }
    
    public long getPolicyData(int pID) {
        return records[pID].getBytes();
    }
    
    public long getLifeTotal() {
        return life_total;
    }
    
    public long[] getLifeContent() {
        return life_content;
    }
    
    public long getIP() {
        return ip;
    }
    
    public int hashCode() {
        return (int)(ip & 0xFFFFFFFF);
    }
    
    public boolean equals(Object o) {
        return ip == ((Host)o).ip;
    }

    public long getLastUpdateTime() {
        return lastUpdateTime;
    }
    
    public class Record {
        private long lastReset;
        private long bytes;
        private PolicyItem policyItem;

        public Record(long lastReset, PolicyItem policyItem) {
            this.policyItem = policyItem;
            this.lastReset = lastReset;
        }
        
        public long getLastReset() {
            return lastReset;
        }

        public void reset(long lastReset) {
            this.lastReset = lastReset;
            bytes = 0;
        }

        public long getBytes() {
            return bytes;
        }

        public void addBytes(long bytes) {
            this.bytes += bytes;
        }

        public PolicyItem getPolicyItem() {
            return policyItem;
        }

        public void setPolicyItem(PolicyItem policyItem, long lastReset) {
            this.policyItem = policyItem;
            this.lastReset = lastReset;
        }
    }
}