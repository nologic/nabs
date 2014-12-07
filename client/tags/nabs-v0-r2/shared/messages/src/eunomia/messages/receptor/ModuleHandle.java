/*
 * ModuleHandle.java
 *
 * Created on October 23, 2005, 4:56 PM
 *
 */

package eunomia.messages.receptor;

import eunomia.messages.Message;
import java.io.*;

/**
 * 
 * @author Mikhail Sosonkin
 */
public class ModuleHandle implements Message {
    private String className;
    private int instID;
    private boolean readOnly;
    
    private static final long serialVersionUID = 9180907134744732041L;
    
    public ModuleHandle() {
        readOnly = false;
    }
    
    public String getModuleName(){
        return className;
    }
    
    public int hashCode(){
        return instID;
    }
    
    public boolean equals(Object o){
        return hashCode() == o.hashCode();
    }
    
    public int getInstanceID(){
        return instID;
    }
    
    public void setReadOnly(){
        readOnly = true;
    }
    
    public void setModuleName(String className){
        if(readOnly){
            throw new UnsupportedOperationException("setClassName: ModuleHandle is read only");
        }
        
        this.className = className;
    }
    
    public void setInstanceID(int id){
        if(readOnly){
            throw new UnsupportedOperationException("setInstanceID: ModuleHandle is read only");
        }
        
        instID = id;
    }
    
    public int getVersion() {
        return 0;
    }

    public void setVersion(int v) {
    }
    
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(className);
        out.writeInt(instID);
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        readOnly = true;
        className = (String)in.readObject();
        instID = in.readInt();
    }
    
    public String toString(){
        return className + " (" + instID + ")";
    }
}