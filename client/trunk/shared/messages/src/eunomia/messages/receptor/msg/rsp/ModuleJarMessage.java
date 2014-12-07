/*
 * ModuleJarMessage.java
 *
 * Created on March 6, 2007, 9:17 PM
 *
 */

package eunomia.messages.receptor.msg.rsp;

import eunomia.messages.BlockingMessage;
import eunomia.messages.Message;
import eunomia.messages.receptor.ResponceMessage;
import eunomia.util.oo.LargeTransfer;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 *
 * @author Mikhail Sosonkin
 */
public class ModuleJarMessage implements ResponceMessage, BlockingMessage {
    private String module;
    private LargeTransfer file;
    private int modVers;
    private boolean isLib;
    
    public ModuleJarMessage() {
    }

    public Message getCause() {
        return null;
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(module);
        out.writeObject(file);
        out.writeBoolean(isLib);
        if(isLib) {
            out.writeInt(modVers);
        }
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        module = (String)in.readObject();
        file = (LargeTransfer)in.readObject();
        if(isLib = in.readBoolean()) {
            modVers = in.readInt();
        }
    }

    public int getVersion() {
        return 0;
    }

    public void setVersion(int v) {
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public LargeTransfer getFile() {
        return file;
    }

    public void setFile(LargeTransfer file) {
        this.file = file;
    }

    public int getModuleVersion() {
        return modVers;
    }

    public void setModuleVersion(int modVers) {
        setLibrary(true);
        this.modVers = modVers;
    }

    public boolean isLibrary() {
        return isLib;
    }

    public void setLibrary(boolean isLib) {
        this.isLib = isLib;
    }
    
}
