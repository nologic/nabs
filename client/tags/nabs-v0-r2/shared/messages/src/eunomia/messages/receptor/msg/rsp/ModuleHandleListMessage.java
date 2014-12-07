/*
 * ModuleHandleListMessage.java
 *
 * Created on October 23, 2005, 8:19 PM
 *
 */

package eunomia.messages.receptor.msg.rsp;

import eunomia.messages.receptor.ResponceMessage;
import eunomia.messages.*;
import eunomia.messages.receptor.*;
import java.io.*;
import java.util.*;

/**
 *
 * @author Mikhail Sosonkin
 */
public class ModuleHandleListMessage implements ResponceMessage {
    private List handles;
    private boolean readOnly;
    
    private static final long serialVersionUID = 7085935173667935936L;

    public ModuleHandleListMessage() {
        handles = new LinkedList();
        readOnly = false;
    }
 
    public int getVersion() {
        return 0;
    }

    public void setVersion(int v) {
    }
    
    public void addHandle(ModuleHandle handle){
        if(readOnly){
            throw new UnsupportedOperationException("addHandle: ModuleHandleListMessage is read only");
        }
        
        handles.add(handle);
    }
    
    public Iterator getHandles(){
        return handles.iterator();
    }
    
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        readOnly = true;
        int size = in.readInt();
        
        handles = new Vector(size);
        for(int i = 0; i < size; ++i){
            handles.add(in.readObject());
        }
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeInt(handles.size());
        
        Iterator it = handles.iterator();
        while(it.hasNext()){
            Externalizable ext = (Externalizable)it.next();
            out.writeObject(ext);
        }
    }

    public Message getCause() {
        return null;
    }
}