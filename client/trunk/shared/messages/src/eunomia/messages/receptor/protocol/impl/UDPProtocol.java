/*
 * UDPProtocol.java
 *
 * Created on August 24, 2006, 11:51 PM
 *
 */

package eunomia.messages.receptor.protocol.impl;

import eunomia.messages.receptor.protocol.ProtocolDescriptor;
import java.io.*;

/**
 *
 * @author Mikhail Sosonkin
 */
public class UDPProtocol implements ProtocolDescriptor {
    private int listenPort;
    
    public UDPProtocol() {
    }

    public String protoString() {
        return "UDP";
    }

    public int getVersion() {
        return 0;
    }

    public void setVersion(int v) {
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeInt(listenPort);
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        listenPort = in.readInt();
    }

    public int getListenPort() {
        return listenPort;
    }

    public void setListenPort(int listenPort) {
        this.listenPort = listenPort;
    }
    
    public String toString(){
        return protoString() + " " + listenPort;
    }
}