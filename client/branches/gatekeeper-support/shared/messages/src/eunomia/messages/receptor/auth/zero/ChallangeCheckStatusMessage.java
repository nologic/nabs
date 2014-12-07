/*
 * ChallangeCheckStatusMessage.java
 *
 * Created on October 11, 2006, 11:24 PM
 *
 */

package eunomia.messages.receptor.auth.zero;

import eunomia.messages.receptor.auth.AuthenticationMessage;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 *
 * @author Mikhail Sosonkin
 */
public class ChallangeCheckStatusMessage implements AuthenticationMessage {
    private boolean ok;
    
    public ChallangeCheckStatusMessage() {
        ok = false;
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeBoolean(ok);
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        ok = in.readBoolean();
    }

    public int getVersion() {
        return 0;
    }

    public void setVersion(int v) {
    }

    public boolean isOk() {
        return ok;
    }

    public void setOk(boolean ok) {
        this.ok = ok;
    }
    
}
