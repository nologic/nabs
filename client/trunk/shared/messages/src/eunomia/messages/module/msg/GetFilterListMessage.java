/*
 * GetFilterList.java
 *
 * Created on January 9, 2006, 8:53 PM
 *
 */

package eunomia.messages.module.msg;

import eunomia.messages.module.AbstractModuleMessage;
import eunomia.messages.receptor.ModuleHandle;
import java.io.*;

/**
 *
 * @author Mikhail Sosonkin
 */
public class GetFilterListMessage extends AbstractModuleMessage {
    private static final long serialVersionUID = 5931597597260468537L;

    public GetFilterListMessage() {
    }
    
    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);
    }
    
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);
    }
}