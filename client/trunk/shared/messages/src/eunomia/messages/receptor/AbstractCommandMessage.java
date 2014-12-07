/*
 * AbstractCommandMessage.java
 *
 * Created on September 6, 2005, 4:14 PM
 *
 */

package eunomia.messages.receptor;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.security.SecureRandom;

/**
 *
 * @author Mikhail Sosonkin
 */
public abstract class AbstractCommandMessage implements CommandMessage {
    private static SecureRandom rand;
    
    static {
        try {
            rand = new SecureRandom();
        } catch(Exception e){
            e.printStackTrace();
        }
    }
    
    protected int hash;
    protected transient boolean readOnly = false;
    
    public AbstractCommandMessage(){
        hash = rand.nextInt();
    }
    
    public void setReadOnly(){
        readOnly = true;
    }
    
    public int hashCode(){
        return hash;
    }
    
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeInt(hash);
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        readOnly = true;
        hash = in.readInt();
    }
}