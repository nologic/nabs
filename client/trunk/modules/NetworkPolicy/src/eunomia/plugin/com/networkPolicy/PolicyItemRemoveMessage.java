/*
 * PolicyItemRemoveMessage.java
 *
 * Created on March 23, 2007, 11:23 AM
 *
 */

package eunomia.plugin.com.networkPolicy;

import eunomia.messages.Message;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 *
 * @author Mikhail Sosonkin
 */
public class PolicyItemRemoveMessage implements Message {
    private long id;
    
    public PolicyItemRemoveMessage() {
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeLong(id);
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        id = in.readLong();
    }

    public int getVersion() {
        return 0;
    }

    public void setVersion(int v) {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
    
}
