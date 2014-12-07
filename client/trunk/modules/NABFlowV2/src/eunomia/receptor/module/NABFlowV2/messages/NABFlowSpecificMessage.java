/*
 * NABFlowSpecificMessage.java
 *
 * Created on July 12, 2006, 10:33 PM
 *
 */

package eunomia.receptor.module.NABFlowV2.messages;

import eunomia.messages.Message;
import eunomia.receptor.module.NABFlowV2.NABFlowV2;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * Flow specific information is required to be kept in a Message type. This class
 * maintain a list of allowed flows.
 * @author Mikhail Sosonkin
 */
public class NABFlowSpecificMessage implements Message {
    /**
     * An array of allowed flows. Mirror for the same array in the filter entry.
     */
    private boolean[] allowType;
    private boolean[] allowProto;
    private boolean doFlagCheck;
    private int[][] tcpFlagRanges;
    
    public NABFlowSpecificMessage() {
        allowType = new boolean[NABFlowV2.NUM_TYPES];
        allowProto = new boolean[2];
        tcpFlagRanges = new int[NABFlowV2.NUM_TCP_FLAGS][2];
    }
    
    public boolean[] getAllowProto() {
        return allowProto;
    }

    public void setAllowProto(boolean[] allowProto) {
        this.allowProto = allowProto;
    }

    public boolean[] getAllowType() {
        return allowType;
    }

    public void setAllowType(boolean[] a) {
        for (int i = 0; i < allowType.length; i++) {
            allowType[i] = a[i];
        }
    }
    
    public boolean isDoFlagCheck() {
        return doFlagCheck;
    }

    public void setDoFlagCheck(boolean doFlagCheck) {
        this.doFlagCheck = doFlagCheck;
    }

    public int[][] getTcpFlagRanges() {
        return tcpFlagRanges;
    }

    public void setTcpFlagRanges(int[][] tcpFlagRanges) {
        this.tcpFlagRanges = tcpFlagRanges;
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        for (int i = 0; i < allowType.length; i++) {
            out.writeBoolean(allowType[i]);
        }
        
        for(int i = 0; i < allowProto.length; i++) {
            out.writeBoolean(allowProto[i]);
        }
        
        for(int i = 0; i < tcpFlagRanges.length; i++) {
            out.writeInt(tcpFlagRanges[i][0]);
            out.writeInt(tcpFlagRanges[i][1]);
        }
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        for (int i = 0; i < allowType.length; i++) {
            allowType[i] = in.readBoolean();
        }
        
        for(int i = 0; i < allowProto.length; i++) {
            allowProto[i] = in.readBoolean();
        }
        
        for(int i = 0; i < tcpFlagRanges.length; i++) {
            tcpFlagRanges[i][0] = in.readInt();
            tcpFlagRanges[i][1] = in.readInt();
        }
    }

    public int getVersion() {
        return 0;
    }

    public void setVersion(int v) {
    }
}