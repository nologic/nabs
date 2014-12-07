/*
 * ModuleListeningListMessage.java
 *
 * Created on September 4, 2006, 8:25 PM
 *
 */

package eunomia.messages.receptor.msg.rsp;

import eunomia.messages.Message;
import eunomia.messages.receptor.ModuleHandle;
import eunomia.messages.receptor.ResponceMessage;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Mikhail Sosonkin
 */
public class ModuleListeningListMessage implements ResponceMessage {
    private ModuleHandle handle;
    private String[] servers;
    
    public ModuleListeningListMessage() {
    }

    public Message getCause() {
        return null;
    }
    
    public void writeExternal(ObjectOutput out) throws IOException {
        int size = servers.length;
        
        out.writeObject(handle);
        
        out.writeInt(size);
        for(int i = 0; i < size; ++i){
            out.writeObject(servers[i]);
        }
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        int size;
        
        handle = (ModuleHandle)in.readObject();
        size = in.readInt();
        servers = new String[size];
        
        for(int i = 0; i < size; ++i){
            servers[i] = in.readObject().toString();
        }
    }

    public int getVersion() {
        return 0;
    }

    public void setVersion(int v) {
    }

    public ModuleHandle getHandle() {
        return handle;
    }

    public void setHandle(ModuleHandle handle) {
        this.handle = handle;
    }

    public String[] getServers() {
        return servers;
    }

    public void setServers(String[] servers) {
        this.servers = servers;
    }
    
}
