/*
 * GenericModuleMessage.java
 *
 * Created on December 29, 2005, 8:44 PM
 */

package eunomia.messages.module.msg;

import eunomia.messages.module.AbstractModuleMessage;

import java.io.*;

/**
 *
 * @author Mikhail Sosonkin
 */
public class GenericModuleMessage extends AbstractModuleMessage {
    // should change this message to the the new ByteArrayMessage instead.
    private transient ByteArrayOutputStream bos;
    private transient ByteArrayInputStream bin;
    private byte[] bytes;

    private static final long serialVersionUID = 1796546999546789177L;
    
    public GenericModuleMessage(){
    }
    
    public void putBytes(byte[] bytes) throws IOException {
        bos.write(bytes);
    }

    public byte[] getBytes(){
        return bytes;
    }
    
    public OutputStream getOutputStream(){
        if(bos == null){
            bos = new ByteArrayOutputStream();
        }
        return bos;
    }
    
    public InputStream getInputStream(){
        return bin;
    }
    
    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);
        if(bos != null){
            out.writeBoolean(true);
            out.writeInt(bos.size());
            bos.writeTo((OutputStream)out);
        } else {
            out.writeBoolean(false);
        }
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);
        boolean bBos = in.readBoolean();
        if(bBos){
            int size = in.readInt();
            bytes = new byte[size];
            in.readFully(bytes);
            bin = new ByteArrayInputStream(bytes);
        }
        bos = new ByteArrayOutputStream();
    }
}