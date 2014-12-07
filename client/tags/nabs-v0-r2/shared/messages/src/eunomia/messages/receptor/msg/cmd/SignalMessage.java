/*
 * SignalMessage.java
 *
 * Created on December 9, 2005, 11:51 AM
 */

package eunomia.messages.receptor.msg.cmd;

import eunomia.messages.Message;
import eunomia.messages.receptor.*;
import java.io.*;

/**
 *
 * @author Mikhail Sosonkin
 */
public class SignalMessage extends AbstractCommandMessage {
    public static final int SIG_VOID = -1;
    public static final int SIG_STATUS = 0;
    
    private int signal;
    
    private static final long serialVersionUID = 5811723138888104399L;
            
    public SignalMessage() {
        this(SIG_VOID);
    }
    
    public SignalMessage(int sig){
        signal = sig;
    }
    
    public int getSignal(){
        return signal;
    }
    
    public int getVersion() {
        return 0;
    }

    public void setVersion(int v) {
    }
    
    public int getCommandID(){
        return 0;
    }
    
    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);
        out.writeInt(signal);
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);
        signal = in.readInt();
    }
}