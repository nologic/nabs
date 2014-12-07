/*
 * Main.java
 *
 * Created on March 1, 2007, 8:18 PM
 *
 */

package eunomia.plugin.gui.atas;

import eunomia.messages.Message;
import eunomia.plugin.com.atas.ClassifierConfigurationMessage;
import eunomia.plugin.com.atas.HostInfo;
import eunomia.plugin.com.atas.RoleChangeListener;
import eunomia.plugin.com.atas.RoleUpdateMessage;
import com.vivic.eunomia.module.frontend.GUIModule;
import com.vivic.eunomia.sys.frontend.ConsoleReceptor;
import eunomia.util.io.EunomiaObjectInputStream;
import java.awt.Color;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.JComponent;

/**
 *
 * @author Kulesh Shanmugasundaram
 */
public class Main implements GUIModule, RoleChangeListener{
    private ConsoleReceptor receptor;
    private AtasMainPanel mainPanel;
    private AssociationMap associationMap;
    private RoleConfigurationPanel rcPanel;
    
    public Main() {
        mainPanel = new AtasMainPanel();
        rcPanel = new RoleConfigurationPanel();
        
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setOpaque(true);
        mainPanel.setRoleChangeListener(this);
        associationMap= new AssociationMap(mainPanel);
    }
    
    public JComponent getJComponent() {
        return mainPanel;
    }
    
    public JComponent getControlComponent() {
        return rcPanel;
    }
    
    public String getTitle() {
        return "Atas Module";
    }
    
    //Sieve will send the updates requested in RoleUpdateMessage by the GUI
    //Upon recieving a RoleUpdateMessage Sieve will send the following:
    //0) RoleType Number (int)
    //1) Number of HostInfos for this role (int)
    //2) The HostInfos themselves.
    public void processMessage(DataInputStream din) throws IOException {
        ObjectInputStream oin= new EunomiaObjectInputStream(din);
        
        int roleNumber = oin.readInt();
        String roleName = "";
        try {
            roleName = (String) oin.readObject();
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        }
        int n = oin.readInt(); //Number of HostInfos to follow
        
        if(n == 0) {
            return;
        }

        //Read the host info and update the roles
        HostInfo updatedInfo = null;
        for(int i = 0; i < n; ++i){
            try {
                updatedInfo= (HostInfo) oin.readObject();
            } catch (ClassNotFoundException ex) {
                ex.printStackTrace();
            }

            //Update the information maintained in AssociationMap
            associationMap.updateWorkingSet(roleNumber, roleName, updatedInfo);
        }

        //Update the GUI
        associationMap.updateRoleDisplay(roleNumber, roleName);
    }
    
    public void setReceptor(ConsoleReceptor receptor) {
        mainPanel.init(receptor.getManager());
        this.receptor = receptor;
    }
    
    public void setProperty(String name, Object value) {
    }
    
    public Object getProperty(String name) {
        return null;
    }
    
    //Sieve periodically sends the following information to the GUI:
    //0) number of active roles it has (int)
    //1) for each active role: 
    //2)    send the role name (int)
    //3)    send number of hosts in it (int)
    //4)    send (ip, lastseen) pair (long, long)*(number of hosts)
    public void updateStatus(InputStream in) throws IOException {
        DataInputStream din = new DataInputStream(in);

        //Read activeRoles (guranteed to be > 0)
        int activeRoles = din.readInt();
        RoleUpdateMessage[] rcm= new RoleUpdateMessage[activeRoles];
        
        int updateMessageCount = 0;
        int roleNumber= 0;
        int hostsInRole= 0;
        int n=0;
        long ip, ts;
        long []list;
        HostInfo hi= null;

        
        //Now read activeRoles number of roles and their hosts
        for(int i=0; i < activeRoles; ++i){
            roleNumber= din.readInt();
            hostsInRole= din.readInt();
            
            if(hostsInRole == 0) 
                continue;
            
            list= new long[hostsInRole];
            
            n = 0;
            for(int j = 0; j < hostsInRole; ++j){
                ip = din.readLong(); //Read the IP of the host
                ts = din.readLong(); //Read the last seen time stamp
        
                if((hi = associationMap.getHostInfo(roleNumber, ip)) != null){
                    if(hi.getLastSeen() < ts){
                        list[n] = ip;
                        ++n;
                    }
                } else {
                    list[n] = ip;
                    ++n;
                }
            }
            
            //if GUI needs new data create a RoleUpdateMessage
            if(n > 0){
                rcm[updateMessageCount] = new RoleUpdateMessage(roleNumber);
                rcm[updateMessageCount].setList(list, n);
                ++updateMessageCount;
            }
        }

        //If we have RoleUpdateMessages to send, send them now.
        //These messages will be captured and processed by processMessage() on
        //the Sieve.
        for(int i=0; (i < updateMessageCount); ++i){
            try {
                ObjectOutput oo = new ObjectOutputStream(receptor.getManager().openInterModuleStream(this));
                oo.writeObject(rcm[i]);
                oo.close();
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }
    
    public void getControlData(OutputStream out) throws IOException {
        ObjectOutputStream oout = new ObjectOutputStream(out);
        
        List list = rcPanel.getMessageList();
        oout.writeInt(list.size());
        
        Iterator it = list.iterator();
        while (it.hasNext()) {
            Message msg = (Message) it.next();
            oout.writeObject(msg);
        }
    }
    
    public void setControlData(InputStream in) throws IOException {
        ObjectInputStream oin = new EunomiaObjectInputStream(in);
        
        int count = oin.readInt();
        for (int i = 0; i < count; i++) {
            try {
                ClassifierConfigurationMessage msg = (ClassifierConfigurationMessage)oin.readObject();
                rcPanel.addMessage(msg);
            } catch (ClassNotFoundException ex) {
                ex.printStackTrace();
            }
        }
    }

    public void setIntersection(ArrayList<String> roleNames) {
        System.err.println("Formed intersections between sets: ");
        
        for(int i=0; i < roleNames.size(); ++i){
            System.err.println("\t" + roleNames.get(i));
        }
        
        //associationMap.addRelationSet(roleNames);
        //System.err.println("Also Informed AssociationMap about this.");
    }

    public void removeIntersection(ArrayList<String> roleNames) {
        System.err.println("Removed intersections between sets: ");
        
        for(int i=0; i < roleNames.size(); ++i){
            System.err.println("\t" + roleNames.get(i));
        }
        
        //associationMap.removeRelationSet(roleNames);
        //System.err.println("Also Informed AssociationMap about this.");
    }

    public void insertRole(String roleName, ArrayList<HostInfo> hostList) {
    }

    public void removeRole(String roleName) {
    }

    public ArrayList<HostInfo> getHostsOfRole(String roleName) {
        return null;
    }

    public void insertHost(String roleName, HostInfo host) {
    }

    public void insertHosts(String roleName, ArrayList<HostInfo> hostList) {
    }

    public void insertHost(ArrayList<String> roleNames, HostInfo host) {
    }

    public void insertHosts(ArrayList<String> roleNames, ArrayList<HostInfo> hostList) {
    }

    public void removeHost(String roleName, HostInfo host) {
    }

    public void removeHosts(String roleName, ArrayList<HostInfo> hostList) {
    }

    public void removeHost(ArrayList<String> roleNames, HostInfo host) {
    }

    public void removeHosts(ArrayList<String> roleNames, ArrayList<HostInfo> hostList) {
    }
}
