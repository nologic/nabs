/*
 * InitialModuleStatusMessage.java
 *
 * Created on January 18, 2006, 1:52 PM
 *
 */

package eunomia.messages.module.msg;

import java.io.*;

/**
 *
 * @author Mikhail Sosonkin
 */
public class InitialModuleStatusMessage extends GenericModuleMessage {
    private static final long serialVersionUID = 7955699135953400460L;
    
    private long lastReset;
    
    public InitialModuleStatusMessage(){
    }

    public long getLastReset() {
        return lastReset;
    }

    public void setLastReset(long lastReset) {
        this.lastReset = lastReset;
    }
    
    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);
        out.writeLong(lastReset);
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);
        lastReset = in.readLong();
    }
}