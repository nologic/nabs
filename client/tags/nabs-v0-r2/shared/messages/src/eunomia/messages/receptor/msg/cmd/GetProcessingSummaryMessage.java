/*
 * GetProcessingSummaryMessage.java
 *
 * Created on November 27, 2006, 5:01 PM
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
public class GetProcessingSummaryMessage extends AbstractCommandMessage {
    private String databaseName;
    
    public GetProcessingSummaryMessage() {
    }

    public int getCommandID() {
        return 0;
    }

    public int getVersion() {
        return 0;
    }

    public void setVersion(int v) {
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }
    
    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);
        
        out.writeObject(databaseName);
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);
        
        databaseName = (String)in.readObject();
    }
}