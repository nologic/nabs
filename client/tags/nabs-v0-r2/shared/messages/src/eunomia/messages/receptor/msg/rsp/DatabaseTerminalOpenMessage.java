/*
 * DatabaseTerminalOpenMessage.java
 *
 * Created on April 10, 2006, 11:50 PM
 *
 */

package eunomia.messages.receptor.msg.rsp;

import eunomia.messages.Message;
import eunomia.messages.receptor.ResponceMessage;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 *
 * @author Mikhail Sosonkin
 */
public class DatabaseTerminalOpenMessage implements ResponceMessage {
    private int port1;
    private int port2;
    private int random1;
    private int random2;
    
    private static final long serialVersionUID = 7192551467567291161L;
    
    public DatabaseTerminalOpenMessage() {
    }

    public int getCommandID() {
        return 0;
    }

    public int getVersion() {
        return 0;
    }

    public void setVersion(int v) {
    }

    public Message getCause() {
        return null;
    }

    public int getPort1() {
        return port1;
    }

    public void setPort1(int port1) {
        this.port1 = port1;
    }

    public int getPort2() {
        return port2;
    }

    public void setPort2(int port2) {
        this.port2 = port2;
    }

    public int getRandom1() {
        return random1;
    }

    public void setRandom1(int random1) {
        this.random1 = random1;
    }

    public int getRandom2() {
        return random2;
    }

    public void setRandom2(int random2) {
        this.random2 = random2;
    }
    
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeInt(port1);
        out.writeInt(port2);
        out.writeInt(random1);
        out.writeInt(random2);
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        port1 = in.readInt();
        port2 = in.readInt();
        random1 = in.readInt();
        random2 = in.readInt();
    }
}