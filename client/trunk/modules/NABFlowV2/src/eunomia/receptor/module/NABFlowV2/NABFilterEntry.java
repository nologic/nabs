/*
 * FilterEntry.java
 *
 * Created on July 31, 2005, 2:50 PM
 */
package eunomia.receptor.module.NABFlowV2;

import com.vivic.eunomia.filter.FilterEntry;
import com.vivic.eunomia.module.flow.Flow;
import eunomia.messages.FilterEntryMessage;
import eunomia.messages.Message;
import eunomia.receptor.module.NABFlowV2.messages.NABFlowSpecificMessage;

/**
 * The class that defines the specific parts of the filter entry for this module.
 * There is an option to override generic features, however that depends on the
 * module writer.
 * @author Mikhail Sosonkin
 */
public class NABFilterEntry extends FilterEntry {
    /**
     * This is really the only thing that this filter entry does specific, it keeps
     * track of what types should be filtered out.
     */

    // Flag ranges
    private boolean doFlagCheck;
    private int[][] tcpFlagRanges;

    private boolean[] allowType;
    private boolean[] allowProtocol;
    
    private StringBuilder summary;

    /**
     * Created an entry with default values.
     */
    public NABFilterEntry() {
        this(null);
    }

    /**
     * 
     * @param fem The message with all the info, will be sent in by the middleware
     */
    public NABFilterEntry(FilterEntryMessage fem) {
        super(fem);

        summary = new StringBuilder();

        // User experience shows that generally they want all type to be allowed
        // on some specialized cases the types can be excluded.
        allowType = new boolean[NABFlowV2.NUM_TYPES];
        for(int i = 0; i < allowType.length; i++) {
            allowType[i] = true;
        }

        // At the moment NABFlowV2 supports only TCP(6) and UDP(17). so doing
        // module 2, 1 is UDP, 0 is TCP
        allowProtocol = new boolean[]{true, true};

        doFlagCheck = false;
        tcpFlagRanges = new int[NABFlowV2.NUM_TCP_FLAGS][];
        for(int i = 0; i < tcpFlagRanges.length; i++) {
            tcpFlagRanges[i] = new int[]{0, 0xFFFFFFFF};
        }

        if(fem != null) {
            // if there is not specific information then set the default
            // to allow all types through.
            processSpecific(fem.getSpecific());
        }
    }

    /**
     * Called by the constructor to process the message and set the allowed types.
     * @param msg The message that is generated by the <I>getSpecific()</I> method.
     */
    private void processSpecific(Message msg) {
        if(msg != null && msg instanceof NABFlowSpecificMessage) {
            NABFlowSpecificMessage nsm = (NABFlowSpecificMessage)msg;

            System.arraycopy(nsm.getAllowType(), 0, allowType, 0, allowType.length);
            System.arraycopy(nsm.getAllowProto(), 0, allowProtocol, 0, allowProtocol.length);
            
            int[][] flags = nsm.getTcpFlagRanges();
            for(int i = 0; i < flags.length; i++){
                tcpFlagRanges[i][0] = flags[i][0];
                tcpFlagRanges[i][1] = flags[i][1];
            }
            
            doFlagCheck = nsm.isDoFlagCheck();
        }
    }

    /**
     * 
     * @param i Type number
     * @return the allow value
     */
    public boolean isAllowedType(int i) {
        return allowType[i];
    }

    /**
     * 
     * @param i The type number
     * @param v new allow value
     */
    public void setAllowedType(int i, boolean v) {
        allowType[i] = v;
    }

    public void setAllowedProtocol(int proto, boolean v) {
        allowProtocol[proto % 2] = v;
    }

    public boolean isAllowedProtocol(int proto) {
        return allowProtocol[proto % 2];
    }
    
    public void setFlagRange(int flag, int l, int u) {
        tcpFlagRanges[flag][0] = l;
        tcpFlagRanges[flag][1] = u;
    }
    
    public int getFlagLowerRange(int flag) {
        return tcpFlagRanges[flag][0];
    }
    
    public int getFlagUpperRange(int flag) {
        return tcpFlagRanges[flag][1];
    }
    
    public void setDoFlagCheck(boolean dc) {
        doFlagCheck = dc;
    }
    
    public boolean isDoFlagCheck() {
        return doFlagCheck;
    }

    /**
     * Checks if the flow is matches this entry, in this case the check is to return
     * the allow value for flow's type.
     * @param flow Flow to check
     * @return flag indicating whether or not the flow is allowed based on the type.
     */
    public boolean inRangeFlow(Flow flow) {
        // This entry can only check the NABFlow flows. Everything else is allowed
        // to pass. However, again, the actual enforcement is done by the Flow
        // Processor module.

        if(flow instanceof NABFlowV2) {
            NABFlowV2 f = (NABFlowV2)flow;

            boolean typeCheck = false;
            // at least one of the allowed types must be greater than 0
            int[] types = f.getTypeCount();
            for(int i = types.length - 1; i != -1; --i) {
                if(allowType[i] && types[i] > 0) {
                    typeCheck = true;
                    break;
                }
            }
            
            // check if protocol is allowed
            boolean protoCheck = allowProtocol[(int)(f.getProtocol() & 0xFF) % 2];

            // all flag numbers have to be in range.
            boolean flagCheck = !doFlagCheck;
            if(doFlagCheck) {
                for(int i = tcpFlagRanges.length - 1; i != -1; --i) {
                    int[] range = tcpFlagRanges[i];
                    int curFlag = f.getTcpFlag(i);
                    
                    if(curFlag < range[0] || curFlag > range[1]) {
                        flagCheck = false;
                        break;
                    }
                }
            }

            return typeCheck && protoCheck && flagCheck;
        }

        return true;
    }

    /**
     * Generates the text about the entry to be displayed to the user. 
     * Should be short.
     * @return short description of the module specific parameters.
     */
    public String getSpecificSummary() {
        summary.delete(0, summary.length());
        for(int i = 0; i < allowType.length; i++) {
            if(allowType[i]) {
                summary.append(NABFlowV2.typeNames[i]);
                summary.append(" ");
            }
        }
        
        return summary.toString();
    }

    /**
     * This value must be the same as that returned by the Descriptor.moduleName() 
     * method. This is used to determined which editor to use.
     * @return The name of the module that generated this flow entry.
     */
    public String getModuleName() {
        return "NABFlowV2";
    }

    /**
     * This method is called by the super class to construct a complete serialized
     * stream of the entry.
     * @return Message that contains enough enformation to reconstruct the specific
     * data.
     */
    protected Message getSpecific() {
        NABFlowSpecificMessage nsm = new NABFlowSpecificMessage();
        nsm.setAllowType(allowType);
        nsm.setAllowProto(allowProtocol);
        nsm.setDoFlagCheck(doFlagCheck);
        nsm.setTcpFlagRanges(tcpFlagRanges);

        return nsm;
    }
}