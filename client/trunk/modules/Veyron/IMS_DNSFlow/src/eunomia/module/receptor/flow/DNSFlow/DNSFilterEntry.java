/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package eunomia.module.receptor.flow.DNSFlow;

import com.vivic.eunomia.filter.FilterEntry;
import com.vivic.eunomia.module.flow.Flow;
import eunomia.messages.FilterEntryMessage;
import eunomia.messages.Message;
import eunomia.module.receptor.flow.DNSFlow.messages.DNSFlowSpecificMessage;

/**
 *
 * @author justin
 */
public class DNSFilterEntry extends FilterEntry {
    private boolean[] allowType;
    private boolean[] allowProto;
    
    private StringBuilder summary;
    
    public DNSFilterEntry() {
        this(null);
    }
    
    public DNSFilterEntry(FilterEntryMessage fem) {
        super(fem);
        
        summary = new StringBuilder();
        
        allowType = new boolean[DNSFlow.NUM_TYPES];
        for(int i = 0; i < allowType.length; i++) {
            allowType[i] = true;
        }
        
        allowProto = new boolean[]{true, true};
        
        if(fem != null) {
            processSpecific(fem.getSpecific());
        }
    }
    
    private void processSpecific(Message msg) {
        if(msg != null && msg instanceof DNSFlowSpecificMessage) {
            DNSFlowSpecificMessage dsm = (DNSFlowSpecificMessage) msg;

            System.arraycopy(dsm.getAllowType(), 0, allowType, 0, allowType.length);
            System.arraycopy(dsm.getAllowProto(), 0, allowProto, 0, allowProto.length);
        }
    }
    
    public boolean setAllowedProtocol(short protocol, boolean allow) {
        if (protocol == DNSFlow.PROTOCOL_TCP) {
            allowProto[0] = allow;
            return true;
        }
        if (protocol == DNSFlow.PROTOCOL_UDP) {
            allowProto[1] = allow;
            return true;
        }
        return false;
    }
    
    public boolean isAllowedProtocol(short protocol) {
        if (protocol == DNSFlow.PROTOCOL_TCP) {
            return allowProto[0];
        }
        if (protocol == DNSFlow.PROTOCOL_UDP) {
            return allowProto[1];
        }
        return false;
    }
    
    public boolean isAllowedProtocol(int index) {
        return allowProto[index];
    }
    
    public boolean setAllowedType(char type, boolean allow) {
        if (type == DNSFlow.TYPE_A) {
            allowType[0] = allow;
            return true;
        }
        if (type == DNSFlow.TYPE_CNAME) {
            allowType[1] = allow;
            return true;
        }
        if (type == DNSFlow.TYPE_PTR) {
            allowType[2] = allow;
            return true;
        }
        if (type == DNSFlow.TYPE_MX) {
            allowType[3] = allow;
            return true;
        }
        return false;
    }
    
    public void setAllowedType(int index, boolean allow) {
        allowType[index] = allow;
    }
    
    public boolean isAllowedType(char type) {
        if (type == DNSFlow.TYPE_A) {
            return allowType[0];
        }
        if (type == DNSFlow.TYPE_CNAME) {
            return allowType[1];
        }
        if (type == DNSFlow.TYPE_PTR) {
            return allowType[2];
        }
        if (type == DNSFlow.TYPE_MX) {
            return allowType[3];
        }
        return false;
    }
    
    public boolean isAllowedType(int index) {
        return allowType[index];
    }
    
    public boolean inRangeFlow(Flow flow) {
        if (flow instanceof DNSFlow) {
            DNSFlow f = (DNSFlow) flow;
            
            if (!isAllowedProtocol(f.getProtocol())) {
                return false;
            }
            
            if (!isAllowedType(f.getQueryType())) {
                return false;
            }
        }
        
        return true;
    }
    
    public String getSpecificSummary() {
        summary.delete(0, summary.length());
        
        for(int i = 0; i < allowType.length; i++) {
            if(allowType[i]) {
                summary.append(DNSFlow.typeNames[i]);
                summary.append(" ");
            }
        }
        
        return summary.toString();
    }
    
    public String getModuleName() {
        return "DNSFlow";
    }
    
    protected Message getSpecific() {
        DNSFlowSpecificMessage dsm = new DNSFlowSpecificMessage();
        
        dsm.setAllowProto(allowProto);
        dsm.setAllowType(allowType);
        
        return dsm;
    }
}
