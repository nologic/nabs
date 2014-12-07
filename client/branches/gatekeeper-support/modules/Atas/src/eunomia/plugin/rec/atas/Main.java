/*
 * Main.java
 *
 * Created on March 1, 2007, 8:18 PM
 *
 */

package eunomia.plugin.rec.atas;

import eunomia.flow.Filter;
import com.vivic.eunomia.module.Flow;
import com.vivic.eunomia.module.receptor.FlowProcessor;
import eunomia.messages.Message;
import eunomia.plugin.com.atas.ClassifierConfigurationMessage;
import eunomia.plugin.com.atas.HostInfo;
import eunomia.plugin.com.atas.RoleInterface;
import eunomia.plugin.com.atas.RoleUpdateMessage;
import com.vivic.eunomia.module.receptor.ReceptorModule;
import eunomia.receptor.module.NABFlowV2.NABFlowV2;
import com.vivic.eunomia.module.receptor.FlowModule;
import eunomia.util.io.EunomiaObjectInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Kulesh Shanmugasundaram
 */
public class Main implements FlowProcessor, ReceptorModule {
    private Filter filter;
    private boolean doProc;
    private NABFlowV2 nf;

    private RoleClassifier rc;
    private int activeRoles;
    
    
    public Main() {
        doProc = true;
        filter = new Filter();
        rc= new RoleClassifier();
    }

    public void setFilter(Filter filter) {
        this.filter = filter;
    }

    public Filter getFilter() {
        return filter;
    }

    public void newFlow(Flow flow) {
        if(!(flow instanceof NABFlowV2 && filter.allow(flow) && doProc))
            return;
  
        nf= (NABFlowV2)flow;
        rc.identifyRole(nf);
    }

    public boolean accept(FlowModule module) {
        return module.getNewFlowInstance() instanceof NABFlowV2;
    }

    public void destroy() {
    }

    public FlowProcessor getFlowProcessor() {
        return this;
    }

    //periodically Sieve sends the following information to the console:
    //0) number of active roles it has (int)
    //1) for each active role: 
    //2)    send the role number (int)
    //3)    send number of hosts in it (int)
    //4)    send (ip, lastseen) pair (long, long)*(number of hosts)
    public void updateStatus(OutputStream out) throws IOException {
        DataOutputStream o = new DataOutputStream(out);
        Iterator it;
        Collection c;
        Map.Entry tmp;
        int activeRoles= rc.getActiveRoles();
        int roles;

        //Send the number of activeRoles
        o.writeInt(activeRoles);
        
        if(activeRoles == 0) 
            return;
        
        int n = 0;
        int hostCount = 0;
        int count = rc.getRoleClassifierCount();
        
        RoleInterface ri = null;
        
        for(int i=0; ((i < count) && (n < activeRoles)); ++i){
            ri = rc.getRoleClassifierByIndex(i);
            hostCount = ri.hostCount();
            
            //anything to send?
            if(hostCount == 0) 
                continue;
            ++n;

            //Send the role number
            o.writeInt(ri.getRoleNumber());
            
            //Send # of (IPs/TS) pair
            o.writeInt(hostCount);
            
            //Send that many pairs only
            c= ri.getCollection();
            it= c.iterator();
            int j=0;
            while((it.hasNext()) && (j < hostCount)){
                tmp= (Map.Entry)it.next();
                HostInfo hi= (HostInfo)tmp.getValue();
                o.writeLong(hi.getIp());
                o.writeLong(hi.getLastSeen());
                ++j;
            }
        }
    }

    public void setControlData(InputStream in) throws IOException {
        ObjectInputStream oin = new EunomiaObjectInputStream(in);
        
        int count = oin.readInt();
        for (int i = 0; i < count; i++) {
            try {
                ClassifierConfigurationMessage msg = (ClassifierConfigurationMessage)oin.readObject();
                rc.getRoleClassifierByIndex(msg.getRoleNumber()).setConfigurationMessage(msg);
            } catch (ClassNotFoundException ex) {
                ex.printStackTrace();
            }
        }
    }

    public void getControlData(OutputStream out) throws IOException {
        ObjectOutputStream oout = new ObjectOutputStream(out);
        
        List list = rc.getConfigurationList();
        oout.writeInt(list.size());
        
        Iterator it = list.iterator();
        while (it.hasNext()) {
            Message msg = (Message) it.next();
            oout.writeObject(msg);
        }
    }

    //Periodically the GUI sends RoleUpdateMessages for those roles that need updates
    //Upon recieving a RoleUpdateMessage Sieve will send the following information to the GUI:
    //0) RoleType Number (int)
    //1) Number of HostInfos for this role (int)
    //2) The HostInfos themselves.
    public void processMessage(DataInputStream in, DataOutputStream out) throws IOException {
        ObjectInputStream oin = new EunomiaObjectInputStream(in);
        Object o = null;
        try {
            o = oin.readObject();
        } catch (ClassNotFoundException ex){
            ex.printStackTrace();
            return;
        }
        
        if(o instanceof RoleUpdateMessage){
            RoleUpdateMessage ruh = (RoleUpdateMessage)o;
            ObjectOutputStream oo= new ObjectOutputStream(out);
            sendRoleUpdates(ruh, oo);
        }
    }

    private void sendRoleUpdates(RoleUpdateMessage ruh, ObjectOutputStream oo) throws IOException{
        long[] list = ruh.getList();
        int length = ruh.getLength();

        RoleInterface ri = rc.getRoleClassifierByRoleNumber(ruh.getRoleNumber());
        
        oo.writeInt(ruh.getRoleNumber()); //Send role number
        oo.writeObject(ri.getRoleName());
        oo.writeInt(length); //send the number of HostInfos
        HostInfo tmp;
        for (int i = 0; i < length; ++i) { //Send the HostInfos
            tmp = (HostInfo)ri.getHostInfo(list[i]);
            oo.writeObject(tmp);
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
    
}
