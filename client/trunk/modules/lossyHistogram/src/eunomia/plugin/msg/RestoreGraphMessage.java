/*
 * RestoreGraphMessage.java
 *
 * Created on June 9, 2007, 11:44 AM
 *
 */

package eunomia.plugin.msg;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 *
 * @author Mikhail Sosonkin
 */
public class RestoreGraphMessage implements Externalizable {
    private static final long serialVersionUID = 4691389240943904442L;
    
    public RestoreGraphMessage() {
    }

    public void writeExternal(ObjectOutput out) throws IOException {
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
    }
    
}
