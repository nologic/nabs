/*
 * ShellLineMessage.java
 *
 * Created on November 11, 2006, 4:24 PM
 */

package eunomia.messages.receptor.ncm;

import eunomia.messages.receptor.NoCauseMessage;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 *
 * @author Mikhail Sosonkin
 */
public class ShellLineMessage implements NoCauseMessage {
    private String line;
    
    public ShellLineMessage() {
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(line);
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        line = (String)in.readObject();
    }

    public int getVersion() {
        return 0;
    }

    public void setVersion(int v) {
    }

    public String getLine() {
        return line;
    }

    public void setLine(String line) {
        this.line = line;
    }
    
}