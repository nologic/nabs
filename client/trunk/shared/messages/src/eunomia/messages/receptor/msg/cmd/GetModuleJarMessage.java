/*
 * GetModuleJarMessage.java
 *
 * Created on March 6, 2007, 9:15 PM
 *
 */

package eunomia.messages.receptor.msg.cmd;

import eunomia.messages.DatabaseDescriptor;
import eunomia.messages.receptor.AbstractCommandMessage;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 *
 * @author Mikhail Sosonkin
 */
public class GetModuleJarMessage extends AbstractCommandMessage {
    private String module;
    private int type;
    
    public GetModuleJarMessage() {
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
        out.writeInt(type);
    }
    
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);
        module = (String)in.readObject();
        type = in.readInt();
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
