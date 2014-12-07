/*
 * ExecuteCommandMessage.java
 *
 * Created on November 10, 2006, 5:35 PM
 *
 */

package eunomia.messages.receptor.msg.cmd.admin;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 *
 * @author Mikhail Sosonkin
 */
public class ExecuteCommandMessage extends AdminMessage {
    private String command;
    
    public ExecuteCommandMessage() {
    }
    
    public void setCommand(String cmd){
        command = cmd;
    }
    
    public String getCommand(){
        return command;
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(command);
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        command = (String)in.readObject();
    }

    public int getVersion() {
        return 0;
    }

    public void setVersion(int v) {
    }
    
}
