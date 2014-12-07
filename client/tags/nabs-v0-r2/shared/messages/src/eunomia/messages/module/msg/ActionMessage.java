/*
 * ActionMessage.java
 *
 * Created on December 24, 2005, 8:37 PM
 *
 */

package eunomia.messages.module.msg;

import eunomia.messages.module.AbstractModuleMessage;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 *
 * @author Mikhail Sosonkin
 */
public class ActionMessage extends AbstractModuleMessage {
    public static final int VOID = -1, START = 0, STOP = 1, RESET = 2;
    private static final long serialVersionUID = 1046598097985165788L;
    
    private int action;
    
    public ActionMessage() {
        setAction(VOID);
    }

    public int getAction() {
        return action;
    }

    public void setAction(int action) {
        this.action = action;
    }
    
    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);
        out.writeInt(action);
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);
        action = in.readInt();
    }
}