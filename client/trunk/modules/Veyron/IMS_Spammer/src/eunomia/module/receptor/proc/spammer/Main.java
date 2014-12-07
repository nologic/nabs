/*
 * Main.java
 *
 * Created on November 4, 2007, 9:02 PM
 *
 */
package eunomia.module.receptor.proc.spammer;

import com.vivic.eunomia.module.flow.Flow;
import com.vivic.eunomia.module.flow.FlowModule;
import com.vivic.eunomia.module.receptor.FlowProcessor;
import com.vivic.eunomia.module.receptor.ReceptorProcessorModule;
import com.vivic.eunomia.filter.Filter;
import eunomia.module.common.proc.spammer.MailServer;
import eunomia.receptor.module.NABFlowV2.NABFilterEntry;
import eunomia.receptor.module.NABFlowV2.NABFlowV2;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 *
 * @author Mikhail Sosonkin
 */
public class Main implements ReceptorProcessorModule, FlowProcessor {

    private MailTracker tracker;
    
    private Filter filter;
    private Filter eventStartFilter;
    private Filter eventEndFilter;
    
    private boolean doProc;

    public Main() {
        doProc = true;
        filter = new Filter();
        tracker = new MailTracker();
        
        eventStartFilter = new Filter();
        eventEndFilter = new Filter();
        
        initializeFilter(filter, eventStartFilter, eventEndFilter);
    }
    
    private void initializeFilter(Filter f, Filter cStart, Filter cEnd) {
        // the general filter, we want the SMTP port... in this case 25
        NABFilterEntry srcEntry = new NABFilterEntry();
        srcEntry.setDestinationPortRange(25, 25);
        f.addFilterWhite(srcEntry);
        
        // filter for starting a connection, at least 2 SYNs
        NABFilterEntry conStart = new NABFilterEntry();
        conStart.setDoFlagCheck(true);
        conStart.setFlagRange(NABFlowV2.TCP_SYN, 2, 0xFFFFFFFF);
        cStart.addFilterWhite(conStart);
        
        // filter for ending a connection, at least 2 FINs
        NABFilterEntry conEnd = new NABFilterEntry();
        conEnd.setDoFlagCheck(true);
        conEnd.setFlagRange(NABFlowV2.TCP_FIN, 2, 0xFFFFFFFF);
        cEnd.addFilterWhite(conEnd);
    }

    public void destroy() {
    }

    public FlowProcessor getFlowProcessor() {
        return this;
    }

    public void updateStatus(OutputStream out) throws IOException {
    }

    public void setControlData(InputStream in) throws IOException {
    }

    public void getControlData(OutputStream out) throws IOException {
    }

    public void processMessage(DataInputStream in, DataOutputStream out) throws IOException {
    }

    public void start() {
        doProc = true;
    }

    public void stop() {
        doProc = false;
    }

    public void reset() {
    }

    public void setProperty(String name, Object value) {
    }

    public Object getProperty(String name) {
        return null;
    }

    public Object[] getCommands() {
        return null;
    }

    public Object executeCommand(Object command, Object[] parameters) {
        return null;
    }

    public void setFilter(Filter filter) {
        this.filter = filter;
    }

    public Filter getFilter() {
        return filter;
    }

    public void newFlow(Flow flow) {
        if(doProc && flow instanceof NABFlowV2 && filter.allow(flow)) {
            NABFlowV2 f = (NABFlowV2)flow;
            
            // at this point we are assuming an STMP session has happened.
            boolean isStart = eventStartFilter.allow(flow);
            boolean isEnd = eventEndFilter.allow(flow);
            
            if(isStart || isEnd || f.getSize() > 0) {
                tracker.examineFlow(f, isEnd);
            }
        }
    }
    
    public boolean accept(FlowModule module) {
        return module.getNewFlowInstance() instanceof NABFlowV2;
    }
    
    public boolean isSpammer(long ip) {
        MailServer serv = tracker.getMailServer(ip);
        
        return serv.getTotalConnectionCount(tracker.getLastFlowTime(), MailTracker.HOUR) > tracker.getMedian();
    }
    
    public void addMailTrackerLintener(MailTrackerListener l) {
        tracker.addMailTrackerLintener(l);
    }
    
    public void removeMailTrackerLintener(MailTrackerListener l) {
        tracker.removeMailTrackerLintener(l);
    }
}
