/*
 * CommandResult.java
 *
 * Created on November 8, 2005, 7:44 PM
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
 * @author MikhailSosonkin
 */
public class CommandResultMessage implements ResponceMessage {
    private int msgHash;
    private String msg;
    private Message result;
    
    private static final long serialVersionUID = 4276120560340898956L;
    
    public CommandResultMessage(){
    }
    
    public CommandResultMessage(Message causedBy, String message, Message result) {
        msgHash = causedBy.hashCode();
        msg = message;
        this.result = result;
    }
    
    public Message getResult(){
        return result;
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
        return new Message(){
            public int hashCode(){
                return msgHash;
            }
            public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
            }
            public void writeExternal(ObjectOutput out) throws IOException {
            }
            
            public int getVersion() {
                return 0;
            }

            public void setVersion(int v) {
            }
            
            public boolean equals(Object o){
                return hashCode() == o.hashCode();
            }
        };
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        msgHash = in.readInt();
        msg = (String)in.readObject();
        result = (Message)in.readObject();
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeInt(msgHash);
        out.writeObject(msg);
        out.writeObject(result);
    }
    
    public String toString(){
        return "Success... MSG: " + msg + " CAUSE: " + msgHash;
    }
}