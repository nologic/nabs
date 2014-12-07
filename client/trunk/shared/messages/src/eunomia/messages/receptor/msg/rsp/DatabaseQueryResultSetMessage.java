/*
 * DatabaseQueryResultSetMessage.java
 *
 * Created on February 22, 2007, 10:41 PM
 *
 */

package eunomia.messages.receptor.msg.rsp;

import eunomia.messages.Message;
import eunomia.messages.receptor.ResponceMessage;
import eunomia.messages.receptor.msg.cmd.DatabaseQueryMessage;
import eunomia.util.oo.LargeTransfer;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 *
 * @author Mikhail Sosonkin
 */
public class DatabaseQueryResultSetMessage implements Message {
    private LargeTransfer result;
    private LargeTransfer index;
    private String db;
    
    public DatabaseQueryResultSetMessage() {
        result = new LargeTransfer();
        index = new LargeTransfer();
    }

    public void setIndex(InputStream in) {
        index.setInputStream(in);
    }
    
    public void setResult(InputStream in) {
        result.setInputStream(in);
    }
    
    public LargeTransfer getIndex() {
        return index;
    }
    
    public LargeTransfer getResult() {
        return result;
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(db);
        out.writeObject(result);
        out.writeObject(index);
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        db = (String)in.readObject();
        result = (LargeTransfer)in.readObject();
        index = (LargeTransfer)in.readObject();
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
    
}
