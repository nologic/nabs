/*
 * OpenDetailsMessage.java
 *
 * Created on April 22, 2006, 10:54 PM
 *
 */

package eunomia.plugin.msg;

import eunomia.messages.Message;
import eunomia.messages.receptor.ModuleHandle;
import java.io.*;

/**
 *
 * @author Mikhail Sosonkin
 */
public class OpenDetailsMessage implements Message {
    private static final long serialVersionUID = 4882900958371308075L;
    
    private ModuleHandle handle;
    private long ip;

    public OpenDetailsMessage() {
    }
    
    public ModuleHandle getHandle() {
        return handle;
    }

    public void setHandle(ModuleHandle handle) {
        this.handle = handle;
    }

    public long getIp() {
        return ip;
    }

    public void setIp(long ip) {
        this.ip = ip;
    }
    
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeLong(ip);
        out.writeObject(handle);
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        ip = in.readLong();
        handle = (ModuleHandle)in.readObject();
    }

    public int getVersion() {
        return 0;
    }

    public void setVersion(int v) {
    }
}