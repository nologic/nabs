/*
 * NABFlowSpecificMessage.java
 *
 * Created on July 12, 2006, 10:33 PM
 *
 */

package eunomia.receptor.module.NABFlow.messages;

import eunomia.messages.Message;
import eunomia.receptor.module.NABFlow.NABFlow;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * Flow specific information is required to be kept in a Message type. This class
 * maintain a list of allowed flows.
 * @author Mikhail Sosonkin
 */
public class NABFlowSpecificMessage implements Message {
    /**
     * An array of allowed flows. Mirror for the same array in the filter entry.
     */
    private boolean[] allow;
    
    public NABFlowSpecificMessage() {
        allow = new boolean[NABFlow.NUM_TYPES];
    }
    
    public boolean[] getAllow() {
        return allow;
    }

    public void setAllow(boolean[] a) {
        for (int i = 0; i < allow.length; i++) {
            allow[i] = a[i];
        }
    }
    
    public void writeExternal(ObjectOutput out) throws IOException {
        for (int i = 0; i < allow.length; i++) {
            out.writeBoolean(allow[i]);
        }
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        for (int i = 0; i < allow.length; i++) {
            allow[i] = in.readBoolean();
        }
    }

    public int getVersion() {
        return 0;
    }

    public void setVersion(int v) {
    }
}