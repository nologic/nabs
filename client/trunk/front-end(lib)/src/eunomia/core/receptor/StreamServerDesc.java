/*
 * StreamServerDesc.java
 *
 * Created on December 12, 2005, 9:41 PM
 *
 */

package eunomia.core.receptor;

import eunomia.messages.receptor.msg.rsp.StatusMessage;
import eunomia.messages.receptor.protocol.ProtocolDescriptor;

/**
 *
 * @author Mikhail Sosonkin
 */
public class StreamServerDesc {
    private String name;
    private String modName;
    private ProtocolDescriptor protocol;
    private boolean connected;
    
    public StreamServerDesc() {
        connected = false;
    }
    
    public StreamServerDesc(StatusMessage.StreamServer ss) {
        name = ss.getName();
        connected = ss.isConnected();
        modName = ss.getModUsed();
        setProtocol(ss.getProtocol());
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isConnected() {
        return connected;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    public String getModName() {
        return modName;
    }

    public void setModName(String modName) {
        this.modName = modName;
    }

    public ProtocolDescriptor getProtocol() {
        return protocol;
    }

    public void setProtocol(ProtocolDescriptor protocol) {
        this.protocol = protocol;
    }
    
    public String toString() {
        return name + "(" + protocol.protoString() + ")";
    }
    
}