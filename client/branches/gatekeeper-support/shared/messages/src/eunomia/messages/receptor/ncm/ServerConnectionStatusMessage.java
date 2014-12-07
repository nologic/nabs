/*
 * ServerConnectionStatusMessage.java
 *
 * Created on July 6, 2006, 10:02 PM
 *
 */

package eunomia.messages.receptor.ncm;

import eunomia.messages.Message;
import eunomia.messages.receptor.NoCauseMessage;
import java.io.*;

/**
 *
 * @author Mikhail Sosonkin
 */
public class ServerConnectionStatusMessage implements NoCauseMessage {
    public static final int CONNECTED = 0, CON_FAILURE = 1, DROPPED = 2, CLOSED = 3;
    public static final String[] desc = {"Connected", "Connection Failure", "Connection Dropped", "Connection Closed"};
    
    private int status;
    private String server;
    
    public ServerConnectionStatusMessage() {
    }

    public int getVersion() {
        return 0;
    }

    public void setVersion(int v) {
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
    
    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }
    
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(server);
        out.writeInt(status);
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        server = in.readObject().toString();
        status = in.readInt();
    }
}