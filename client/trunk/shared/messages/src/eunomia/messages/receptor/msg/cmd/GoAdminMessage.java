/*
 * GoAdminMessage.java
 *
 * Created on October 2, 2006, 5:56 AM
 *
 */

package eunomia.messages.receptor.msg.cmd;

import eunomia.messages.receptor.AbstractCommandMessage;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 *
 * @author Mikhail Sosonkin
 */
public class GoAdminMessage  extends AbstractCommandMessage {
    public GoAdminMessage() {
    }

    public int getCommandID() {
        return 0;
    }

    public int getVersion() {
        return 0;
    }

    public void setVersion(int v) {
    }
    
    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);
    }
}