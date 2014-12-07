/*
 * StartDatabaseAnalysisMessage.java
 *
 * Created on November 24, 2006, 2:32 PM
 *
 */

package eunomia.messages.receptor.msg.cmd;

import eunomia.messages.ByteArrayMessage;
import eunomia.messages.receptor.AbstractCommandMessage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 *
 * @author Mikhail Sosonkin
 */
public class StartDatabaseAnalysisMessage extends AbstractCommandMessage {
    private String db;
    private String module;
    private ByteArrayMessage params;
    
    public StartDatabaseAnalysisMessage() {
        params = new ByteArrayMessage();
    }

    public int getCommandID() {
        return 0;
    }

    public int getVersion() {
        return 0;
    }

    public void setVersion(int v) {
    }

    public String getDb() {
        return db;
    }

    public void setDb(String db) {
        this.db = db;
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }
    
    public DataOutputStream getParamOutputStream() {
        return new DataOutputStream(params.getOutputStream());
    }
    
    public DataInputStream getParamInputStream() {
        return new DataInputStream(params.getInputStream());
    }
    
    public ByteArrayMessage getParams() {
        return params;
    }

    public void setParams(ByteArrayMessage params) {
        this.params = params;
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);
        
        out.writeObject(db);
        out.writeObject(module);
        out.writeObject(params);
    }
    
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);
        
        db = (String)in.readObject();
        module = (String)in.readObject();
        params = (ByteArrayMessage)in.readObject();
    }
}