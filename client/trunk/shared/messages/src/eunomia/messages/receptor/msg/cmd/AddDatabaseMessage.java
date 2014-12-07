/*
 * AddDatabaseMessage.java
 *
 * Created on September 6, 2005, 4:06 PM
 *
 */

package eunomia.messages.receptor.msg.cmd;

import eunomia.messages.*;
import eunomia.messages.receptor.*;
import java.io.*;

/**
 *
 * @author Mikhail Sosonkin
 */
public class AddDatabaseMessage extends AbstractCommandMessage {
    private static final long serialVersionUID = 4414409508971308764L;
    
    private DatabaseDescriptor dbDesc;
    
    public AddDatabaseMessage() {
        dbDesc = new DatabaseDescriptor();
    }
    
    public int getCommandID() {
        return CommandMessage.CMD_ADD_DATABASE;
    }
    
    public DatabaseDescriptor getDbDescriptor() {
        return dbDesc;
    }

    public void setDbDescriptor(DatabaseDescriptor dbDesc) {
        if(readOnly){
            throw new UnsupportedOperationException("setDbDescription: AddDatabaseMessage is read only");
        }
        
        this.dbDesc = dbDesc;
    }
    
    public int getVersion() {
        return 0;
    }

    public void setVersion(int v) {
    }
    
    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);
        out.writeObject(dbDesc);
    }
    
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);
        dbDesc = (DatabaseDescriptor)in.readObject();
    }
}