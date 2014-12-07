/*
 * SetDefaultListenMessage.java
 *
 * Created on June 6, 2007, 8:32 PM
 *
 */

package eunomia.messages.receptor.msg.cmd;

import eunomia.messages.receptor.AbstractCommandMessage;
import eunomia.messages.receptor.ModuleHandle;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 *
 * @author Mikhail Sosonkin
 */
public class ConnectModuleDefaultMessage extends AbstractCommandMessage {
    private boolean doAdd;
    private ModuleHandle moduleHandle;
    
    public ConnectModuleDefaultMessage() {
        setDoAdd(false);
    }
    
    public int getVersion(){
        return 0;
    }
    
    public void setVersion(int v){
    }
    
    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);
        
        out.writeObject(moduleHandle);
        out.writeBoolean(doAdd);
    }
    
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);
        
        moduleHandle = (ModuleHandle)in.readObject();
        doAdd = in.readBoolean();
    }

    public boolean isDoAdd() {
        return doAdd;
    }

    public void setDoAdd(boolean doAdd) {
        this.doAdd = doAdd;
    }

    public int getCommandID() {
        return 0;
    }

    public ModuleHandle getModuleHandle() {
        return moduleHandle;
    }

    public void setModuleHandle(ModuleHandle moduleHandle) {
        this.moduleHandle = moduleHandle;
    }
}