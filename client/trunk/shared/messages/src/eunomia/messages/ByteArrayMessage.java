/*
 * ByteArrayMesage.java
 *
 * Created on December 3, 2006, 12:50 AM
 *
 */

package eunomia.messages;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.OutputStream;

/**
 *
 * @author Mikhail Sosonkin
 */
public class ByteArrayMessage implements Message {
    private transient ByteArrayOutputStream bos;
    private transient ByteArrayInputStream bin;
    private byte[] bytes;

    private static final long serialVersionUID = 1796546999546789177L;
    
    public ByteArrayMessage(){
    }
    
    public void putBytes(byte[] bytes) throws IOException {
        getOutputStream().write(bytes);
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
        if(bos != null){
            out.writeBoolean(true);
            out.writeInt(bos.size());
            bos.writeTo((OutputStream)out);
        } else {
            out.writeBoolean(false);
        }
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        boolean bBos = in.readBoolean();
        if(bBos){
            int size = in.readInt();
            bytes = new byte[size];
            in.readFully(bytes);
            bin = new ByteArrayInputStream(bytes);
        }
        bos = new ByteArrayOutputStream();
    }

    public int getVersion() {
        return 0;
    }

    public void setVersion(int v) {
    }
}