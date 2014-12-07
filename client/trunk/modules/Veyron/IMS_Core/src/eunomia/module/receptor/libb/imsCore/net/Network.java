/*
 * Network.java
 *
 * Created on January 6, 2008, 5:30 PM
 *
 */

package eunomia.module.receptor.libb.imsCore.net;

import eunomia.module.receptor.libb.imsCore.db.NetEnv;
import eunomia.module.receptor.libb.imsCore.NetworkTopology;
import eunomia.module.receptor.libb.imsCore.db.NetworkInserter;
import eunomia.module.receptor.libb.imsCore.iterators.ChannelEntityFilter;
import eunomia.module.receptor.libb.imsCore.iterators.ChannelSharedEntityFilter;
import eunomia.module.receptor.libb.imsCore.iterators.ChannelSubnetFilter;
import eunomia.module.receptor.libb.imsCore.iterators.DarkAccessSourceFilter;
import eunomia.module.receptor.libb.imsCore.iterators.FilteredMultiIterator;
import eunomia.module.receptor.libb.imsCore.iterators.IteratorFilter;
import eunomia.module.receptor.libb.imsCore.listeners.NetworkListenerManager;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author Mikhail Sosonkin
 */
public class Network implements NetworkTopology {
    public static final int PROTOCOL_TCP = 6;
    public static final int PROTOCOL_UDP = 17;
    
    private NetEnv env;
    private NetworkDefinition ndef;
    private NetworkInserter nins;
    private SelectSpec allTime;
    
    public Network(NetEnv env, NetworkInserter nins) {
        this.env = env;
        this.nins = nins;
        
        allTime = new SelectSpec(0, Long.MAX_VALUE, SelectSpec.START_ACTIVITY);
        ndef = new NetworkDefinition();
    }
    
    public NetworkListenerManager getNetworkListenerManager() {
        return nins.getNetworkListenerManager();
    }
    
    public NetworkDefinition getNetworkDefinition() {
        return ndef;
    }
    
    public long getLastActivity() {
        return nins.getLastActivity();
    }
    
    private int getEntryCount(int map) {
        return env.getMap(map, null).size();
    }
    
    private FilteredMultiIterator getValuesIterator(int map) {
        return env.getMap(map, null).valuesIterator();
    }
    
    private Collection getEntryDups(Object key, int map) {
        return env.getMap(map, null).duplicates(key);
    }
    
    public int getHostCount() {
        return getEntryCount(NetEnv.MAP_ENTITY);
    }
    
    public int getChannelCount() {
        return getEntryCount(NetEnv.MAP_CHANNEL);
    }
    
    public int getDarkAccessCount() {
        return getEntryCount(NetEnv.MAP_DARKSPACE);
    }

    public FilteredMultiIterator getAllHosts() {
        return getValuesIterator(NetEnv.MAP_ENTITY);
    }
    
    public FilteredMultiIterator getAllChannels() {
        return getValuesIterator(NetEnv.MAP_CHANNEL);
    }
    
    public FilteredMultiIterator getAllDarkAccesses() {
        return getValuesIterator(NetEnv.MAP_DARKSPACE);
    }
    
    public Iterator getEntities(NetworkEntityHostKey key) {
        return getEntryDups(key, NetEnv.SEC_HOST_ENTITY).iterator();
    }
    
    public Iterator getEntities(long ip) {
        return getEntities(NetworkEntityHostKey.wrapIPv4(ip));
    }

    public Iterator getChannel(long ip1, long ip2, int port1, int port2, int protocol) {
        NetworkChannelFlowID chanId = new NetworkChannelFlowID();
        chanId.setKey(NetworkEntityHostKey.wrapIPv4(ip1), NetworkEntityHostKey.wrapIPv4(ip2), port1, port2, protocol);

        return getChannel(chanId);
    }
    
    public Iterator getChannel(NetworkChannelFlowID flowId) {
        return getEntryDups(flowId, NetEnv.SEC_FLOW_CHANNEL).iterator();
    }
    
    private List getChannelsByTimeMarker(long startMarker, long endMarker, int timeType) {
        long firstMarker = nins.getFirstMarker();
        long lastMarker = nins.getLastMarker();
        
        if(startMarker < firstMarker) {
            startMarker = firstMarker;
        }
        
        if(endMarker > lastMarker) {
            endMarker = lastMarker;
        }
        
        int map = NetEnv.MAP_CHANNEL;
        
        switch(timeType) {
            case SelectSpec.START_ACTIVITY:
                //map = ImsEnv.SEC_START_TIME_CHANNEL;
                break;
            case SelectSpec.LAST_ACTIVITY:
                //map = ImsEnv.SEC_LAST_ACT_TIME_CHANNEL;
                break;
        }
        
        ArrayList list = new ArrayList();
        for (; startMarker <= endMarker; ++startMarker) {
            Collection coll = getEntryDups(Long.valueOf(startMarker), map);
            
            list.add(coll.iterator());
        }
        
        return list;
    }
    
    public Iterator getChannelsForEntity(SelectSpec frame, NetworkEntityHostKey key, int op) {
        if(frame == null) {
            frame = allTime;
        }
        
        Collection coll = getEntryDups(key, NetEnv.SEC_ENTITY_CHANNEL);
        Iterator channels = coll.iterator();
        //Iterator channels = env.getValuesIterator(ImsEnv.MAP_CHANNEL);
        
        if(op == ChannelEntityFilter.EITHER_ENTITY) {
            return new FilteredMultiIterator(channels, frame.getChannelTimeFilter());
        } else {
            return new FilteredMultiIterator(channels, new IteratorFilter[] {
                    frame.getChannelTimeFilter(),
                    new ChannelEntityFilter(key, key, op)}
            );
        }
    }
    
    public Iterator getChannelsBetweenEntities(SelectSpec frame, NetworkEntityHostKey key1, NetworkEntityHostKey key2) {
        if(frame == null) {
            frame = allTime;
        }

        //Collection coll1 = env.getEntryDups(key1, ImsEnv.SEC_ENTITY_CHANNEL);
        //Collection coll2 = env.getEntryDups(key2, ImsEnv.SEC_ENTITY_CHANNEL);
        Iterator channels = getValuesIterator(NetEnv.MAP_CHANNEL);
        
        IteratorFilter chTimeFilter = frame.getChannelTimeFilter();
        
        ArrayList iters = new ArrayList();
        iters.add(new FilteredMultiIterator(channels, chTimeFilter));
        //iters.add(new FilteredMultiIterator(coll2.iterator(), chTimeFilter));
        
        return new FilteredMultiIterator(iters, new ChannelSharedEntityFilter(key1, key2));
    }
    
    public Iterator getChannels(SelectSpec frame) {
        if(frame == null) {
            frame = allTime;
        }

        List list = getChannelsByTimeMarker(frame.getStartTime() >> NetworkInserter.TIME_SHIFT, 
                                            frame.getEndTime() >> NetworkInserter.TIME_SHIFT, 
                                            frame.getType());
        
        return new FilteredMultiIterator(list, frame.getChannelTimeFilter());
    }
    
    public Iterator getChannelsExternal(SelectSpec frame, boolean source) {
        if(frame == null) {
            frame = allTime;
        }

        List list = getChannelsByTimeMarker(frame.getStartTime() >> NetworkInserter.TIME_SHIFT, 
                                            frame.getEndTime() >> NetworkInserter.TIME_SHIFT, 
                                            frame.getType());
        
        IteratorFilter[] filters = new IteratorFilter[] {
            frame.getChannelTimeFilter(),
            new ChannelSubnetFilter(ndef, source)
        };
        
        return new FilteredMultiIterator(list, filters);
    }
    
    public Iterator getEntities(SelectSpec frame) {
        if(frame == null) {
            frame = allTime;
        }

        long firstMarker = nins.getFirstMarker();
        long lastMarker = nins.getLastMarker();

        long timeStartSeconds = frame.getStartTime();
        long timeEndSeconds = frame.getEndTime();
        
        long startMarker = timeStartSeconds >> NetworkInserter.TIME_SHIFT;
        long endMarker = timeEndSeconds >> NetworkInserter.TIME_SHIFT;
        
        if(startMarker < firstMarker) {
            startMarker = firstMarker;
        }
        
        if(endMarker > lastMarker) {
            endMarker = lastMarker;
        }
        
        int map = NetEnv.MAP_ENTITY;
        
        switch(frame.getType()) {
            case SelectSpec.START_ACTIVITY:
                //map = ImsEnv.SEC_START_TIME_ENTITY;
                break;
            case SelectSpec.LAST_ACTIVITY:
                //map = ImsEnv.SEC_LAST_ACT_TIME_ENTITY;
                break;
        }
        
        // Need to figure out how to do it by time
        ArrayList list = new ArrayList();
        /*for (; startMarker <= endMarker; ++startMarker) {
            Collection coll = env.getEntryDups(Long.valueOf(startMarker), map);
            
            list.add(coll.iterator());
        }*/
        list.add(this.getAllHosts());
        
        return new FilteredMultiIterator(list, frame.getEntityTimeFilter());
    }
    
    public NetworkChannel getFirstChannelFor(NetworkEntityHostKey key) {
        NetworkChannel ch = null;
        Iterator it = getChannelsForEntity(null, key, ChannelEntityFilter.EITHER_ENTITY);
        
        while(it.hasNext()) {
            NetworkChannel channel = (NetworkChannel)it.next();
            
            if(ch == null || ch.getStartTime().getSeconds() > channel.getStartTime().getSeconds()) {
                ch = channel;
            }
        }
        
        return ch;
    }
    
    public NetworkChannel getLastChannelFor(NetworkEntityHostKey key) {
        NetworkChannel ch = null;
        Iterator it = getChannelsForEntity(null, key, ChannelEntityFilter.EITHER_ENTITY);
        
        while(it.hasNext()) {
            NetworkChannel channel = (NetworkChannel)it.next();
            
            if(ch == null || ch.getEndTime().getSeconds() < channel.getEndTime().getSeconds()) {
                ch = channel;
            }
        }
        
        return ch;
    }
    
    public Iterator getDarkAccesses(SelectSpec frame) {
        if(frame == null) {
            frame = allTime;
        }

        long timeStartSeconds = frame.getStartTime();
        long timeEndSeconds = frame.getEndTime();
        long startMarker = timeStartSeconds >> NetworkInserter.TIME_SHIFT;
        long endMarker = timeEndSeconds >> NetworkInserter.TIME_SHIFT;
        long firstMarker = nins.getFirstMarker();
        long lastMarker = nins.getLastMarker();

        if(startMarker < firstMarker) {
            startMarker = firstMarker;
        }
        
        if(endMarker > lastMarker) {
            endMarker = lastMarker;
        }
        
        ArrayList list = new ArrayList();
        for (; startMarker <= endMarker; ++startMarker) {
            Collection coll = getEntryDups(Long.valueOf(startMarker), NetEnv.SEC_TIME_DARKSPACE);
            
            list.add(coll.iterator());
        }
        
        return new FilteredMultiIterator(list, frame.getDarkAccessTimeFilter());
    }

    public Iterator getDarkAccessesFromEntity(SelectSpec frame, NetworkEntityHostKey key) {
        if(frame == null) {
            frame = allTime;
        }

        long timeStartSeconds = frame.getStartTime();
        long timeEndSeconds = frame.getEndTime();
        long startMarker = timeStartSeconds >> NetworkInserter.TIME_SHIFT;
        long endMarker = timeEndSeconds >> NetworkInserter.TIME_SHIFT;
        long firstMarker = nins.getFirstMarker();
        long lastMarker = nins.getLastMarker();

        if(startMarker < firstMarker) {
            startMarker = firstMarker;
        }
        
        if(endMarker > lastMarker) {
            endMarker = lastMarker;
        }
        
        ArrayList list = new ArrayList();
        for (; startMarker <= endMarker; ++startMarker) {
            Collection coll = getEntryDups(Long.valueOf(startMarker), NetEnv.SEC_TIME_DARKSPACE);
            
            list.add(coll.iterator());
        }
        
        return new FilteredMultiIterator(list, new IteratorFilter[]{
                                                new DarkAccessSourceFilter(key.getIPv4()),
                                                frame.getDarkAccessTimeFilter()});
    }
}