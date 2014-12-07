/*
 * ErrorMessage.java
 *
 * Created on April 15, 2006, 8:14 PM
 *
 */

package eunomia.messages.receptor.ncm;

import eunomia.messages.Message;
import eunomia.messages.receptor.NoCauseMessage;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 *
 * @author Mikhail Sosonkin
 */
public class LogMessage implements NoCauseMessage {
    private String message;
    private int level;
    
    public LogMessage() {
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String msg) {
        this.message = msg;
    }
    
    public void setLevel(int level) {
    	this.level = level;
    }
    
    public int getLevel() {
    	return level;
    }
    
    public int getVersion() {
        return 0;
    }

    public void setVersion(int v) {
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
    	level = in.readInt();
        message = in.readObject().toString();
    }

    public void writeExternal(ObjectOutput out) throws IOException {
    	out.writeInt(level);
        out.writeObject(message);
    }
}