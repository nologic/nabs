package eunomia.messages.receptor.protocol.impl;

import eunomia.messages.receptor.protocol.ProtocolDescriptor;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 *
 * @author Mikhail Sosonkin
 */
public class FILEProtocol implements ProtocolDescriptor {
    private String file;
    
    public String protoString() {
        return "File: " + file;
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(file);
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        file = (String)in.readObject();
    }

    public int getVersion() {
        return 0;
    }

    public void setVersion(int v) {
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }
}