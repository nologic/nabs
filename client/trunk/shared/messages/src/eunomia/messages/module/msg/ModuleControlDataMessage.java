/*
 * ModuleControlDataMessage.java
 *
 * Created on December 19, 2005, 6:38 PM
 *
 */

package eunomia.messages.module.msg;

import eunomia.messages.module.AbstractModuleMessage;
import java.io.*;

/**
 *
 * @author Mikhail Sosonkin.
 */
public class ModuleControlDataMessage extends GenericModuleMessage {
    private static final long serialVersionUID = 8309608198169507410L;
    
    public ModuleControlDataMessage(){
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);
    }
}