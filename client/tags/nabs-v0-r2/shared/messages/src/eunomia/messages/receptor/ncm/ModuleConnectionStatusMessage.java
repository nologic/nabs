/*
 * ModuleConnectionStatusMessage.java
 *
 * Created on December 15, 2006, 6:46 PM
 *
 */

package eunomia.messages.receptor.ncm;

import eunomia.messages.receptor.ModuleHandle;
import eunomia.messages.receptor.NoCauseMessage;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 *
 * @author Mikhail Sosonkin
 */
public class ModuleConnectionStatusMessage implements NoCauseMessage {
    private String flowServer;
    private ModuleHandle handle;
    private boolean connect;

    public ModuleConnectionStatusMessage() {
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(flowServer);
        out.writeObject(handle);
        out.writeBoolean(connect);
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        flowServer = (String)in.readObject();
        handle = (ModuleHandle)in.readObject();
        connect = in.readBoolean();
    }

    public int getVersion() {
        return 0;
    }

    public void setVersion(int v) {
    }

    public String getFlowServer() {
        return flowServer;
    }

    public void setFlowServer(String flowServer) {
        this.flowServer = flowServer;
    }

    public ModuleHandle getHandle() {
        return handle;
    }

    public void setHandle(ModuleHandle handle) {
        this.handle = handle;
    }

    public boolean isConnect() {
        return connect;
    }

    public void setConnect(boolean connect) {
        this.connect = connect;
    }
}