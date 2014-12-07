/*
 * TCPProtocol.java
 *
 * Created on August 24, 2006, 11:57 PM
 *
 */

package eunomia.messages.receptor.protocol.impl;

import eunomia.messages.receptor.protocol.ProtocolDescriptor;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 *
 * @author Mikhail Sosonkin
 */
public class TCPProtocol implements ProtocolDescriptor {
    private String ip;
    private int port;
    
    public TCPProtocol() {
    }

    public String protoString() {
        return "TCP";
    }

    public int getVersion() {
        return 0;
    }

    public void setVersion(int v) {
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(ip);
        out.writeInt(port);
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        ip = in.readObject().toString();
        port = in.readInt();
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
    
    public String toString(){
        return protoString() + " " + ip + ":" + port;
    }
}