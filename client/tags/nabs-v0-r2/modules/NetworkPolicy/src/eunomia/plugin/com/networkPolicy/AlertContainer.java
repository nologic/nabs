/*
 * AlertContainer.java
 *
 * Created on December 19, 2006, 2:20 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package eunomia.plugin.com.networkPolicy;

import eunomia.plugin.networkPolicy.utils.FlowId;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

/**
 *
 * @author kulesh
 */
public class AlertContainer {
    
    private HashMap alerts;
    private HashMap<FlowId, AlertItem> hm;
    private int size=0;
    
    /** Creates a new instance of AlertContainer */
    public AlertContainer() {
        alerts= new HashMap();
        hm= new HashMap<FlowId, AlertItem>();
    }
    
    public int getSize(){
        return size;
    }
    
    public void putAlertItem(AlertItem a){
        alerts.put(a.getAlertID(), a);
        hm.put(a.getFlowId(), a);
        ++size;
    }

    public AlertItem removeAlertItem(AlertItem a){
        hm.remove(a.getFlowId());
        --size;
        return ((AlertItem)alerts.remove(a));
    }

    public Iterator iterator(){
        return ((alerts.entrySet()).iterator());
    }
    
    public AlertItem getAlertItem(long alertId){
        return ((AlertItem)alerts.get(alertId));
    }
    
    public AlertItem getAlertItem(FlowId flowId){
        return ((AlertItem)hm.get(flowId));
    }
    
}
