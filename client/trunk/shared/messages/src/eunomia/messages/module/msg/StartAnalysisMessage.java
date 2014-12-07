/*
 * SetAnalysisParametersMessage.java
 *
 * Created on January 15, 2007, 6:51 PM
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
public class StartAnalysisMessage extends AbstractModuleMessage {
    private ByteArrayMessage params;
    private String[] databases;

    public StartAnalysisMessage() {
    }
    
    public int getVersion(){
        return 0;
    }
    
    public void setVersion(int v){
    }
    
    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);
        out.writeObject(params);
        out.writeInt(databases.length);
        for (int i = 0; i < databases.length; i++) {
            out.writeObject(databases[i]);
        }
    }
    
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);
        params = (ByteArrayMessage)in.readObject();
        databases = new String[in.readInt()];
        for (int i = 0; i < databases.length; i++) {
            databases[i] = (String)in.readObject();
        }
    }

    public ByteArrayMessage getParams() {
        return params;
    }

    public void setParams(ByteArrayMessage params) {
        this.params = params;
    }

    public String[] getDatabases() {
        return databases;
    }

    public void setDatabases(String[] databases) {
        this.databases = databases;
    }
    
}
