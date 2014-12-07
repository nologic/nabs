/*
 * GetModuleControlDataMessage.java
 *
 * Created on December 24, 2005, 5:48 PM
 *
 */

package eunomia.messages.module.msg;

import eunomia.messages.module.AbstractModuleMessage;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 *
 * @author Mikhail Sosonkin
 */
public class GetModuleControlDataMessage extends AbstractModuleMessage {
    private static final long serialVersionUID = 632652762194531494L;
    
    public GetModuleControlDataMessage() {
    }
    
    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);

    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);
    }
}