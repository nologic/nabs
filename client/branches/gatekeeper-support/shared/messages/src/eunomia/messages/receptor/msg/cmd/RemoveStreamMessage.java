/*
 * RemoveStreamMessage.java
 *
 * Created on September 6, 2005, 3:14 PM
 *
 */

package eunomia.messages.receptor.msg.cmd;

import eunomia.messages.Message;
import eunomia.messages.receptor.AbstractCommandMessage;
import eunomia.messages.receptor.CommandMessage;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;


/**
 *
 * @author Mikhail Sosonkin
 */
public class RemoveStreamMessage extends AbstractCommandMessage {
    private String name;
    
    private static final long serialVersionUID = 4048485553411164930L;
    
    public RemoveStreamMessage() {
    }

    public int getVersion() {
        return 0;
    }

    public void setVersion(int v) {
    }
    
    public int getCommandID(){
        return 0;
    }
    

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if(readOnly){
            throw new UnsupportedOperationException("setName: RemoveStreamMessage is read only");
        }

        this.name = name;
    }
    
    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);
        out.writeObject(name);
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);
        name = in.readObject().toString();
    }
}