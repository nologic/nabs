/*
 * TheHost.java
 *
 * Created on February 2, 2006, 9:56 PM
 *
 */

package eunomia.plugin.rec.hostView;

import com.vivic.eunomia.module.Flow;
import eunomia.flow.*;
import java.io.*;

/**
 *
 * @author Mikhail Sosonkin
 */
public class TheHost {
    private FilterEntry[] entrySrc;
    private FilterEntry[] entryDst;
    private HostData hData;
    private long host;
    
    public TheHost(long ip) {
        host = ip;
        hData = new HostData();
        
        entrySrc = new FilterEntry[2];
        entryDst = new FilterEntry[2];
    }
    
    public void updateData() {
        hData.computeRates();
        hData.updateHistory();
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
        hData.outgoing(types, size, time);
    }
    
    public void newFlowDestination(Flow flow, int[] types, long size, long time) {
        hData.incoming(types, size, time);
    }
    
    public void writeOut(DataOutputStream dout) throws IOException {
        hData.writeOut(dout);
    }
    
    public long getLongIp(){
        return host;
    }
}