/*
 * HostListMessage.java
 *
 * Created on April 22, 2006, 2:47 PM
 *
 */

package eunomia.plugin.msg.hostDetails;

import eunomia.messages.Message;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 *
 * @author Mikhail Sosonkin
 */
public class HostListMessage implements Message {
    private static final long serialVersionUID = 1762359961406924065L;
    
    private long[] list;
    
    public HostListMessage() {
    }

    public long[] getList() {
        return list;
    }

    public void setList(long[] list) {
        this.list = list;
    }
    
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeInt(list.length);
        for (int i = 0; i < list.length; i++) {
            out.writeLong(list[i]);
        }
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        list = new long[in.readInt()];
        for (int i = 0; i < list.length; i++) {
            list[i] = in.readLong();
        }
    }

    public int getVersion() {
        return 0;
    }

    public void setVersion(int v) {
    }
}
