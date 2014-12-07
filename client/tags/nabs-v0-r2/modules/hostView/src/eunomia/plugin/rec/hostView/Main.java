/*
 * HostView.java
 *
 * Created on August 9, 2005, 4:20 PM
 *
 */

package eunomia.plugin.rec.hostView;

import eunomia.flow.*;
import eunomia.managers.ModuleManager;
import eunomia.messages.Message;
import eunomia.messages.module.ModuleMessage;
import eunomia.messages.module.msg.GenericModuleMessage;
import eunomia.plugin.interfaces.*;
import eunomia.plugin.msg.AddRemoveHostMessage;
import eunomia.plugin.msg.OpenDetailsMessage;
import eunomia.flow.Filter;
import eunomia.receptor.module.NABFlow.NABFilterEntry;
import eunomia.receptor.module.NABFlow.NABFlow;
import eunomia.receptor.module.interfaces.FlowModule;
import eunomia.util.number.*;

import java.util.*;
import java.io.*;

import org.apache.log4j.*;


/**
 *
 * @author Mikhail Sosonkin
 */
public class Main extends TimerTask implements ReceptorModule, FlowProcessor, Runnable {
    private List hosts;
    private Filter filter;
    private HashMap ipToHost;
    private ModLong tmpLong;
    
    private static Logger logger;
    
    static {
        logger = Logger.getLogger(Main.class);
    }
    
    public Main() {
        hosts = new Vector();
        filter = new Filter();
        ipToHost = new HashMap();
        tmpLong = new ModLong();
        
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
        
        TheHost hostViewer = (TheHost)ipToHost.remove(ident);
                
        if(hostViewer == null){
            return;
        }
        
        filter.removeFilterWhite(hostViewer.getEntryDst());
        filter.removeFilterWhite(hostViewer.getEntrySrc());
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
        
        NABFilterEntry entrySrc = new NABFilterEntry();
        NABFilterEntry entryDst = new NABFilterEntry();
        entrySrc.setSourceIpRange(ip, ip);
        entryDst.setDestinationIpRange(ip, ip);
        hostViewer.setEntryDst(entryDst);
        hostViewer.setEntrySrc(entrySrc);
        filter.addFilterWhite(entrySrc);
        filter.addFilterWhite(entryDst);
        
        ipToHost.put(ident, hostViewer);
        hosts.add(hostViewer);
    }

    public void newFlow(Flow flow) {
        if(filter.allow(flow)){
            if(filter.getWhiteList().getCount() == 0){
                // empty list lets everything though but no need to process.
                return;
            }
            
            TheHost host;
            ModLong tLong = tmpLong;

            tLong.setLong(flow.getSourceIP());
            host = (TheHost)ipToHost.get(tLong);
            if(host != null){
                if(flow instanceof NABFlow){
                    host.newFlowSource((NABFlow)flow);
                }
            }

            tLong.setLong(flow.getDestinationIP());
            host = (TheHost)ipToHost.get(tLong);
            if(host != null){
                if(flow instanceof NABFlow){
                    host.newFlowDestination((NABFlow)flow);
                }
            }
        }
    }

    public Filter getFilter() {
        return null;
    }

    public void setFilter(Filter filter) {
    }

    public void getControlData(OutputStream out) throws IOException {
    }

    public FlowProcessor getFlowProcessor() {
        return this;
    }

    public Message processMessage(ModuleMessage msg) throws IOException {
        if(msg instanceof GenericModuleMessage){
            GenericModuleMessage gmm = (GenericModuleMessage)msg;
            ObjectInputStream oin = new ObjectInputStream(gmm.getInputStream());
            Object o = null;
            try {
                o = oin.readObject();
            } catch (ClassNotFoundException ex){
                ex.printStackTrace();
                return null;
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
                ReceptorModule details = ModuleManager.v().getModule(odm.getHandle());
                if(details != null){
                    details.executeCommand("ah", new Object[]{Long.valueOf(odm.getIp())});
                }
            }
        }
        
        return null;
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
