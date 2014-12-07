/*
 * UnknownMessage.java
 *
 * Created on September 8, 2005, 4:11 PM
 *
 */

package eunomia.messages.receptor.msg.rsp;

import eunomia.messages.Message;
import eunomia.messages.receptor.ResponceMessage;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 *
 * @author Mikhail Sosonkin
 */
public class UnknownMessage implements ResponceMessage {
    private String message;
    
    private static final long serialVersionUID = 2485129061552990009L;
    
    public UnknownMessage(String msg) {
        message = msg;
    }

    public int getVersion() {
        return 0;
    }

    public void setVersion(int v) {
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        message = (String)in.readObject();
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(message);
    }

    public Message getCause() {
        // how do I know what caused the Unknown message
        return null;
    }
}