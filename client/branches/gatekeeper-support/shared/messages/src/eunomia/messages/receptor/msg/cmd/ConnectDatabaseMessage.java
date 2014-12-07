/*
 * ConnectDatabaseMessage.java
 *
 * Created on April 8, 2006, 10:02 AM
 */

package eunomia.messages.receptor.msg.cmd;

import eunomia.messages.Message;
import eunomia.messages.receptor.AbstractCommandMessage;
import eunomia.messages.receptor.CommandMessage;
import java.io.*;

/**
 *
 * @author Mikhail Sosonkin
 */
public class ConnectDatabaseMessage extends AbstractCommandMessage {
    private String dbName;
    private boolean connect;
    
    private static final long serialVersionUID = 5558124907583268772L;
    
    public ConnectDatabaseMessage() {
    }
    
    public int getCommandID() {
        return 0;
    }

    public int getVersion() {
        return 0;
    }

    public void setVersion(int v) {
    }
    
    public String getDbName() {
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public boolean isConnect() {
        return connect;
    }

    public void setConnect(boolean connect) {
        this.connect = connect;
    }
    
    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);
        out.writeObject(dbName);
        out.writeBoolean(connect);
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);
        dbName = in.readObject().toString();
        connect = in.readBoolean();
    }
}
