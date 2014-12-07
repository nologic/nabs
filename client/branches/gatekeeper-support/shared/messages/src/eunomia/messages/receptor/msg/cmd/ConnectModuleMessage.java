/*
 * ConnectModuleMessage.java
 *
 * Created on August 8, 2006, 10:42 PM
 *
 */

package eunomia.messages.receptor.msg.cmd;

import eunomia.messages.Message;
import eunomia.messages.receptor.*;
import java.io.*;

/**
 *
 * @author Mikhail Sosonkin
 */
public class ConnectModuleMessage extends AbstractCommandMessage {
    private String flowServer;
    private ModuleHandle handle;
    private boolean connect;
    
    public ConnectModuleMessage() {
    }

    public int getCommandID() {
        return 0;
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

    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);
        
        out.writeObject(flowServer);
        out.writeObject(handle);
        out.writeBoolean(connect);
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);
        
        flowServer = (String)in.readObject();
        handle = (ModuleHandle)in.readObject();
        connect = in.readBoolean();
    }
}