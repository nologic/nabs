/*
 * SuccessMessage.java
 *
 * Created on September 8, 2005, 11:09 AM
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
public class SuccessMessage implements ResponceMessage {
    private Message cause;
    private String msg;
    
    private static final long serialVersionUID = 1543213874300784892L;
    
    public SuccessMessage(){
        this(null, null);
    }
    
    public SuccessMessage(Message causedBy, String message) {
        cause = causedBy;
        msg = message;
    }

    public int getVersion() {
        return 0;
    }

    public void setVersion(int v) {
    }
    
    public String getMessage(){
        return msg;
    }
    
    public Message getCause(){
        return cause;
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        cause = (Message)in.readObject();
        msg = (String)in.readObject();
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(cause);
        out.writeObject(msg);
    }
    
    public String toString(){
        return "Success... MSG: " + msg + " CAUSE: " + cause;
    }
}