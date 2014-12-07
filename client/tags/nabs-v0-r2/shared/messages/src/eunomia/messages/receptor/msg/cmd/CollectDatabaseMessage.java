/*
 * CollectDatabaseMessage.java
 *
 * Created on September 6, 2005, 4:31 PM
 *
 */

package eunomia.messages.receptor.msg.cmd;

import eunomia.messages.Message;
import eunomia.messages.receptor.AbstractCommandMessage;
import eunomia.messages.receptor.CommandMessage;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;


/**
 *
 * @author Mikhail Sosonkin
 */
public class CollectDatabaseMessage extends AbstractCommandMessage {
    private String dbName;
    private boolean collect;
    
    private static final long serialVersionUID = 4242652705302810123L;
    
    public CollectDatabaseMessage() {
    }
    
    public int getCommandID() {
        return 0;
    }

    public int getVersion() {
        return 0;
    }

    public void setVersion(int v) {
    }

    public boolean isCollect() {
        return collect;
    }

    public String getDbName() {
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public void setCollect(boolean collect) {
        this.collect = collect;
    }
    
    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);
        out.writeObject(dbName);
        out.writeBoolean(collect);
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);
        dbName = in.readObject().toString();
        collect = in.readBoolean();
    }
}