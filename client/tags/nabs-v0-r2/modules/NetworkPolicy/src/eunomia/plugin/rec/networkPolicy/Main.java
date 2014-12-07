/*
 * Main.java
 *
 * Created on December 15, 2006, 11:30 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package eunomia.plugin.rec.networkPolicy;

import eunomia.flow.Filter;
import eunomia.flow.Flow;
import eunomia.flow.FlowProcessor;
import eunomia.messages.Message;
import eunomia.messages.module.ModuleMessage;
import eunomia.messages.module.msg.GenericModuleMessage;
import eunomia.plugin.com.networkPolicy.AlertContainer;
import eunomia.plugin.com.networkPolicy.AlertItem;
import eunomia.plugin.com.networkPolicy.AlertListMessage;
import eunomia.plugin.com.networkPolicy.PolicyItem;
import eunomia.plugin.com.networkPolicy.PolicyListMessage;
import eunomia.plugin.interfaces.ReceptorModule;
import eunomia.plugin.networkPolicy.utils.FlowId;
import eunomia.plugin.networkPolicy.utils.FlowStat;
import eunomia.receptor.module.NABFlow.NABFilterEntry;
import eunomia.receptor.module.NABFlow.NABFlow;
import eunomia.receptor.module.interfaces.FlowModule;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.LinkedList;


/**
 *
 * @author kulesh
 */
public class Main implements FlowProcessor, ReceptorModule{
    
    //monotonically increasing IDs across the board
    private static long policyId=0;
    private static long alertId= 0;
    //default flow-rate and timeout values
    private static final int defaultRate= 19600;
    private static final int defaultTimeout= 30000;
    
    private int rate= defaultRate;       //in Kbps
    private long idleTime = defaultTimeout; //in milli seconds
    private LinkedHashMap policy;
    private AlertContainer alerts;
    
    private LinkedHashMap<FlowId, FlowStat> flowTable;
    private boolean doProc;
    private long lastReset;
    private FlowId flowId;
    private FlowStat flowStats;
    private LinkedList<FlowStat> flowStatFreeStore;
    private LinkedList<FlowId> flowIdFreeStore;
    private LinkedList<AlertItem> alertItemFreeStore;
    private long total;
    
    /** Creates a new instance of Main */
    public Main() {
        doProc= true;
        total=0;
        
        policy= new LinkedHashMap();
        alerts= new AlertContainer();
        setDefaultPolicy(policy);
        lastReset= System.currentTimeMillis();
        
        flowTable= new LinkedHashMap<FlowId, FlowStat>();
        flowStatFreeStore= new LinkedList<FlowStat>();
        flowIdFreeStore= new LinkedList<FlowId>();
        alertItemFreeStore= new LinkedList<AlertItem>();
    }
    
    public void initialize() {
    }
    
    public void destroy() {
    }
    
    public FlowProcessor getFlowProcessor() {
        return this;
    }
    
    //Send the gui component the following information:
    //a) number of policy-items in store followed by a list of policyIds
    //b) number of alerts in store followed by a list of alertIds
    public void updateStatus(OutputStream out) throws IOException {
        DataOutputStream o= new DataOutputStream(out);
        Iterator it;
        Collection c;
        Map.Entry tmp;
        
        System.err.println("[" + System.currentTimeMillis() + " ] Middleware sending " + policy.size() + " policyIds.");
        o.writeInt(policy.size());
        if(policy.size() != 0){
            c= policy.entrySet();
            it= c.iterator();
            while (it.hasNext()) {
                tmp= (Map.Entry)it.next();
                PolicyItem elm= (PolicyItem)tmp.getValue();
                o.writeLong(elm.getPolicyID());
            }
        }
       
        /*
        System.err.println("[" + System.currentTimeMillis() + " ] Middleware sending " +alerts.getSize() + " alertIds.");
        o.writeInt(alerts.getSize());
        if(alerts.getSize() != 0){
            it= alerts.iterator();
            while (it.hasNext()) {
                tmp= (Map.Entry)it.next();
                AlertItem elm = (AlertItem)tmp.getValue();
                o.writeLong(elm.getAlertID());
            }
        }
        System.err.println("[" + System.currentTimeMillis() + " ] Middleware done sending policy, alert IDs");
         */
    }
    
    public void setControlData(InputStream in) throws IOException {
        DataInputStream din= new DataInputStream(in);
        
        rate= din.readInt();
        idleTime= (din.readLong()*1000); //milliseconds
    }
    
    public void getControlData(OutputStream out) throws IOException {
        DataOutputStream dout= new DataOutputStream(out);
        
        dout.writeInt(rate);
        dout.writeLong((idleTime/1000)); //GUI is in seconds
    }
    
    //The front-end will send two types of messages to the middleware instance
    //of this module: a) PolicyListMessage b) AlertListMessage
    //The messages carry Policy[Alert]Ids that are missing in the front-end
    public Message processMessage(ModuleMessage msg) throws IOException {
        if(!(msg instanceof GenericModuleMessage))
            return null;
        
        GenericModuleMessage gmm= (GenericModuleMessage)msg;
        ObjectInputStream oin= new ObjectInputStream(gmm.getInputStream());
        Object o= null;
        
        try{
            o= oin.readObject();
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
        
        if(o instanceof AlertListMessage){
            System.err.println("[" + System.currentTimeMillis() + " ] Middleware recieved AlertListMessage");
            //get the list gui sent and send the corresponding AlertItems
            GenericModuleMessage resp= new GenericModuleMessage();
            ObjectOutputStream oout= new ObjectOutputStream(resp.getOutputStream());
            AlertListMessage alm= (AlertListMessage)o;
            AlertItem ai;
            long [] list= alm.getList();

            System.err.println("[" + System.currentTimeMillis() + " ] Middleware sending " + list.length + " AlertItems");
            for(int i=0; i < list.length; ++i){
                ai= alerts.getAlertItem(list[i]);
                oout.writeObject(ai);
            }
            System.err.println("[" + System.currentTimeMillis() + " ] Middleware done sending " + list.length + " AlertItems");
            return resp;
        }else if(o instanceof PolicyListMessage){
            System.err.println("[" + System.currentTimeMillis() + " ] Middleware recieved PolicyListMessage");
            //get the list gui sent and send the corresponding PolicyItems
            GenericModuleMessage resp= new GenericModuleMessage();
            ObjectOutputStream oout= new ObjectOutputStream(resp.getOutputStream());
            PolicyListMessage plm= (PolicyListMessage)o;
            PolicyItem pi;
            long list[]= plm.getList();
            System.err.println("[" + System.currentTimeMillis() + " ] Middleware sending " + list.length + " PolicyItems");
            for(int i=0; i < list.length; ++i){
                pi= (PolicyItem)policy.get(list[i]);
                oout.writeObject(pi);
            }
            System.err.println("[" + System.currentTimeMillis() + " ] Middleware done sending " + list.length + " PolicyItems");
            return resp;
        }else{
            System.err.println("Middleware recieved crappy message!");
        }
        return null;
    }
    
    public void start() {
        doProc= true;
    }
    
    public void stop() {
        doProc=false;
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
    }
    
    public Filter getFilter() {
        return null;
    }
    
    public void newFlow(Flow flow) {
        if((!doProc) || (!(flow instanceof NABFlow)))
            return;
        
        ++total;
        updateFlowTable(flow);
        
        if((total % 100000) == 0)
            checkForViolations();
        
        if((total % 250000) == 0)
            generateAlerts();
    }
    
    private void updateFlowTable(Flow flow){
        if(flowIdFreeStore.size() != 0){
            flowId= (FlowId)flowIdFreeStore.removeFirst();
        }else{
            flowId= new FlowId();
        }
        
        flowId.initializeFlowId(flow);
        
        if((flowStats= (FlowStat)flowTable.get(flowId)) == null){
            //when things are available in freeStore use them instead
            if(flowStatFreeStore.size() != 0){
                flowStats= flowStatFreeStore.removeFirst();
            }else{
                flowStats= new FlowStat();
            }
            flowStats.setStartTime(System.currentTimeMillis());
            flowStats.setLastUpdate(flowStats.getStartTime());
            flowStats.setBytes(((NABFlow)flow).getSize());
            flowTable.put(flowId, flowStats);
        }else{
            flowStats.incrementBytes(((NABFlow)flow).getSize());
            flowStats.setLastUpdate(System.currentTimeMillis());
            flowIdFreeStore.addFirst(flowId); //dont need the flowID put it back
        }
    }
    
    private void checkForViolations(){
        long currentTime= System.currentTimeMillis();
        long diffTime;
        AlertItem a=null;
        Map.Entry<FlowId, FlowStat> tmp;
        Collection entries= flowTable.entrySet();
        Iterator it= entries.iterator();
        
        
        while (it.hasNext()) {
            tmp = (Map.Entry<FlowId, FlowStat>) it.next();
            flowStats= tmp.getValue();
            flowId = tmp.getKey();
            diffTime = ((currentTime - flowStats.getStartTime())/1000);
            
            if((diffTime!=0) && ((flowStats.getBytes()/diffTime)*8/1024) > rate){
                //already produced an alert for this flow?
                if((a=alerts.getAlertItem(flowId)) == null){
                    if(alertItemFreeStore.size()!=0){
                        a= alertItemFreeStore.removeFirst();
                    }else{
                        a= new AlertItem();
                    }
                    
                    a.setAlertID(++alertId);
                    a.setFlowId(flowId);
                    a.incrementViolations();
                    a.setFirstSeen(flowStats.getStartTime());
                    a.setLastSeen(flowStats.getStartTime());
                    alerts.putAlertItem(a);
                    
                    it.remove();
                    flowStatFreeStore.addFirst(flowStats);
                }else{
                    a.incrementViolations();
                    a.setLastSeen(flowStats.getStartTime());
                    
                    it.remove();
                    flowStatFreeStore.addFirst(flowStats);
                    flowIdFreeStore.addFirst(flowId);
                }
            }else if((currentTime - flowStats.getLastUpdate()) > idleTime){
                it.remove();
                flowStatFreeStore.addFirst(flowStats);
                flowIdFreeStore.addFirst(flowId);
            }
        }
    }
    
    private void generateAlerts(){
        AlertItem ai;
        Iterator it= alerts.iterator();
        
        while(it.hasNext()){
            Map.Entry tmp= (Map.Entry)it.next();
            ai= (AlertItem)tmp.getValue();
            //System.out.println(ai);
            
            it.remove();
            flowIdFreeStore.addFirst(ai.getFlowId());
            alertItemFreeStore.addFirst(ai);
        }
    }
    
    public boolean accept(FlowModule module) {
        return true;
    }
    
    public void setDefaultPolicy(LinkedHashMap lhm){
        PolicyItem p= new PolicyItem();
        NABFilterEntry f= new NABFilterEntry();
        
        //first default policy to forbid MP3 streams
        p.setPolicyID(++policyId);
        p.setDescription("Do not allow MP3 streams above 196Kbps.");
        //Disallow all types
        for(int i=0; i < NABFlow.NUM_TYPES; f.setAllowed(i++, false));
        f.setAllowed(NABFlow.DT_Audio_MP3, true);//allow MP3
        p.setFilter(f);
        p.setRate(defaultRate);
        p.setTimeout(defaultTimeout);
        lhm.put(policyId, p);
        
        //second default policy to forbid encrypted streams
        p= new PolicyItem();
        f= new NABFilterEntry();
        p.setPolicyID(++policyId);
        p.setDescription("Do not allow encrypted streams above 196Kbps.");
        //Disallow all types
        for(int i=0; i < NABFlow.NUM_TYPES; ++i, f.setAllowed(i++, false));
        f.setAllowed(NABFlow.DT_Encrypted, true);//allow encrypted
        p.setFilter(f);
        p.setRate(defaultRate);
        p.setTimeout(defaultTimeout);
        lhm.put(policyId, p);
    }
}