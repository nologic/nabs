/*
 * AlertContainer.java
 *
 * Created on December 19, 2006, 2:20 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package eunomia.plugin.utils.networkPolicy;

import eunomia.plugin.com.networkPolicy.*;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 *
 * @author kulesh
 */
public class AlertContainer {
    private ConcurrentHashMap alerts;
    private ConcurrentHashMap hm;
    private AtomicBoolean updating;
    
    /** Creates a new instance of AlertContainer */
    public AlertContainer() {
        alerts = new ConcurrentHashMap();
        hm = new ConcurrentHashMap();
        updating = new AtomicBoolean();
    }
    
    public int size(){
        return alerts.size();
    }
    
    public void putAlertItem(AlertItem a){
        while(!updating.compareAndSet(false, true));
        
        alerts.put(a.getAlertID(), a);
        hm.put(a.getFlowId(), a);
        updating.set(false);
    }

    public void removeAlertItem(AlertItem a){
        while(!updating.compareAndSet(false, true));
        
        hm.remove(a.getFlowId());
        alerts.remove(a.getAlertID());
        updating.set(false);
    }

    public Iterator iterator(){
        return alerts.entrySet().iterator();
    }
    
    public AlertItem getAlertItem(long alertId){
        return (AlertItem)alerts.get(alertId);
    }
    
    public AlertItem getAlertItem(Object flowId){
        return (AlertItem)hm.get(flowId);
    }
    
}
