/*
 * PolicyItem.java
 *
 * Created on December 17, 2006, 1:04 AM
 */

package eunomia.plugin.com.networkPolicy;

import com.vivic.eunomia.sys.EunomiaUtils;
import com.vivic.eunomia.sys.frontend.ConsoleModuleManager;
import com.vivic.eunomia.sys.receptor.SieveContext;
import eunomia.flow.Filter;
import eunomia.flow.FilterEntry;
import eunomia.flow.FilterList;
import eunomia.messages.module.msg.ChangeFilterMessage;
import eunomia.receptor.module.NABFlow.NABFilterEntry;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 *
 * @author kulesh, Mikhail Sosonkin
 */
public class PolicyItem implements Externalizable {
    public static int NUM_TYPES = 5;
    public static int REAL_TIME = 0;
    public static int HOURLY = 1;
    public static int DAILY = 2;
    public static int WEEKLY = 3;
    public static int MONTHLY = 4;
    
    public static long[] INTERVALS = new long[]{1000, 1000*60*60, 1000*60*60*24, 1000*60*60*24*7, 1000*60*60*24*7*30};
    
    private static ConsoleModuleManager cmMan;
    
    private int policyID;
    private int policyType;
    private String description;
    private Filter filter;
    private long rate;

    private int timeout; //timeout (in seconds) of idle flows
    
    //Control flags
    private boolean removeAlerts;
    
    private transient int alertCount;
    private transient int newAlerts;
    
    public PolicyItem(int type, String desc) {
        alertCount = newAlerts = 0;
        policyType = type;
        description = desc;
        filter = new Filter();
    }
    
    public PolicyItem() {
        this(0, null);
    }
    
    public static void setModuleManager(ConsoleModuleManager man) {
        cmMan = man;
    }
    
    public long getTimeInterval() {
        return INTERVALS[policyType];
    }
    
    public int getPolicyID() {
        return policyID;
    }

    public void setPolicyID(int policyID) {
        this.policyID = policyID;
    }

    public int getPolicyType() {
        return policyType;
    }

    public void setPolicyType(int policyType) {
        this.policyType = policyType;
    }
    
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Filter getFilter() {
        return filter;
    }
    
    public void setFilter(Filter filter) {
        this.filter = filter;
    }

    public long getRate() {
        return rate;
    }
    
    public void setRate(long rate) {
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
        out.writeInt(policyID);
        out.writeInt(policyType);
        
        out.writeLong(rate);
        out.writeInt(timeout);
        
        out.writeObject(description);
        out.writeObject(filter.getChangeFilterMessage());
        out.writeBoolean(removeAlerts);
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        policyID = in.readInt();
        policyType = in.readInt();

        rate = in.readLong();
        timeout = in.readInt();
        
        description = (String)in.readObject();
        if(cmMan != null) {
            filter = EunomiaUtils.makeFrontendFilter((ChangeFilterMessage)in.readObject(), cmMan);
        } else {
            filter = EunomiaUtils.makeReceptorFilter((ChangeFilterMessage)in.readObject(), SieveContext.getModuleManager());
        }
        removeAlerts = in.readBoolean();
    }
    
    public String toString(){
        return ("[" + policyID + "] " + description);
    }

    public void addAlerts(int alerts) {
        alertCount += alerts;
    }
    
    public void addNewAlerts(int alerts) {
        newAlerts += alerts;
    }
    
    public int getNewAlertCount() {
        return newAlerts;
    }
    
    public void resetAlertCount() {
        alertCount = newAlerts = 0;
    }
    
    public int getAlertCount() {
        return alertCount;
    }

    public boolean isRemoveAlerts() {
        return removeAlerts;
    }

    public void setRemoveAlerts(boolean removeAlerts) {
        this.removeAlerts = removeAlerts;
    }
}