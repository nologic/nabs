/*
 * AbstractModuleMessage.java
 *
 * Created on October 19, 2005, 8:35 PM
 *
 */

package eunomia.messages.module;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.security.SecureRandom;

/**
 *
 * @author Mikhail Sosonkin
 */
public abstract class AbstractModuleMessage implements ModuleMessage {
    private static SecureRandom rand;
    
    static {
        try {
            rand = new SecureRandom();
        } catch(Exception e){
            e.printStackTrace();
        }
    }
    
    protected int hash;
    private int modId;
    protected transient boolean readOnly = false;
    
    public AbstractModuleMessage() {
        hash = rand.nextInt();
    }
    
    public void setReadOnly(){
        readOnly = true;
    }
    
    public void setModuleID(int id){
        modId = id;
    }
    
    public int getModuleID(){
        return modId;
    }
    
    public int hashCode(){
        return hash;
    }
    
    public int getVersion(){
        return 0;
    }
    
    public void setVersion(int v){
    }
        
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeInt(hash);
        out.writeInt(modId);
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        readOnly = true;
        hash = in.readInt();
        modId = in.readInt();
    }
}
