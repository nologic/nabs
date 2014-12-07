/*
 * StatusMessage.java
 *
 * Created on October 19, 2005, 8:12 PM
 *
 */

package eunomia.messages.module.msg;

import eunomia.messages.receptor.ModuleHandle;
import eunomia.util.oo.LargeTransfer;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 *
 * @author Mikhail Sosonkin
 */
public class ModuleStatusMessage extends GenericModuleMessage {
    private LargeTransfer trans;
    
    public ModuleStatusMessage(){
    }
    
    public void setLargeTransfer(LargeTransfer t) {
        trans = t;
    }
    
    public LargeTransfer getLargeTransfer() {
        return trans;
    }
    
    public InputStream getInputStream() {
        if(trans != null) {
            return trans.getInputStream();
        }
        
        return super.getInputStream();
    }
    
    public void cleanup() throws IOException {
        if(trans != null) {
            trans.getInputStream().close();
            trans.getDestinationFile().delete();
        }
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeBoolean(trans == null);
        if(trans == null) {
            super.writeExternal(out);
        } else {
            out.writeObject(this.getModuleHandle());
            out.writeObject(trans);
        }
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        if(in.readBoolean()) {
            super.readExternal(in);
        } else {
            this.setModuleHandle((ModuleHandle)in.readObject());
            trans = (LargeTransfer)in.readObject();
        }
    }
}