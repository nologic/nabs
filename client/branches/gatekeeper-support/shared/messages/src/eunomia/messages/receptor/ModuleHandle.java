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
    public static final int 
            TYPE_PROC = 0, // Flow processor.
            TYPE_FLOW = 1, // Flow producer.
            TYPE_ANLZ = 2, // Static analysis
            TYPE_COLL = 3; // Flow collection (to DB).

    private String className;
    private int moduleType;
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
        return instID << 2 | moduleType;
    }
    
    public int getModuleType() {
        return moduleType;
    }

    public void setModuleType(int type) {
        if(readOnly) {
            throw new UnsupportedOperationException("setModuleType: ModuleHandle is read only");
        }

        this.moduleType = type;
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
        if(readOnly) {
            throw new UnsupportedOperationException("setClassName: ModuleHandle is read only");
        }
        
        this.className = className;
    }
    
    public void setInstanceID(int id){
        if(readOnly) {
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
        out.writeInt(moduleType);
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        readOnly = true;
        className = (String)in.readObject();
        instID = in.readInt();
        moduleType = in.readInt();
    }
    
    public String toString(){
        return moduleType + ":" + className + " (" + instID + ")";
    }
}