/*
 * StatusMessage.java
 *
 * Created on October 19, 2005, 8:12 PM
 *
 */

package eunomia.messages.module.msg;

import eunomia.messages.*;
import eunomia.messages.module.*;
import java.io.*;

/**
 *
 * @author Mikhail Sosonkin
 */
public class ModuleStatusMessage extends GenericModuleMessage {
    private static final long serialVersionUID = 8638998296819164426L;
    
    public ModuleStatusMessage(){
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);
    }
}