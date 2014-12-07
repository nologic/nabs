/*
 * DatabaseQueryMessage.java
 *
 * Created on April 10, 2006, 10:27 PM
 */

package eunomia.messages.receptor.msg.cmd;

import eunomia.messages.Message;
import eunomia.messages.receptor.*;
import java.io.*;

/**
 *
 * @author Mikhail Sosonkin
 */
public class DatabaseQueryMessage extends AbstractCommandMessage {
    private String dbname;
    private String query;
    
    private static final long serialVersionUID = 4654363871912634644L;
    
    public DatabaseQueryMessage() {
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
        return dbname;
    }

    public void setDbName(String dbname) {
        this.dbname = dbname;
    }
    
    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }
    
    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);
        out.writeObject(dbname);
        out.writeObject(query);
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);
        dbname = in.readObject().toString();
        query = in.readObject().toString();
    }
}