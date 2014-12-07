/*
 * NetworkListenerManager.java
 *
 * Created on July 12, 2008, 2:06 PM
 *
 */

package eunomia.module.receptor.libb.imsCore.listeners;

import com.vivic.eunomia.sys.util.Util;
import eunomia.module.receptor.libb.imsCore.net.DarkAccess;
import eunomia.module.receptor.libb.imsCore.net.NetworkChannel;
import eunomia.module.receptor.libb.imsCore.net.NetworkChannelFlowID;
import eunomia.module.receptor.libb.imsCore.net.NetworkEntity;
import eunomia.module.receptor.libb.imsCore.net.NetworkEntityHostKey;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 *
 * @author Mikhail Sosonkin
 */
public class NetworkListenerManager {
    private static final long LIST_CHECK_MILLISECONDS = 60000L;
    
    private Map actEntListeners;
    private Map actChnListeners;

    private NetworkEntityNewListener[] newEntitiesListeners;
    private NetworkChannelNewListener[] newChannelsListeners;
    private NetworkDarkAccessListener[] newDarkListeners;
    
    private long lastCheck;

    public NetworkListenerManager() {
        lastCheck = System.currentTimeMillis();
        
        newEntitiesListeners = new NetworkEntityNewListener[0];
        newChannelsListeners = new NetworkChannelNewListener[0];
        newDarkListeners = new NetworkDarkAccessListener[0];
        
        actEntListeners = new HashMap();
        actChnListeners = new HashMap();
    }

    public void fileNewDarkAccess(DarkAccess da) {
        NetworkDarkAccessListener[] tmp = newDarkListeners;
        for (int i = 0; i < tmp.length; ++i) {
            tmp[i].newDarkAccess(da);
        }
    }
    
    public void fireNewEntity(NetworkEntity ent) {
        NetworkEntityNewListener[] tmp = newEntitiesListeners;
        for (int i = 0; i < tmp.length; ++i) {
            NetworkEntityNewListener l = tmp[i];
            NetworkEntityActivityListener al = l.newEntity(ent);
            
            if(al != null) {
                addNetworkEntityActivityListener(al, ent.getHostKey());
            }
        }
    }
    
    public void fireNewChannel(NetworkChannel chan) {
        NetworkChannelNewListener[] tmp = newChannelsListeners;
        for (int i = 0; i < tmp.length; ++i) {
            NetworkChannelNewListener l = tmp[i];
            NetworkChannelActivityListener al = l.newChannel(chan);
            
            if(al != null) {
                addNetworkChannelActivityListener(al, chan.getChannelFlowID());
            }
        }
    }
    
    public boolean fireChannelActivity(NetworkChannel chan) {
        NCActEntry list = (NCActEntry)actChnListeners.get(chan.getChannelFlowID());
        
        if(list != null) {
            if(!list.notify(chan)) {
                actChnListeners.remove(chan.getChannelFlowID());
            } else {
                return true;
            }
        }
        
        return false;
    }
    
    public boolean fireEntityActivity(NetworkEntity ent) {
        NEActEntry list = (NEActEntry)actEntListeners.get(ent.getHostKey());
        
        if(list != null) {
            if(!list.notify(ent)) {
                actEntListeners.remove(ent.getKey());
            } else {
                // set was not null.
                return true;
            }
        }
        
        return false;
    }
    
    public void runChecks() {
        long time = System.currentTimeMillis();
        if(time - lastCheck > LIST_CHECK_MILLISECONDS) {
            Iterator it = actEntListeners.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry entry = (Map.Entry) it.next();
                NEActEntry neEnt = (NEActEntry)entry.getValue();
                
                if(time - neEnt.lastNotification > LIST_CHECK_MILLISECONDS) {
                    if(!neEnt.checkAll((NetworkEntityHostKey)entry.getKey(), time)) {
                        it.remove();
                    }
                }
            }

            it = actChnListeners.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry ent = (Map.Entry) it.next();
                NCActEntry ncEnt = (NCActEntry)ent.getValue();
                
                if(time - ncEnt.lastNotification > LIST_CHECK_MILLISECONDS) {
                    if(!ncEnt.checkAll((NetworkChannelFlowID)ent.getKey(), time)) {
                        it.remove();
                    }
                }
            }
        }
    }
    
    public void addNetworkDarkAccessListener(NetworkDarkAccessListener l) {
        newDarkListeners = (NetworkDarkAccessListener[])Util.arrayAppend(newDarkListeners, l);
    }
    
    public void addNetworkChannelNewListener(NetworkChannelNewListener l) {
        newChannelsListeners = (NetworkChannelNewListener[])Util.arrayAppend(newChannelsListeners, l);
    }
    
    public void addNetworkEntityNewListener(NetworkEntityNewListener l) {
        newEntitiesListeners = (NetworkEntityNewListener[])Util.arrayAppend(newEntitiesListeners, l);
    }
    
    public void addNetworkChannelActivityListener(NetworkChannelActivityListener l, Object key) {
        NCActEntry list = (NCActEntry)actChnListeners.get(key);
        if(list == null) {
            list = new NCActEntry(l);
            actChnListeners.put(key, list);
        } else {
            list.add(l);
        }
    }
    
    public void addNetworkEntityActivityListener(NetworkEntityActivityListener l, Object key) {
        NEActEntry list = (NEActEntry)actEntListeners.get(key);
        
        if(list == null) {
            list = new NEActEntry(l);
            actEntListeners.put(key, list);
        } else {
            list.add(l);
        }
    }
    
    private class ActEntry {
        protected long lastNotification;

        public long getLastNotification() {
            return lastNotification;
        }
    }
    
    private class NEActEntry extends ActEntry {
        private NetworkEntityActivityListener[] list;
        
        public NEActEntry(NetworkEntityActivityListener l) {
            list = new NetworkEntityActivityListener[10];
            list[0] = l;
        }
        
        public void add(NetworkEntityActivityListener l) {
            for (int i = 0; i < list.length; ++i) {
                if(list[i] == null) {
                    list[i] = l;
                    
                    return;
                }
            }
            
            list = (NetworkEntityActivityListener[])Util.arrayAppend(list, l);
        }
        
        public boolean notify(NetworkEntity ent) {
            boolean nonNull = false;
            
            for (int i = 0; i < list.length; ++i) {
                NetworkEntityActivityListener l = list[i];
                
                if(l != null) {
                    nonNull = true;
                    
                    if(!l.entityActivity(ent)) {
                        list[i] = null;
                    }
                }
            }
            
            lastNotification = System.currentTimeMillis();
            
            return nonNull;
        }
        
        public boolean checkAll(NetworkEntityHostKey key, long curTime) {
            boolean hasInterest = false;
            
            for (int i = 0; i < list.length; ++i) {
                NetworkEntityActivityListener l = list[i];
                
                if(l != null) {
                    if(!l.isStillInterested(key, curTime - lastNotification)) {
                        list[i] = null;
                    } else {
                        hasInterest = true;
                    }
                }
            }
            
            return hasInterest;
        }
    }
    
    private class NCActEntry extends ActEntry {
        public NetworkChannelActivityListener[] list;
        
        public NCActEntry(NetworkChannelActivityListener l) {
            list = new NetworkChannelActivityListener[10];
            list[0] = l;
        }
        
        public void add(NetworkChannelActivityListener l) {
            for (int i = 0; i < list.length; ++i) {
                if(list[i] == null) {
                    list[i] = l;
                    
                    return;
                }
            }
            
            list = (NetworkChannelActivityListener[])Util.arrayAppend(list, l);
        }
        
        public boolean notify(NetworkChannel chan) {
            boolean nonNull = false;
            
            for (int i = 0; i < list.length; ++i) {
                NetworkChannelActivityListener l = list[i];
                
                if(l != null) {
                    nonNull = true;
                    
                    if(!l.channelActivity(chan)) {
                        list[i] = null;
                    }
                }
            }
            
            lastNotification = System.currentTimeMillis();
            
            return nonNull;
        }
        
        public boolean checkAll(NetworkChannelFlowID key, long curTime) {
            boolean hasInterest = false;
            
            for (int i = 0; i < list.length; ++i) {
                NetworkChannelActivityListener l = list[i];
                
                if(l != null) {
                    if(!l.isStillInterested(key, curTime - lastNotification)) {
                        list[i] = null;
                    } else {
                        hasInterest = true;
                    }
                }
            }
            
            return hasInterest;
        }
    }
}