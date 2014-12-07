/*
 * RemoveDatabaseMessage.java
 *
 * Created on September 6, 2005, 4:11 PM
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
public class RemoveDatabaseMessage extends AbstractCommandMessage {
    private String name;

    private static final long serialVersionUID = -189934813050194000L;
    
    public RemoveDatabaseMessage() {
    }
    
    public int getCommandID() {
        return 0;
    }

    public int getVersion() {
        return 0;
    }

    public void setVersion(int v) {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if(readOnly){
            throw new UnsupportedOperationException("setName: RemoveDatabaseMessage is read only");
        }

        this.name = name;
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);
        out.writeObject(name);
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);
        name = (String)in.readObject();
    }
}