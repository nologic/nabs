/*
 * ModuleDescriptor.java
 *
 * Created on August 8, 2006, 10:34 PM
 *
 */

package eunomia.messages.receptor;

import eunomia.messages.Message;
import java.io.*;

/**
 *
 * @author Mikhail Sosonkin
 */
public class ModuleDescriptor implements Message {
    public ModuleDescriptor() {
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
    }

    public int getVersion() {
        return 0;
    }

    public void setVersion(int v) {
    }
}