/*
 * InterModuleOutputStream.java
 *
 * Created on February 1, 2007, 10:28 PM
 *
 */

package eunomia.module.comm;

import eunomia.core.receptor.Receptor;
import eunomia.messages.module.msg.GenericModuleMessage;
import eunomia.messages.module.msg.ModuleInterCommMessage;
import eunomia.messages.receptor.ModuleHandle;
import java.io.DataOutputStream;

/**
 *
 * @author Mikhail Sosonkin
 */
public class InterModuleOutComm extends DataOutputStream {
    private GenericModuleMessage gmm;
    private Receptor receptor;
    
    public InterModuleOutComm(ModuleHandle handle, Receptor rec) {
        super(null);
        
        gmm = new ModuleInterCommMessage();
        gmm.setModuleHandle(handle);
        
        this.receptor = rec;
        this.out = gmm.getOutputStream();
    }
    
    public void close() {
        receptor.sendMessage(gmm);
    }
}