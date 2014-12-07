/*
 * TheHost.java
 *
 * Created on February 2, 2006, 9:56 PM
 *
 */

package eunomia.plugin.rec.hostDetails;

import eunomia.flow.FilterEntry;
import com.vivic.eunomia.module.Flow;
import eunomia.plugin.oth.hostDetails.conv.Conversation;
import eunomia.plugin.oth.hostDetails.conv.ConversationList;
import eunomia.receptor.module.NABFlow.NABFlow;
import java.io.*;

/**
 *
 * @author Mikhail Sosonkin
 */
public class TheHost {
    private FilterEntry[] entrySrc;
    private FilterEntry[] entryDst;
    private long host;

    private ConversationList cList;
    private HostData hData;
    
    public TheHost(long ip) {
        host = ip;

        entrySrc = new FilterEntry[2];
        entryDst = new FilterEntry[2];
        
        hData = new HostData();
        cList = new ConversationList();
        cList.setGCTimeInterval(20000, 15000);
    }
    
    public FilterEntry[] getEntrySrc() {
        return entrySrc;
    }

    public void addEntrySrc(FilterEntry eSrc) {
        for (int i = 0; i < entrySrc.length; i++) {
            if(entrySrc[i] == null) {
                entrySrc[i] = eSrc;
                return;
            }
        }
    }

    public FilterEntry[] getEntryDst() {
        return entryDst;
    }

    public void addEntryDst(FilterEntry eDst) {
        for (int i = 0; i < entryDst.length; i++) {
            if(entryDst[i] == null) {
                entryDst[i] = eDst;
                break;
            }
        }
    }
    
    public void newFlowSource(Flow flow, int[] types, long size, long time) {
        Conversation conv = cList.findConversation(flow.getDestinationIP(), flow.getSourcePort(), flow.getDestinationPort());
        
        conv.outgoing(types, size, time);
        hData.outgoing(types, size, time);
    }
    
    public void newFlowDestination(Flow flow, int[] types, long size, long time) {
        Conversation conv = cList.findConversation(flow.getSourceIP(), flow.getDestinationPort(), flow.getSourcePort());
        conv.incoming(types, size, time);
        hData.incoming(types, size, time);
    }
    
    public void writeOut(DataOutputStream dout) throws IOException {
        hData.writeOut(dout);
        cList.writeOut(dout);
    }
    
    public long getLongIp(){
        return host;
    }
}