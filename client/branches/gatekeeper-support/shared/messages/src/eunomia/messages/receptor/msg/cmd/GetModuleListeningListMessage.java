/*
 * GetModuleListeningListMessage.java
 *
 * Created on September 4, 2006, 8:44 PM
 *
 */

package eunomia.messages.receptor.msg.cmd;

import eunomia.messages.receptor.AbstractCommandMessage;
import eunomia.messages.receptor.ModuleHandle;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Mikhail Sosonkin
 */
public class GetModuleListeningListMessage extends AbstractCommandMessage {
    private ModuleHandle handle;
    
    public GetModuleListeningListMessage() {
    }

    public int getCommandID() {
        return 0;
    }

    public int getVersion() {
        return 0;
    }

    public void setVersion(int v) {
    }
    
    public ModuleHandle getHandle() {
        return handle;
    }

    public void setHandle(ModuleHandle handle) {
        this.handle = handle;
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);
        
        out.writeObject(handle);
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);
        
        handle = (ModuleHandle)in.readObject();
    }
}