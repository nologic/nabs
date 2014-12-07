/*
 * DarkSpaceConfigMessage.java
 *
 * Created on April 11, 2007, 7:30 PM
 *
 */

package eunomia.plugin.rec.atas.classifiers.msg;

import eunomia.plugin.com.atas.ClassifierConfigurationMessage;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 *
 * @author Mikhail Sosonkin
 */
public class DarkSpaceConfigMessage implements ClassifierConfigurationMessage {
    private int roleNumber;
    private String roleName;
    private long ipRangeBegin;
    private long ipRangeEnd;
    
    public DarkSpaceConfigMessage() {
    }
    
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeInt(roleNumber);
        out.writeObject(roleName);
        out.writeLong(ipRangeBegin);
        out.writeLong(ipRangeEnd);
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        roleNumber = in.readInt();
        roleName = (String)in.readObject();
        ipRangeBegin = in.readLong();
        ipRangeEnd = in.readLong();
    }

    public int getVersion() {
        return 0;
    }

    public void setVersion(int v) {
    }

    public int getRoleNumber() {
        return roleNumber;
    }

    public void setRoleNumber(int roleNumber) {
        this.roleNumber = roleNumber;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public long getIpRangeBegin() {
        return ipRangeBegin;
    }

    public void setIpRangeBegin(long ipRangeBegin) {
        this.ipRangeBegin = ipRangeBegin;
    }

    public long getIpRangeEnd() {
        return ipRangeEnd;
    }

    public void setIpRangeEnd(long ipRangeEnd) {
        this.ipRangeEnd = ipRangeEnd;
    }

    public String getClassName() {
        return "DarkSpaceRole";
    }
}