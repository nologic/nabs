/*
 * GetModuleHandlesMessage.java
 *
 * Created on October 23, 2005, 8:31 PM
 *
 */

package eunomia.messages.receptor.msg.cmd;

import eunomia.messages.*;
import eunomia.messages.receptor.*;
import java.io.*;

/**
 *
 * @author Mikhail Sosonkin
 */
public class GetModuleHandlesMessage extends AbstractCommandMessage {
    private static final long serialVersionUID = 226053742542383681L;

    public GetModuleHandlesMessage() {
    }
    
    public int getVersion() {
        return 0;
    }

    public void setVersion(int v) {
    }
    
    public int getCommandID(){
        return CommandMessage.CMD_ADD_SERVER;
    }
    
    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);
    }
}