/*
 * Main.java
 *
 * Created on April 17, 2006, 8:32 PM
 */

package eunomia.plugin.rec.hostDetails;

import eunomia.messages.module.ModuleMessage;
import eunomia.plugin.interfaces.ReceptorModule;
import eunomia.flow.Filter;
import eunomia.receptor.module.NABFlow.NABFilterEntry;
import eunomia.receptor.module.NABFlow.NABFlow;
import eunomia.receptor.module.interfaces.FlowModule;
import eunomia.util.number.ModLong;
import eunomia.messages.Message;
import java.io.*;
import java.util.*;
import eunomia.flow.*;
import eunomia.messages.module.msg.GenericModuleMessage;
import eunomia.plugin.msg.hostDetails.AddRemoveHostMessage;
import eunomia.plugin.msg.hostDetails.HostListMessage;

/**
 *
 * @author Mikhail Sosonkin
 */
public class Main implements ReceptorModule, FlowProcessor {
    private List hosts;
    private Filter filter;
    private HashMap ipToHost;
    private ModLong tmpLong;
    
    public Main() {
        hosts = new LinkedList();
        filter = new Filter();
        ipToHost = new HashMap();
        tmpLong = new ModLong();
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
        
        filter.removeFilterWhite(hostViewer.getEntryDst());
        filter.removeFilterWhite(hostViewer.getEntrySrc());
        hosts.remove(hostViewer);
    }
    
    public void addHost(long ip){
        Long ident;

        ident = new Long(ip);
        
        if(ipToHost.containsKey(ident)){
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
        
        synchronized(ipToHost){
            ipToHost.put(ident, hostViewer);
            hosts.add(hostViewer);
        }
    }

    public void initialize() {
    }

    public void destroy() {
    }

    public FlowProcessor getFlowProcessor() {
        return this;
    }

    public void updateStatus(OutputStream out) throws IOException {
        DataOutputStream dout = new DataOutputStream(out);
        
        Object[] hsts = hosts.toArray();
        for(int i = 0; i < hsts.length; i++){
            TheHost host = (TheHost)hsts[i];
            dout.writeLong(host.getLongIp());
        }
    }

    public void setControlData(InputStream in) throws IOException {
    }

    public void getControlData(OutputStream out) throws IOException {
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
            
            if(o instanceof HostListMessage){
                HostListMessage hlm = (HostListMessage)o;
                GenericModuleMessage resp = new GenericModuleMessage();
                DataOutputStream dout = new DataOutputStream(resp.getOutputStream());
                ModLong findIp = new ModLong();
                
                dout.write(0xFE);
                dout.write(0xED);
                
                long[] list = hlm.getList();
                for (int i = 0; i < list.length; i++) {
                    TheHost host = null;
                    findIp.setLong(list[i]);
                    synchronized(ipToHost){
                        host = (TheHost)ipToHost.get(findIp);
                    }
                    if(host != null){
                        dout.writeLong(list[i]);
                        host.writeOut(dout);
                    }
                }
                return resp;
            } else if(o instanceof AddRemoveHostMessage){
                AddRemoveHostMessage arhm = (AddRemoveHostMessage)o;
                if(arhm.isDoAdd()){
                    addHost(arhm.getIp());
                } else {
                    removeHost(arhm.getIp());
                }
            }
        }
        return null;
    }

    public void start() {
    }

    public void stop() {
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
        String str = command.toString();
        if(str.equals("ah")){
            addHost(((Long)parameters[0]).longValue());
        }
        return null;
    }

    public void setFilter(Filter filter) {
    }

    public Filter getFilter() {
        return null;
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
                if(flow instanceof NABFlow) {
                    host.newFlowSource((NABFlow)flow);
                }
            }

            tLong.setLong(flow.getDestinationIP());
            host = (TheHost)ipToHost.get(tLong);
            if(host != null){
                if(flow instanceof NABFlow) {
                    host.newFlowDestination((NABFlow)flow);
                }
            }
        }
    }

    public boolean accept(FlowModule module) {
        return true;
    }
    
}
