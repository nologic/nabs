/*
 * StartDatabaseAnalysisMessage.java
 *
 * Created on November 24, 2006, 2:32 PM
 *
 */

package eunomia.messages.receptor.msg.cmd;

import eunomia.messages.receptor.AbstractCommandMessage;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 *
 * @author Mikhail Sosonkin
 */
public class InstantiateAnalysisModuleMessage extends AbstractCommandMessage {
    private String module;
    
    public InstantiateAnalysisModuleMessage() {
    }

    public int getCommandID() {
        return 0;
    }

    public int getVersion() {
        return 0;
    }

    public void setVersion(int v) {
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }
    
    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);
        
        out.writeObject(module);
    }
    
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);
        
        module = (String)in.readObject();
    }
}