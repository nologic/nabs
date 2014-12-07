/*
 * AddRemoveHostMessage.java
 *
 * Created on February 27, 2006, 9:26 PM
 *
 */

package eunomia.plugin.msg.hostDetails;

import eunomia.messages.Message;
import java.io.*;

/**
 *
 * @author Mikhail Sosonkin
 */
public class AddRemoveHostMessage implements Message {
    private static final long serialVersionUID = 8806415966856167255L;

    private long ip;
    private boolean doAdd;
    
    public AddRemoveHostMessage() {
    }
    
    public long getIp() {
        return ip;
    }

    public void setIp(long ip) {
        this.ip = ip;
    }

    public boolean isDoAdd() {
        return doAdd;
    }

    public void setDoAdd(boolean doAdd) {
        this.doAdd = doAdd;
    }
    
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        ip = in.readLong();
        doAdd = in.readBoolean();
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeLong(ip);
        out.writeBoolean(doAdd);
    }

    public int getVersion() {
        return 0;
    }

    public void setVersion(int v) {
    }
}