/*
 * GetAnalysisParametersMessage.java
 *
 * Created on February 5, 2007, 11:46 PM
 *
 */

package eunomia.messages.module.msg;

import eunomia.messages.ByteArrayMessage;
import eunomia.messages.module.AbstractModuleMessage;
import eunomia.messages.receptor.ModuleHandle;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 *
 * @author Mikhail Sosonkin
 */
public class AnalysisParametersMessage extends GenericModuleMessage {
    private String[] databases;

    public AnalysisParametersMessage() {
    }
    
    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);
        out.writeInt(databases.length);
        for (int i = 0; i < databases.length; i++) {
            out.writeObject(databases[i]);
        }
    }
    
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);
        databases = new String[in.readInt()];
        for (int i = 0; i < databases.length; i++) {
            databases[i] = (String)in.readObject();
        }
    }

    public String[] getDatabases() {
        return databases;
    }

    public void setDatabases(String[] databases) {
        this.databases = databases;
    }

}