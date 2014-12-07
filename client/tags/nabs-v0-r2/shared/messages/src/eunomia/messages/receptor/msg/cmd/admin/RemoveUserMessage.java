/*
 * RemoveUser.java
 *
 * Created on September 28, 2006, 11:22 PM
 *
 */

package eunomia.messages.receptor.msg.cmd.admin;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 *
 * @author Mikhail Sosonkin
 */
public class RemoveUserMessage extends AdminMessage {
    private String username;
    
    public RemoveUserMessage() {
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(username);
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        username = (String)in.readObject();
    }

    public int getVersion() {
        return 0;
    }

    public void setVersion(int v) {
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}