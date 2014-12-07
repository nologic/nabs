package eunomia.module.receptor.flow.DNSFlow.messages;

import eunomia.messages.Message;
import eunomia.module.receptor.flow.DNSFlow.DNSFlow;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 *
 * @author justin
 */
public class DNSFlowSpecificMessage implements Message {
    private boolean[] allowProto;
    private boolean[] allowType;
    
    public DNSFlowSpecificMessage() {
        allowType = new boolean[DNSFlow.NUM_TYPES];
        allowProto = new boolean[2];
    }

    public void setAllowProto(boolean[] allowProto) {
        this.allowProto = allowProto;
    }

    public boolean[] getAllowProto() {
        return allowProto;
    }

    public void setAllowType(boolean[] a) {
        for (int i = 0; i < allowType.length; i++) {
            allowType[i] = a[i];
        }
    }

    public boolean[] getAllowType() {
        return allowType;
    }
    
    public void writeExternal(ObjectOutput out) throws IOException {
        for(int i = 0; i < allowProto.length; i++) {
            out.writeBoolean(allowProto[i]);
        }
        
        for (int i = 0; i < allowType.length; i++) {
            out.writeBoolean(allowType[i]);
        }
    }
    
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        for(int i = 0; i < allowProto.length; i++) {
            allowProto[i] = in.readBoolean();
        }
        
        for (int i = 0; i < allowType.length; i++) {
            allowType[i] = in.readBoolean();
        }
    }
    
    public int getVersion() {
        return 0;
    }
    
    public void setVersion(int v) {
    }
}