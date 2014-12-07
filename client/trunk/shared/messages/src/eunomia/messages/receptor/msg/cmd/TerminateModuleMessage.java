/*
 * TerminateModuleMessage.java
 *
 * Created on January 18, 2006, 2:50 PM
 */

package eunomia.messages.receptor.msg.cmd;

import eunomia.messages.Message;
import eunomia.messages.receptor.*;
import java.io.*;

/**
 *
 * @author Mikhail Sosonkin
 */
public class TerminateModuleMessage extends AbstractCommandMessage {
    private static final long serialVersionUID = 7866445092807732031L;
    
    private ModuleHandle moduleHandle;
    
    public TerminateModuleMessage() {
    }
    
    public int getVersion() {
        return 0;
    }

    public void setVersion(int v) {
    }
    
    public int getCommandID(){
        return 0;
    }
    
    public ModuleHandle getModuleHandle() {
        return moduleHandle;
    }

    public void setModuleHandle(ModuleHandle moduleHandle) {
        this.moduleHandle = moduleHandle;
    }
    
    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);
        out.writeObject(moduleHandle);
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);
        moduleHandle = (ModuleHandle)in.readObject();
    }
}