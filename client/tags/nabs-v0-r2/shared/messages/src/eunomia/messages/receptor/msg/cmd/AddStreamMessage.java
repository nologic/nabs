/*
 * AddStreamMessage.java
 *
 * Created on September 6, 2005, 2:55 PM
 *
 */

package eunomia.messages.receptor.msg.cmd;

import eunomia.messages.receptor.*;
import eunomia.messages.receptor.protocol.ProtocolDescriptor;
import java.io.*;


/**
 *
 * @author Mikhail Sosonkin
 */
public class AddStreamMessage extends AbstractCommandMessage {
    public static final String UDP = "UDP", TCP = "TCP";
    private String name;
    private String modName;
    private String protocol;
    private ProtocolDescriptor desc;
    
    private static final long serialVersionUID = 785549678615995488L;
    
    public AddStreamMessage() {
    }
    
    public int getVersion() {
        return 0;
    }

    public void setVersion(int v) {
    }
    
    public int getCommandID(){
        return CommandMessage.CMD_ADD_SERVER;
    }
    
    public String getName(){
        return name;
    }

    public void setName(String name) {
        if(readOnly){
            throw new UnsupportedOperationException("setName: AddStreamMessage is read only");
        }
        
        this.name = name;
    }
    
    public String getModName() {
        return modName;
    }

    public void setModName(String modName) {
        if(readOnly){
            throw new UnsupportedOperationException("setModName: AddStreamMessage is read only");
        }
        
        this.modName = modName;
    }
    
    public ProtocolDescriptor getProtocol() {
        return desc;
    }

    public void setProtocol(ProtocolDescriptor protocol) {
        if(readOnly){
            throw new UnsupportedOperationException("setProtocol: AddStreamMessage is read only");
        }
        
        this.desc = protocol;
    }
    
    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);
        out.writeObject(name);
        out.writeObject(modName);
        out.writeObject(protocol);
        out.writeObject(desc);
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);
        name = (String)in.readObject();
        modName = (String)in.readObject();
        protocol = (String)in.readObject();
        desc = (ProtocolDescriptor)in.readObject();
    }
}