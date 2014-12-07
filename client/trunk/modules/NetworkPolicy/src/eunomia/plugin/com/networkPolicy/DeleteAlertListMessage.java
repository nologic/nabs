/*
 * DeleteAlertListMessage.java
 *
 * Created on August 5, 2007, 12:43 PM
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
public class DeleteAlertListMessage implements Message {
    private long[] list;
    
    public DeleteAlertListMessage() {
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

    public long[] getList() {
        return list;
    }

    public void setList(long[] list) {
        this.list = list;
    }
    
}
