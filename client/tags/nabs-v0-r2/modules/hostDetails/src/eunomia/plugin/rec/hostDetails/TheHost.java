/*
 * TheHost.java
 *
 * Created on February 2, 2006, 9:56 PM
 *
 */

package eunomia.plugin.rec.hostDetails;

import eunomia.flow.*;
import eunomia.plugin.oth.hostDetails.conv.Conversation;
import eunomia.plugin.oth.hostDetails.conv.ConversationList;
import eunomia.receptor.module.NABFlow.NABFilterEntry;
import eunomia.receptor.module.NABFlow.NABFlow;
import java.io.*;

/**
 *
 * @author Mikhail Sosonkin
 */
public class TheHost {
    private NABFilterEntry entrySrc;
    private NABFilterEntry entryDst;
    private long host;

    private ConversationList cList;
    private HostData hData;
    
    public TheHost(long ip) {
        host = ip;
        
        hData = new HostData();
        cList = new ConversationList();
        cList.setGCTimeInterval(20000, 15000);
    }
    
    public NABFilterEntry getEntrySrc() {
        return entrySrc;
    }

    public void setEntrySrc(NABFilterEntry entrySrc) {
        this.entrySrc = entrySrc;
    }

    public NABFilterEntry getEntryDst() {
        return entryDst;
    }

    public void setEntryDst(NABFilterEntry entryDst) {
        this.entryDst = entryDst;
    }
    
    public void newFlowSource(NABFlow flow) {
        long size = (long)flow.getSize();
        int type = flow.getType();
        
        Conversation conv = cList.findConversation(flow.getDestinationIP(), flow.getSourcePort(), flow.getDestinationPort());
        conv.outgoing(size, type);
        hData.outgoing(flow);
    }
    
    public void newFlowDestination(NABFlow flow) {
        long size = (long)flow.getSize();
        int type = flow.getType();
        
        Conversation conv = cList.findConversation(flow.getSourceIP(), flow.getDestinationPort(), flow.getSourcePort());
        conv.incoming(size, type);
        hData.incoming(flow);
    }
    
    public void writeOut(DataOutputStream dout) throws IOException {
        hData.writeOut(dout);
        cList.writeOut(dout);
    }
    
    public long getLongIp(){
        return host;
    }
}