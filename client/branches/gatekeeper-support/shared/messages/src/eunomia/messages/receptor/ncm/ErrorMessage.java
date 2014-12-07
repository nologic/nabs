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
public class ErrorMessage implements NoCauseMessage {
    private static final long serialVersionUID = 3118758630164310056L;
    
    private String error;
    
    public ErrorMessage() {
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
    
    public int getVersion() {
        return 0;
    }

    public void setVersion(int v) {
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        error = in.readObject().toString();
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(error);
    }
}