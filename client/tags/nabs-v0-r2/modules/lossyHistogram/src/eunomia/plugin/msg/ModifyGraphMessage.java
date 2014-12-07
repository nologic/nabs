/*
 * ModifyGraphMessage.java
 *
 * Created on January 21, 2006, 2:50 PM
 *
 */

package eunomia.plugin.msg;

import eunomia.flow.*;
import eunomia.messages.FilterEntryMessage;
import eunomia.messages.receptor.ModuleHandle;
import eunomia.receptor.module.NABFlow.NABFilterEntry;
import java.io.*;

/**
 *
 * @author Mikhail Sosonkin
 */
public class ModifyGraphMessage implements Externalizable {
    private static final long serialVersionUID = 4205380471668841126L;
    
    private NABFilterEntry entry1;
    private NABFilterEntry entry2;
    private int flowID;
    private ModuleHandle handle;
    
    public ModifyGraphMessage() {
    }

    public NABFilterEntry getEntry1() {
        return entry1;
    }

    public void setEntry1(NABFilterEntry entry1) {
        this.entry1 = entry1;
    }

    public NABFilterEntry getEntry2() {
        return entry2;
    }

    public void setEntry2(NABFilterEntry entry2) {
        this.entry2 = entry2;
    }
    
    public void setFlowID(int id){
        flowID = id;
    }
    
    public int getFlowID(){
        return flowID;
    }
    
    public ModuleHandle getHandle(){
        return handle;
    }
    
    public void setHandle(ModuleHandle h){
        handle = h;
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        flowID = in.readInt();
        entry1 = new NABFilterEntry((FilterEntryMessage)in.readObject());
        if(in.readBoolean()){
            entry2 = new NABFilterEntry((FilterEntryMessage)in.readObject());
        }
        
        if(in.readBoolean()){
            handle = (ModuleHandle)in.readObject();
        }
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeInt(flowID);
        out.writeObject(entry1.getFilterEntryMessage());
        out.writeBoolean(entry2 != null);
        if(entry2 != null){
            out.writeObject(entry2.getFilterEntryMessage());
        }
        out.writeBoolean(handle != null);
        if(handle != null){
            out.writeObject(handle);
        }
    }
}