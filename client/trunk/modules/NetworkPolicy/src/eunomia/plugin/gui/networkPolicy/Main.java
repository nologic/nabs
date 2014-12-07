/*
 * Main.java
 *
 * Created on December 15, 2006, 11:30 AM
 *
 */

package eunomia.plugin.gui.networkPolicy;

import com.vivic.eunomia.filter.Filter;
import com.vivic.eunomia.sys.frontend.ConsoleContext;
import eunomia.plugin.com.networkPolicy.AlertItem;
import eunomia.plugin.com.networkPolicy.AlertListMessage;
import eunomia.plugin.com.networkPolicy.PolicyItem;
import eunomia.plugin.com.networkPolicy.PolicyItemRemoveMessage;
import eunomia.plugin.com.networkPolicy.PolicyListMessage;
import eunomia.plugin.gui.networkPolicy.alert.AlertInbox;
import com.vivic.eunomia.module.frontend.FrontendProcessorModule;
import com.vivic.eunomia.sys.frontend.ConsoleReceptor;
import eunomia.plugin.com.networkPolicy.DeleteAlertListMessage;
import eunomia.plugin.gui.networkPolicy.alert.AlertEditor;
import eunomia.util.io.EunomiaObjectInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.DataInputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.swing.JComponent;
import org.apache.log4j.Logger;

/**
 *
 * @author kulesh, Mikhail Sosonkin
 */
public class Main implements FrontendProcessorModule, PolicyListChangeListener{
    private static Logger logger;
    static {
        logger = Logger.getLogger(Main.class);
    }
    
    private AlertInbox alertInbox;
    private String title;
    private ConsoleReceptor receptor;
    private HashMap policy;
    private Map alerts;
    
    private int requestedPolicyItems;
    private int requestedAlertItems;
    
    //Messages
    private AlertListMessage alertListMsg;
    private PolicyListMessage policyListMsg;
    
    public Main() {
        requestedPolicyItems = 0;
        requestedAlertItems = 0;
        
        alertListMsg = new AlertListMessage();
        policyListMsg = new PolicyListMessage();
        
        alertInbox = new AlertInbox(this);
        alertInbox.addPolicyListChangeListener(this);

        policy = new HashMap();
        alerts = Collections.synchronizedMap(new HashMap());
        
        this.receptor = ConsoleContext.getReceptor();
        
        PolicyItem.setModuleManager(receptor.getManager());
        alertInbox.getAlertPanel().getAlertEditor().setGlobalSettings(receptor.getGlobalSettings());
    }
    
    public ConsoleReceptor getReceptor() {
        return receptor;
    }
    
    public JComponent getJComponent() {
        return alertInbox;
    }
    
    public JComponent getControlComponent() {
        return null;
    }
    
    public String getTitle() {
        return "Network Policy Manager";
    }
    
    public Filter getFilter() {
        return null;
    }
    
    public void setProperty(String name, Object value) {
    }
    
    public Object getProperty(String name) {
        return null;
    }
    
    public void updateStatus(InputStream in) throws IOException {
        DataInputStream din = new DataInputStream(in);

        updatePolicy(din);
        updateAlerts(din);
        
        alertInbox.repaint();
    }
    
    private void updatePolicy(DataInputStream din) throws IOException {
        int count;
        int needed = 0;
        int id;
        
        count = din.readInt();
        if(count != 0) {
            int[] policyIdArray = new int[count];
            for(int i = 0; i < count; ++i) {
                id = din.readInt();
                if(policy.get(id) == null){
                    policyIdArray[needed++] = id;
                }
            }

            if(needed != 0) {
                policyListMsg.setList(policyIdArray, needed);
                sendObject(policyListMsg);
            }
        }
    }
    
    private void updateAlerts(DataInputStream din) throws IOException {
        int count;
        int needed = 0;
        long id;
        byte cc;
        AlertItem alert;
        
        count = din.readInt();
        if(count != 0) {
            long[] alertIdArray = new long[count];
            for(int i = 0; i < count; ++i){
                id = din.readLong();
                cc = din.readByte();
                
                if( (alert = (AlertItem)alerts.get(id)) == null || alert.getChangeCount() != cc){
                    alertIdArray[needed++] = id;
                }
            }
            if(needed != 0) {
                alertListMsg.setList(alertIdArray, needed);
                sendObject(alertListMsg);
            }
        }
    }
    
    private void sendObject(Object o) throws IOException {
        ObjectOutput oo = new ObjectOutputStream(receptor.getManager().openInterModuleStream(this));
        oo.writeObject(o);
        oo.close();
    }
    
    public void getControlData(OutputStream out) throws IOException {
    }
    
    public void setControlData(InputStream in) throws IOException {
    }
    
    public void deleteAlertList(long[] list) throws IOException {
        for (int i = 0; i < list.length; i++) {
            AlertItem item = (AlertItem)alerts.remove(list[i]);
            if(item != null) {
                item.getPolicyItem().addAlerts(-1);
                if(item.getStatus() == AlertItem.NEW) {
                    item.getPolicyItem().addNewAlerts(-1);
                }
            }
        }
        DeleteAlertListMessage msg = new DeleteAlertListMessage();
        msg.setList(list);
        sendObject(msg);
    }

    public void saveAlertList(List items) throws IOException {
        ObjectOutput oo = new ObjectOutputStream(receptor.getManager().openInterModuleStream(this));
        Iterator it = items.iterator();
        while (it.hasNext()) {
            AlertItem item = (AlertItem) it.next();
            oo.writeObject(item);
        }
        oo.close();
    }

    public void saveAlert(AlertItem item) throws IOException {
        sendObject(item);
    }
    
    public void policyAdded(PolicyItem p) {
        try {
            sendObject(p);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    
    public void policyRemoved(PolicyItem p) {
        policy.remove(p.getPolicyID());
        PolicyItemRemoveMessage msg = new PolicyItemRemoveMessage();
        msg.setId(p.getPolicyID());
        try {
            sendObject(msg);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    
    public void processMessage(DataInputStream in) throws IOException {
        ObjectInputStream oin = new EunomiaObjectInputStream(in);
        Object obj = null;
        
        while(in.available() != 0){
            try {
                obj = oin.readObject();
            } catch (ClassNotFoundException ex) {
                ex.printStackTrace();
            }
            
            if(obj instanceof AlertItem){
                AlertItem ai = (AlertItem)obj;
                AlertItem lAi = (AlertItem)alerts.get(ai.getAlertID());
                
                if(lAi == null) {
                    PolicyItem item = (PolicyItem)policy.get(ai.getPolicyID());
                    if(item != null) {
                        ai.setPolicyItem(item);
                        alerts.put(ai.getAlertID(), ai);
                        alertInbox.insertAlertItem(ai, item.getPolicyType());
                    }
                } else {
                    lAi.updateFrom(ai);
                    AlertEditor edit = alertInbox.getAlertPanel().getAlertEditor();
                    if(edit.getCurrentAlert() == lAi) {
                        edit.reload();
                    }
                }
            } else if (obj instanceof PolicyItem){
                PolicyItem pi = (PolicyItem)obj;

                policy.put(pi.getPolicyID(), pi);
                alertInbox.insertPolicyItem(pi);
            }
        }
    }
}