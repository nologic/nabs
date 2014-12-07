/*
 * StreamConnectionMessage.java
 *
 * Created on September 6, 2005, 4:26 PM
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
public class StreamConnectionMessage extends AbstractCommandMessage {
    private boolean connect;
    private String name;
    
    private static final long serialVersionUID = 8677871727469212569L;
    
    public StreamConnectionMessage(){
        this(null, false);
    }
    
    public StreamConnectionMessage(String name, boolean con){
        setName(name);
        setConnect(con);
    }
    
    public int getCommandID() {
        return 0;
    }

    public int getVersion() {
        return 0;
    }

    public void setVersion(int v) {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if(readOnly){
            throw new UnsupportedOperationException("setSerial: StreamConnectionMessage is read only");
        }

        this.name = name;
    }

    public boolean isConnect() {
        return connect;
    }

    public void setConnect(boolean connect) {
        if(readOnly){
            throw new UnsupportedOperationException("setConnect: StreamConnectionMessage is read only");
        }

        this.connect = connect;
    }
    
    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);
        out.writeBoolean(connect);
        out.writeObject(name);
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);
        connect = in.readBoolean();
        name = (String)in.readObject();
    }
}