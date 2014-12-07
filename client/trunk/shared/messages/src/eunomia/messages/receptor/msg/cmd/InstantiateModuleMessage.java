/*
 * InstantiateModuleMessage.java
 *
 * Created on January 2, 2006, 12:29 AM
 *
 */

package eunomia.messages.receptor.msg.cmd;

import eunomia.messages.Message;
import eunomia.messages.receptor.*;
import java.io.*;

/**
 *
 * @author Mikhail Sosonkin
 */
public class InstantiateModuleMessage extends AbstractCommandMessage {
    private static final long serialVersionUID = 3053767668897884438L;

    private String modName;
    
    public InstantiateModuleMessage() {
    }
    
    public int getVersion() {
        return 0;
    }

    public void setVersion(int v) {
    }
    
    public int getCommandID(){
        return 0;
    }
    
    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);
        out.writeObject(modName);
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);
        modName = in.readObject().toString();
    }

    public String getModName() {
        return modName;
    }

    public void setModName(String modName) {
        this.modName = modName;
    }
}