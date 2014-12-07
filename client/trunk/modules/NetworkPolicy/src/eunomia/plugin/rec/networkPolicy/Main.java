/*
 * Main.java
 *
 * Created on December 15, 2006, 11:30 AM
 *
 */

package eunomia.plugin.rec.networkPolicy;

import eunomia.config.Config;
import com.vivic.eunomia.filter.Filter;
import com.vivic.eunomia.module.flow.Flow;
import com.vivic.eunomia.module.receptor.FlowProcessor;
import eunomia.plugin.com.networkPolicy.PolicyItemRemoveMessage;
import eunomia.plugin.utils.networkPolicy.PolicyLanguageParser;
import java.text.ParseException;
import java.util.HashMap;
import com.vivic.eunomia.module.receptor.ReceptorProcessorModule;
import eunomia.receptor.module.NABFlow.NABFilterEntry;
import eunomia.receptor.module.NABFlow.NABFlow;
import com.vivic.eunomia.module.flow.FlowModule;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import eunomia.plugin.utils.networkPolicy.AlertContainer;
import eunomia.plugin.com.networkPolicy.AlertItem;
import eunomia.plugin.com.networkPolicy.AlertListMessage;
import eunomia.plugin.com.networkPolicy.DeleteAlertListMessage;
import eunomia.plugin.utils.networkPolicy.ConcurrentStack;
import eunomia.plugin.com.networkPolicy.PolicyItem;
import eunomia.plugin.com.networkPolicy.PolicyListMessage;
import eunomia.plugin.utils.networkPolicy.FlowId;
import eunomia.util.io.EunomiaObjectInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.ObjectInputStream;
import java.util.Arrays;
import java.util.Map.Entry;

/**
 *
 * @author kulesh, Mikhail Sosonkin
 */
public class Main implements FlowProcessor, ReceptorProcessorModule{
    private static final String configStr = "module.networkPolicy";
    private static final int defaultTimeout = 30*1000;
    private static final int defaultHostTimeout = 1000*60*20; // 20 minutes
    
    private long idleTime = defaultTimeout; //in milli seconds
 
    private PolicyItem[] policies;
    private AlertContainer alerts;
    
    private boolean doProc;
    private long lastCheck;
    private FlowId retriever;
    private Host hostRetriever;
    
    private long policyId = 0;
    private long alertId = 0;

    private ConcurrentStack flowIdFreeStore;
    private Map idToId;
    private Map hostToHost;
    private HostPair hostPair;
    
    public Main() {
        doProc = true;
        hostPair = new HostPair();
        idToId = new HashMap();
        hostToHost = new HashMap();
        policies = new PolicyItem[10];
        alerts = new AlertContainer();
        retriever = new FlowId();
        hostRetriever = new Host(0, 0);

        try {
            setDefaultPolicy();
        } catch (ParseException ex) {
            ex.printStackTrace();
        }
        
        flowIdFreeStore = new ConcurrentStack();
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
        DataOutputStream o = new DataOutputStream(out);
        Iterator it;
        Collection c;
        Map.Entry tmp;

        int pCount = 0;
        for (int i = 0; i < policies.length; i++) {
            if(policies[i] != null) {
                ++pCount;
            }
        }
        
        o.writeInt(pCount);
        for (int i = 0; i < policies.length; i++) {
            if(policies[i] != null) {
                o.writeInt(policies[i].getPolicyID());
            }
        }
        
        o.writeInt(alerts.size());
        if(alerts.size() != 0){
            it = alerts.iterator();
            while (it.hasNext()) {
                tmp = (Map.Entry)it.next();
                AlertItem elm = (AlertItem)tmp.getValue();
                o.writeLong(elm.getAlertID());
                o.writeByte(elm.getChangeCount());
            }
        }
    }
    
    public void setControlData(InputStream in) throws IOException {
    }
    
    public void getControlData(OutputStream out) throws IOException {
    }
    
    public void processMessage(DataInputStream in, DataOutputStream out) throws IOException {
        ObjectInputStream oin = new EunomiaObjectInputStream(in);
        Object o = null;
        
        while(true) {
            try {
                o = oin.readObject();
                processObject(o, out);
            } catch(EOFException e){
                return;
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    /*
     The front-end will send two types of messages to the middleware instance
      of this module: a) PolicyListMessage b) AlertListMessage
      The messages carry Policy[Alert]Ids that are missing in the front-end
     */
    private void processObject(Object o, OutputStream out) throws IOException {
        if(o instanceof AlertListMessage){
            ObjectOutputStream oout = new ObjectOutputStream(out);
            AlertListMessage alm = (AlertListMessage)o;
            AlertItem ai;
            long [] list = alm.getList();
            
            for(int i = 0; i < list.length; ++i){
                ai = alerts.getAlertItem(list[i]);
                oout.writeObject(ai);
            }
        } else if(o instanceof PolicyListMessage){
            //get the list gui sent and send the corresponding PolicyItems
            ObjectOutputStream oout = new ObjectOutputStream(out);
            PolicyListMessage plm = (PolicyListMessage)o;
            PolicyItem pi;
            int list[] = plm.getList();

            for(int i = 0; i < list.length; ++i){
                pi = policies[list[i]];
                oout.writeObject(pi);
            }
        } else if(o instanceof PolicyItem) {
            PolicyItem item = (PolicyItem)o;
            int id = item.getPolicyID();

            if(id > 0 && id < policies.length && policies[id] != null) {
                PolicyItem pol = policies[id];
                pol.setDescription(item.getDescription());
                pol.setFilter(item.getFilter());
                pol.setRate(item.getRate());
                if(item.isRemoveAlerts()) {
                    removePolicyAlerts(item.getPolicyID());
                }
            } else {
                addPolicyItem(item);
            }
            
            savePolicies();
        } else if(o instanceof PolicyItemRemoveMessage) {
            PolicyItemRemoveMessage msg = (PolicyItemRemoveMessage)o;
            removePolicyItem(msg.getId());
            removePolicyAlerts(msg.getId());
            savePolicies();
        } else if(o instanceof AlertItem) {
            AlertItem newAlert = (AlertItem)o;
            AlertItem oldAlert = alerts.getAlertItem(newAlert.getAlertID());
            
            if(oldAlert != null) {
                oldAlert.setNotes(newAlert.getNotes());
                oldAlert.setStatus(newAlert.getStatus());
            }
        } else if(o instanceof DeleteAlertListMessage) {
            long[] list = ((DeleteAlertListMessage)o).getList();
            for (int i = 0; i < list.length; i++) {
                AlertItem item = alerts.getAlertItem(list[i]);
                if(item != null) {
                    alerts.removeAlertItem(item);
                }
            }
        }
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
    }
    
    public Filter getFilter() {
        return null;
    }
    
    public void newFlow(Flow flow) {
        if(!doProc) {
            return;
        }
        
        boolean updatedFlow = false;
        boolean updatedHost = false;
        //long time = flow.getTime();
        long time = System.currentTimeMillis();
        long sIp = flow.getSourceIP();
        long dIp = flow.getDestinationIP();
        int sPort = flow.getSourcePort();
        int dPort = flow.getDestinationPort();
        
        PolicyItem[] pItems = policies;
        FlowId flowId = null;
        HostPair pair = null;
        for (int i = 0; i < pItems.length; i++) {
            PolicyItem pi = pItems[i];
            
            if(pi != null) {
                Filter filter = pi.getFilter();
                
                if(filter.allow(flow)){
                    if(!updatedHost) {
                        NABFlow nflow = (NABFlow)flow;
                        pair = updateHostTable(nflow, time);
                        updatedHost = true;
                    }

                    Host src = pair.getSource();
                    Host dst = pair.getDestination();

                    int policy_type = pi.getPolicyType();
                    int policy_id = pi.getPolicyID();
                    int flow_size = flow.getSize();
                    long policy_rate = pi.getRate();

                    src.accountPolicyData(flow_size, time, policy_id, pi);
                    dst.accountPolicyData(flow_size, time, policy_id, pi);

                    if(src.getPolicyData(policy_id) > policy_rate) {
                        alerts.putAlertItem(makeHostAlert(src, pi));
                        src.resetPolicyData(policy_id, time);
                    }

                    if(dst.getPolicyData(policy_id) > policy_rate) {
                        alerts.putAlertItem(makeHostAlert(dst, pi));
                        dst.resetPolicyData(policy_id, time);
                    }
                }
            }
        }
        
        
        if(time - lastCheck > 2000) { //check every 2s: restart rate count and remove idles
            lastCheck = time;
            pruneIdle(time, idleTime, defaultHostTimeout);
        }
    }
    
    private AlertItem makeHostAlert(Host h, PolicyItem pi) {
        FlowId id = new FlowId(h.getIP(), 0, 0, 0);
        id.setStartTime(h.getPolicyDataLastReset(pi.getPolicyID()));
        id.setLastUpdate(h.getLastUpdateTime());
        id.setBytes(h.getLifeTotal(), h.getLifeContent());
        
        return makeAlert(id, pi);
    }
    
    private AlertItem makeAlert(FlowId id, PolicyItem pi) {
        AlertItem alert = alerts.getAlertItem(id);
        if(alert == null) {
            alert = new AlertItem();
            alert.setPolicyID(pi.getPolicyID());
            alert.setAlertID(alertId++);
            alert.setFlowId((FlowId)id.clone());
            alert.setFirstSeen(id.getStartTime());
        }
        
        alert.incrementViolations();
        alert.setLastSeen(id.getLastUpdate());
        
        FlowId alertId = alert.getFlowId();
        alertId.setBytes(id.getBytes(), id.getByteTypes());
        
        return alert;
    }
    
    private HostPair updateHostTable(NABFlow flow, long time){
        hostPair.setSource(getHost(flow.getSourceIP(), time));
        hostPair.setDestination(getHost(flow.getDestinationIP(), time));
        hostPair.accountGlobalData(flow.getType(), flow.getSize(), time);
        
        return hostPair;
    }
    
    private Host getHost(long ip, long time) {
        hostRetriever.setIp(ip);
        Object host = hostToHost.get(hostRetriever);
        
        if(host == null){
            Host h = new Host(ip, time);
            hostToHost.put(h, h);
            
            return h;
        }
        
        return (Host)host;
    }
    
    private FlowId updateFlowTable(NABFlow flow, long time){
        FlowId flowId = getFlowId(flow.getSourceIP(), flow.getDestinationIP(), flow.getSourcePort(), flow.getDestinationPort());
        
        if(flowId.getLastUpdate() == -1) {
            flowId.setStartTime(time);
        }

        flowId.incrementBytes(flow.getSize(), flow.getType());
        flowId.setLastUpdate(time);
        
        return flowId;
    }
    
    private FlowId getFlowId(long sip, long dip, int sp, int dp) {
        retriever.setAll(sip, dip, sp, dp);
        
        FlowId id = (FlowId)idToId.get(retriever);
        if(id == null) {
            id = (FlowId)flowIdFreeStore.pop();
            if(id == null) {
                id = new FlowId(sip, dip, sp, dp);
            } else {
                id.setAll(sip, dip, sp, dp);
            }
            
            idToId.put(id, id);
        }
        
        return id;
    }

    private void pruneIdle(long time, long idle, long hostIdle) {
        Collection entries = idToId.entrySet();
        Iterator it = entries.iterator();
        
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry) it.next();
            FlowId id = (FlowId)entry.getValue();
            
            if((time - id.getLastUpdate()) > idle) {
                it.remove();
                
                if(flowIdFreeStore.getSize() < 1024) {
                    flowIdFreeStore.push(id);
                }
            } else {
                id.resetBytes();
                id.setStartTime(time);
            }
        }
        
        entries = hostToHost.entrySet();
        it = entries.iterator();
        
        while(it.hasNext()) {
            Map.Entry entry = (Map.Entry) it.next();
            Host host = (Host)entry.getValue();
            
            long diff = time - host.getLastUpdateTime();
            if(diff > hostIdle) {
                it.remove();
            }
        }
    }
    
    public boolean accept(FlowModule module) {
        return module.getNewFlowInstance() instanceof NABFlow;
    }
    
    public void addPolicyItem(PolicyItem pi) {
        synchronized(policies) { // doesn't affect flow processing thread.
            int i;
            for (i = 0; i < policies.length; i++) {
                if(policies[i] == null) {
                    pi.setPolicyID(i);
                    policies[i] = pi;
                    return;
                }
            }
            
            // didn't insert
            PolicyItem[] tmp = new PolicyItem[policies.length * 2];
            System.arraycopy(policies, 0, tmp, 0, policies.length);
            
            pi.setPolicyID(i);
            tmp[i] = pi;
            
            policies = tmp;
        }
    }
    
    private void removePolicyAlerts(long id) {
        Iterator it = alerts.iterator();
        while (it.hasNext()) {
            Entry item = (Entry) it.next();
            if( ((AlertItem)item.getValue()).getPolicyID() == id) {
                it.remove();
            }
        }
    }
    
    private void removePolicyItem(long i) {
        policies[(int)i] = null;
    }
    
    private void savePolicies() {
        Config conf = Config.getConfiguration(configStr);

        try {
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            ObjectOutputStream oout = new ObjectOutputStream(bout);

            PolicyItem[] pol = policies;
            int k = 0;
            for (int i = 0; i < pol.length; i++) {
                PolicyItem p = pol[i];
                if(p != null) {
                    ++k;
                    oout.writeObject(p);
                }
            }
            
            oout.close();

            byte[] bytes = bout.toByteArray();
            conf.setBytes("policies", bytes);
            conf.setInt("count", k);
            
            conf.save();
        } catch (Exception e){
            e.printStackTrace();
        }
    }
    
    private boolean loadPolicies() {
        Config conf = Config.getConfiguration(configStr);
        
        byte[] bytes = conf.getBytes("policies", null);
        
        if(bytes != null) {
            try {
                ByteArrayInputStream bin = new ByteArrayInputStream(bytes);
                ObjectInputStream oin = new EunomiaObjectInputStream(bin);
                
                int count = conf.getInt("count", 0);
                for (int i = 0; i < count; i++) {
                    PolicyItem p = (PolicyItem)oin.readObject();
                    addPolicyItem(p);
                }
                
                oin.close();

                return true;
            } catch (Exception e){
                //e.printStackTrace();
                return false;
            }
        }
        
        return false;
    }
    
    private void setDefaultPolicy() throws ParseException {
        if(loadPolicies()) {
            return;
        }
        
        boolean[] allowSet = new boolean[NABFlow.NUM_TYPES];
        PolicyItem p;
        NABFilterEntry f;
        
        //second default policy to forbid encrypted streams
        p = new PolicyItem(PolicyItem.REAL_TIME, "Plaintext traffic from secure ports, over 20KB/s");
        Arrays.fill(allowSet, false);
        allowSet[NABFlow.DT_Plain_Text] = true;
        
        p.setRate(20*1024);
        p.setTimeout(defaultTimeout);
        p.setFilter(PolicyLanguageParser.parseBasicFilter(null, "", "443, 995", allowSet));
                
        addPolicyItem(p);
        
        //
        p = new PolicyItem(PolicyItem.REAL_TIME, "IRC Session of 200KB/s or higher");
        f = new NABFilterEntry();
        
        for(int i = 0; i < NABFlow.NUM_TYPES; f.setAllowed(i++, false));
        f.setAllowed(NABFlow.DT_Plain_Text, true);

        f.setSourceIpRange(0x0L, 0xFFFFFFFFL);
        f.setDestinationIpRange(0x0L, 0xFFFFFFFFL);
        f.setSourcePortRange(0, 0xFFFF);
        f.setDestinationPortRange(6667, 6667);
        p.getFilter().addFilterWhite(f);
        p.setRate(200*1024);
        p.setTimeout(defaultTimeout);
        
        addPolicyItem(p);
        
        //
        p = new PolicyItem(PolicyItem.REAL_TIME, "Port 80 has more than 200KB/s of encrypted traffic");
        f = new NABFilterEntry();
        
        for(int i = 0; i < NABFlow.NUM_TYPES; f.setAllowed(i++, false));
        f.setAllowed(NABFlow.DT_Encrypted, true);

        f.setSourceIpRange(0x0L, 0xFFFFFFFFL);
        f.setDestinationIpRange(0x0L, 0xFFFFFFFFL);
        f.setSourcePortRange(80, 80);
        f.setDestinationPortRange(0, 0xFFFF);
        p.getFilter().addFilterWhite(f);
        p.setRate(200*1024);
        p.setTimeout(defaultTimeout);
        
        addPolicyItem(p);
        
        //Hourly 150MB of MP3 Limit
        p = new PolicyItem(PolicyItem.HOURLY, "Hourly limit of 150MB of music per host (includes both upload and download)");
        f = new NABFilterEntry();
        
        for(int i = 0; i < NABFlow.NUM_TYPES; f.setAllowed(i++, false));
        f.setAllowed(NABFlow.DT_Audio_MP3, true);
        
        f.setSourceIpRange(0x0L, 0xFFFFFFFFL);
        f.setDestinationIpRange(0x0L, 0xFFFFFFFFL);
        f.setSourcePortRange(0, 0xFFFF);
        f.setDestinationPortRange(0, 0xFFFF);
        p.getFilter().addFilterWhite(f);
        
        p.setRate(150*1024*1024);
        p.setTimeout(defaultTimeout);
        
        addPolicyItem(p);
        
        // 30MB of media per hour
        p = new PolicyItem(PolicyItem.HOURLY, "Hourly Media");
        f = new NABFilterEntry();
        
        //Disallow all typesw
        for(int i = 0; i < NABFlow.NUM_TYPES; f.setAllowed(i++, false));
        
        f.setAllowed(NABFlow.DT_Audio_MP3, true);
        f.setAllowed(NABFlow.DT_Audio_WAV, true);
        f.setAllowed(NABFlow.DT_Video_MPG, true);
        
        f.setSourceIpRange(0x0L, 0xFFFFFFFFL);
        f.setDestinationIpRange(0x0L, 0xFFFFFFFFL);
        f.setSourcePortRange(0, 0xFFFF);
        f.setDestinationPortRange(0, 0xFFFF);
        p.getFilter().addFilterWhite(f);
        p.setRate(100*1024*1024);
        p.setTimeout(defaultTimeout);
        
        addPolicyItem(p);
        
        // 300MB of media per day from http
        p = new PolicyItem(PolicyItem.DAILY, "Daily limit on internet Video/Music content");
        f = new NABFilterEntry();
        
        for(int i = 0; i < NABFlow.NUM_TYPES; f.setAllowed(i++, false));
        f.setAllowed(NABFlow.DT_Audio_MP3, true);
        f.setAllowed(NABFlow.DT_Audio_WAV, true);
        f.setAllowed(NABFlow.DT_Video_MPG, true);
        
        f.setSourceIpRange(0x0L, 0xFFFFFFFFL);
        f.setDestinationIpRange(0x0L, 0xFFFFFFFFL);
        f.setSourcePortRange(80, 80);
        f.setDestinationPortRange(0, 0xFFFF);
        p.getFilter().addFilterWhite(f);
        p.setRate(300*1024*1024);
        p.setTimeout(defaultTimeout);
        
        addPolicyItem(p);
        
        savePolicies();
    }
}