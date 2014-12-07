/*
 * HostView.java
 *
 * Created on August 9, 2005, 4:20 PM
 *
 */

package eunomia.plugin.rec.hostView;

import com.vivic.eunomia.module.Flow;
import com.vivic.eunomia.module.receptor.FlowProcessor;
import com.vivic.eunomia.module.receptor.ReceptorModule;
import eunomia.plugin.msg.AddRemoveHostMessage;
import eunomia.plugin.msg.OpenDetailsMessage;
import eunomia.flow.Filter;
import eunomia.receptor.module.NABFlow.NABFlow;
import eunomia.receptor.module.NABFlowV2.NABFlowV2;
import com.vivic.eunomia.module.receptor.FlowModule;
import com.vivic.eunomia.sys.receptor.SieveContext;
import eunomia.flow.FilterEntry;
import eunomia.util.io.EunomiaObjectInputStream;
import eunomia.util.number.ModLong;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import org.apache.log4j.Logger;


/**
 *
 * @author Mikhail Sosonkin
 */
public class Main extends TimerTask implements ReceptorModule, FlowProcessor, Runnable {
    public static final int TYPE_COUNT = NABFlow.NUM_TYPES;
    public static final int PAYLOAD_SIZE = NABFlowV2.MAX_PAYLOAD;

    private List hosts;
    private Filter filter;
    private HashMap ipToHost;
    private ModLong tmpLong;
    private int[] flowTypes;
    
    private static Logger logger;
    
    static {
        logger = Logger.getLogger(Main.class);
    }
    
    public Main() {
        hosts = new Vector();
        filter = new Filter();
        ipToHost = new HashMap();
        tmpLong = new ModLong();
        flowTypes = new int[TYPE_COUNT];
        
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(this, 1000, 1000);
    }
    
    public void run(){
        Object[] hsts = hosts.toArray();
        for(int i = 0; i < hsts.length; i++){
            TheHost host = (TheHost)hsts[i];
            host.updateData();
        }
    }
    
    public void removeHost(long ip){
        Long ident;

        ident = new Long(ip);
        
        TheHost hostViewer = null;
        synchronized(ipToHost){
            hostViewer = (TheHost)ipToHost.remove(ident);
        }
                
        if(hostViewer == null){
            return;
        }
        
        FilterEntry[] src = hostViewer.getEntrySrc();
        FilterEntry[] dst = hostViewer.getEntryDst();
        for (int i = 0; i < src.length; i++) {
            filter.removeFilterWhite(src[i]);
            filter.removeFilterWhite(dst[i]);
        }
        
        hosts.remove(hostViewer);
    }
    
    public void addHost(long ip){
        Long ident;

        ident = new Long(ip);
        
        if(ipToHost.containsKey(ident)){
            //logger.info("Host " + host + " is already on the list");
            return;
        }
        
        TheHost hostViewer = new TheHost(ip);
        
        FlowModule nabFlow = SieveContext.getModuleManager().getFlowModuleInstance("NABFlow");
        FlowModule nab2Flow = SieveContext.getModuleManager().getFlowModuleInstance("NABFlowV2");

        FilterEntry entrySrc = nabFlow.getNewFilterEntry(null);
        FilterEntry entryDst = nabFlow.getNewFilterEntry(null);
        FilterEntry entrySrc2 = nab2Flow.getNewFilterEntry(null);
        FilterEntry entryDst2 = nab2Flow.getNewFilterEntry(null);
        
        entrySrc.setSourceIpRange(ip, ip);
        entryDst.setDestinationIpRange(ip, ip);
        hostViewer.addEntryDst(entryDst);
        hostViewer.addEntrySrc(entrySrc);
        filter.addFilterWhite(entrySrc);
        filter.addFilterWhite(entryDst);
        
        entrySrc2.setSourceIpRange(ip, ip);
        entryDst2.setDestinationIpRange(ip, ip);
        hostViewer.addEntryDst(entryDst2);
        hostViewer.addEntrySrc(entrySrc2);
        filter.addFilterWhite(entrySrc2);
        filter.addFilterWhite(entryDst2);

        synchronized(ipToHost){
            ipToHost.put(ident, hostViewer);
            hosts.add(hostViewer);
        }
    }

    public void newFlow(Flow flow) {
        if(filter.allow(flow)){
            if(filter.getWhiteList().getCount() == 0){
                // empty list lets everything though but no need to process.
                return;
            }
            
            long time = System.currentTimeMillis();
            int[] types = flowTypes;
            long size = 0;
            boolean hasData = false;
            
            TheHost host;
            ModLong tLong = tmpLong;

            tLong.setLong(flow.getSourceIP());
            host = (TheHost)ipToHost.get(tLong);
            if(host != null){
                hasData = true;
                if(flow instanceof NABFlow) {
                    NABFlow nFlow = (NABFlow)flow;
                    
                    size = nFlow.getSize();
                    Arrays.fill(types, 0);
                    types[nFlow.getType()] = 1;
                } else if(flow instanceof NABFlowV2) {
                    NABFlowV2 nFlow = (NABFlowV2)flow;
                    
                    size = nFlow.getSize();
                    System.arraycopy(nFlow.getTypeCount(), 0, types, 0, types.length);
                }
                
                host.newFlowSource(flow, types, size, time);
            }

            tLong.setLong(flow.getDestinationIP());
            host = (TheHost)ipToHost.get(tLong);
            if(host != null){
                if(!hasData){ // did this above, no need to do it again.
                    if(flow instanceof NABFlow) {
                        NABFlow nFlow = (NABFlow)flow;

                        size = nFlow.getSize();
                        Arrays.fill(types, 0);
                        types[nFlow.getType()] = 1;
                    } else if(flow instanceof NABFlowV2) {
                        NABFlowV2 nFlow = (NABFlowV2)flow;

                        size = nFlow.getSize();
                        System.arraycopy(nFlow.getTypeCount(), 0, types, 0, types.length);
                    }
                }

                host.newFlowDestination(flow, types, size, time);
            }
        }
    }

    public Filter getFilter() {
        return filter;
    }

    public void setFilter(Filter filter) {
    }

    public void getControlData(OutputStream out) throws IOException {
    }

    public FlowProcessor getFlowProcessor() {
        return this;
    }

    public void processMessage(DataInputStream in, DataOutputStream out) throws IOException {
        ObjectInputStream oin = new EunomiaObjectInputStream(in);
        Object o = null;
        try {
            o = oin.readObject();
        } catch (ClassNotFoundException ex){
            ex.printStackTrace();
            return;
        }

        if(o instanceof AddRemoveHostMessage){
            AddRemoveHostMessage arhm = (AddRemoveHostMessage)o;
            if(arhm.isDoAdd()){
                addHost(arhm.getIp());
            } else {
                removeHost(arhm.getIp());
            }
        } else if(o instanceof OpenDetailsMessage){
            OpenDetailsMessage odm = (OpenDetailsMessage)o;
            ReceptorModule details = SieveContext.getModuleManager().getProcessorModule(odm.getHandle());
            if(details != null) {
                details.executeCommand("ah", new Object[]{Long.valueOf(odm.getIp())});
            }
        }
    }

    public void reset() {
    }

    public void setControlData(InputStream in) throws IOException {
    }

    public void start() {
    }

    public void stop() {
    }
    
    public void setProperty(String name, Object value) {
        if(name.equals("ah")){
            long lip = ((Long)value).longValue();
            addHost(lip);
        }
    }
    
    public Object getProperty(String name) {
        return null;
    }

    public void updateStatus(OutputStream out) throws IOException {
        DataOutputStream dout = new DataOutputStream(out);
        
        Object[] hsts = hosts.toArray();
        dout.writeInt(hsts.length);
        for(int i = 0; i < hsts.length; i++){
            TheHost host = (TheHost)hsts[i];
            dout.writeLong(host.getLongIp());
            host.writeOut(dout);
        }
    }

    public void initialize() {
    }

    public void destroy() {
    }

    public Object[] getCommands() {
        return null;
    }

    public Object executeCommand(Object command, Object[] parameters) {
        return null;
    }

    public boolean accept(FlowModule module) {
        return true;
    }
}
