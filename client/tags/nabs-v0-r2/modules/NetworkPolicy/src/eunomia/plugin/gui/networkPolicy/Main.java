/*
 * Main.java
 *
 * Created on December 15, 2006, 11:30 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package eunomia.plugin.gui.networkPolicy;

import eunomia.config.ConfigChangeListener;
import eunomia.core.receptor.Receptor;
import eunomia.core.receptor.listeners.MessageReceiver;
import eunomia.flow.Filter;
import eunomia.messages.Message;
import eunomia.messages.module.msg.GenericModuleMessage;
import eunomia.plugin.com.networkPolicy.AlertItem;
import eunomia.plugin.com.networkPolicy.AlertListMessage;
import eunomia.plugin.com.networkPolicy.PolicyItem;
import eunomia.plugin.com.networkPolicy.PolicyListMessage;
import eunomia.plugin.interfaces.GUIModule;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.Iterator;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.apache.log4j.Logger;

/**
 *
 * @author kulesh
 */
public class Main implements GUIModule, MessageReceiver, ConfigChangeListener{
    
    private static Logger logger;
    static {
        logger = Logger.getLogger(Main.class);
    }
    private long total = 0;
    private JComponent policyComponent;
    private ConfigPanel configPanel;
    private String title;
    private Receptor receptor;
    private HashMap policy;
    private HashMap alerts;
    
    private long[] policyIdArray;
    private long[] alertIdArray;
    
    private int requestedPolicyItems;
    private int requestedAlertItems;
    
    /** Creates a new instance of Main */
    public Main() {
        requestedPolicyItems= 0;
        requestedAlertItems = 0;
        
        configPanel= new ConfigPanel();
        policyComponent= new JPanel();
        policy= new HashMap();
        alerts= new HashMap();
    }
    
    public JComponent getJComponent() {
        return policyComponent;
    }
    
    public JComponent getControlComponent() {
        return configPanel;
    }
    
    public String getTitle() {
        return "Network Policy Manager";
    }
    
    public Filter getFilter() {
        return null;
    }
    
    public MessageReceiver getReceiver() {
        return this;
    }
    
    public void setReceptor(Receptor receptor) {
        this.receptor= receptor;
    }
    
    public Receptor getReceptor() {
        return receptor;
    }
    
    public void setProperty(String name, Object value) {
    }
    
    public Object getProperty(String name) {
        return null;
    }
    
    public void updateStatus(InputStream in) throws IOException {
        DataInputStream din= new DataInputStream(in);
        
        ++total;
        
        updatePolicy(din);
        //updateAlerts(din);
        
        if((total %1000) == 0)
            displayPolicy();
        
    }
    
    public void getControlData(OutputStream out) throws IOException {
        DataOutputStream dout= new DataOutputStream(out);
        
        dout.writeInt(configPanel.getRateSlider());
        dout.writeLong(configPanel.getTimeoutSlider());
    }
    
    public void setControlData(InputStream in) throws IOException {
        DataInputStream din= new DataInputStream(in);
        
        configPanel.setRateSlider(din.readInt());
        configPanel.setTimeoutSlider((int)(din.readLong()));
    }
    
    public void configurationChanged() {
    }
    
    private void updateAlerts(DataInputStream din) throws IOException {
        int n, m=0;
        long id;
        AlertItem ai;
        
        n= din.readInt();
        if(n == 0) return;
        
        System.err.println("[" + System.currentTimeMillis() + "] FrontEnd: middleware has " + n + " alertIds");
        
        alertIdArray= new long[n];
        for(int i=0; i < n; ++i){
            //System.err.println("going to read alert " + i);
            id= din.readLong();
            //System.err.println("read alert(" + id + ")" + i);
            if((ai= (AlertItem)alerts.get(id))==null){
                alertIdArray[i]=id;
                ++m;
            }
        }
        AlertListMessage alm= new AlertListMessage();
        alm.setList(alertIdArray, m);
        
        System.err.println("[" + System.currentTimeMillis() +"] FrontEnd did not have " + m + " alertIds");
        
        GenericModuleMessage gmm= (receptor.getManager()).prepareGenericMessage(this);
        try{
            ObjectOutput oo= new ObjectOutputStream(gmm.getOutputStream());
            oo.writeObject(alm);
            receptor.getManager().sendGenericMessage(this, gmm);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    
    private void updatePolicy(DataInputStream din) throws IOException {
        int n, m=0;
        long id;
        PolicyItem pi;
        
        n= din.readInt();
        if(n==0) return;
        
        System.err.println("[" + System.currentTimeMillis() + "] FrontEnd: middleware has: " + n + " PolicyIds");
        
        policyIdArray= new long[n];
        for(int i=0; i < n; ++i){
            id= din.readLong();
            if((pi= (PolicyItem)policy.get(id))==null){
                policyIdArray[i]= id;
                ++m;
            }
        }
        
        System.err.println("[" + System.currentTimeMillis() +"] FrontEnd did not have "+ m+ " PolicyIds");
        
        PolicyListMessage plm= new PolicyListMessage();
        plm.setList(policyIdArray, m);
        
        GenericModuleMessage gmm= (receptor.getManager()).prepareGenericMessage(this);
        try{
            ObjectOutput oo= new ObjectOutputStream(gmm.getOutputStream());
            oo.writeObject(plm);
            receptor.getManager().sendGenericMessage(this, gmm);
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    
    public void messageResponse(Message msg){
        //get the PolicyItems and AlertItems from the receptor and populate the
        //appropriare hashmaps.
        int alertsRead=0;
        int policiesRead=0;
        
        if(!(msg instanceof GenericModuleMessage))
            return;
        
        System.err.println("[" + System.currentTimeMillis() + "] FrontEnd: messageResponse() kicked in.");
        
        InputStream in= ((GenericModuleMessage)msg).getInputStream();
        try {
            ObjectInputStream oin= new ObjectInputStream(in);
            Object obj;
            System.err.println("Frontend ObjectInputStream.available() = " + oin.available());
            while(oin.available()!=0){
                System.err.println("[" + System.currentTimeMillis() + "] FrontEnd: before Object read");
                obj= oin.readObject();
                System.err.println("[" + System.currentTimeMillis() + "] FrontEnd: recieved a " + obj.getClass() + "object");
                if(obj instanceof AlertItem){
                    AlertItem ai= (AlertItem)obj;
                    alerts.put(ai.getAlertID(), ai);
                    ++alertsRead;
                }else if (obj instanceof PolicyItem){
                    PolicyItem pi= (PolicyItem)obj;
                    policy.put(pi.getPolicyID(), pi);
                    ++policiesRead;
                }
            }
            System.err.println("[" + System.currentTimeMillis() + "] FrontEnd read " + alertsRead + " alerts and " + policiesRead + "policies from middleware");
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        }
    }
 
    private void displayAlerts(){
        Iterator it= ((alerts.entrySet()).iterator());
        Map.Entry tmp= null;
        AlertItem ai=null;
        
        while(it.hasNext()){
            tmp= (Map.Entry)it.next();
            ai= (AlertItem)tmp.getValue();
            logger.info(ai);
        }
    }
    
    private void displayPolicy(){
        Iterator it= ((policy.entrySet()).iterator());
        Map.Entry tmp= null;
        PolicyItem pi=null;
        
        while(it.hasNext()){
            tmp= (Map.Entry)it.next();
            pi= (PolicyItem)tmp.getValue();
            logger.info(pi);
        }
    }
}