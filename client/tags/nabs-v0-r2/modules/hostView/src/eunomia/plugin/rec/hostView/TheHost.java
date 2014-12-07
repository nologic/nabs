/*
 * TheHost.java
 *
 * Created on February 2, 2006, 9:56 PM
 *
 */

package eunomia.plugin.rec.hostView;

import eunomia.flow.*;
import eunomia.receptor.module.NABFlow.NABFilterEntry;
import eunomia.receptor.module.NABFlow.NABFlow;
import java.io.*;
import java.net.*;

/**
 *
 * @author Mikhail Sosonkin
 */
public class TheHost {
    private NABFilterEntry entrySrc;
    private NABFilterEntry entryDst;
    private HostData hData;
    private long host;
    
    public TheHost(long ip) {
        host = ip;
        hData = new HostData();
    }
    
    public void updateData() {
        hData.computeRates();
        hData.updateHistory();
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
        hData.outgoing(flow);
    }
    
    public void newFlowDestination(NABFlow flow) {
        hData.incoming(flow);
    }
    
    public void writeOut(DataOutputStream dout) throws IOException {
        hData.writeOut(dout);
    }
    
    public long getLongIp(){
        return host;
    }
}