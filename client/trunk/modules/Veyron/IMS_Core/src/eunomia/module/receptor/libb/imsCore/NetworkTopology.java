/*
 * NetworkTopology.java
 *
 * Created on February 24, 2008, 3:50 PM
 *
 */

package eunomia.module.receptor.libb.imsCore;

import eunomia.module.receptor.libb.imsCore.iterators.FilteredMultiIterator;
import eunomia.module.receptor.libb.imsCore.listeners.NetworkListenerManager;
import eunomia.module.receptor.libb.imsCore.net.DarkAccess;
import eunomia.module.receptor.libb.imsCore.net.NetworkChannel;
import eunomia.module.receptor.libb.imsCore.net.NetworkChannelFlowID;
import eunomia.module.receptor.libb.imsCore.net.NetworkDefinition;
import eunomia.module.receptor.libb.imsCore.net.NetworkEntity;
import eunomia.module.receptor.libb.imsCore.net.NetworkEntityHostKey;
import eunomia.module.receptor.libb.imsCore.net.SelectSpec;
import java.util.Iterator;

/**
 * This interface defines the methods for analyzing stored network data. The data
 * is guaranteed to be valid for the last 24.178 hours. It is possible to retrieve
 * data from earlier times, however it may be incomplete. There are 3 main types of
 * entries:
 * <ul>
 *    <li>{@link NetworkEntity} - Currently hosts with IPv4 addresses.</li>
 *    <li>{@link NetworkChannel} - A communication link as defined by 
 *        {@link NetworkChannelFlowID} between 2 NetworkEntities.</li>
 *    <li>{@link DarkAccess} - An attempt by a NetworkEntity to access non-existent
 *        NetworkEntity</li>
 * </ul>
 * @author Mikhail Sosonkin
 */
public interface NetworkTopology {
    public NetworkListenerManager getNetworkListenerManager();
    
    /**
     * Returns the current network definition.
     * @return Network Definition
     */
    public NetworkDefinition getNetworkDefinition();
    
    /**
     * Returns the last activity in seconds. This time is for any event. The event is
     * defined by the Real-time collect module. Generally it is the time of the most 
     * recent connection activity.
     * @return Last activity.
     */
    public long getLastActivity();
    
    /**
     * Returns the total number of hosts in the last 24 hours.
     * @return Number of hosts
     */
    public int getHostCount();
    
    /**
     * Returns the number of distinct flow ID's in the last 24 hours.
     * @return Number of channles.
     */
    public int getChannelCount();
    
    /**
     * Returns the number of dark space accesses in the last 24 hours.
     * @return Number of dark space accesses.
     */
    public int getDarkAccessCount();

    /**
     * Returns an interator with all the hosts in the data store.
     * @return Hosts interator.
     */
    public FilteredMultiIterator getAllHosts();
    
    /**
     * Returns an iterator with all the channels in the data store.
     * @return channels iterator.
     */
    public FilteredMultiIterator getAllChannels();
    
    /**
     * Returns an iterator with all the dark space accesses in the data store.
     * @return dark space access iterator.
     */
    public FilteredMultiIterator getAllDarkAccesses();
    
    /**
     * Returns an Iterator of NetworkEntity objects for the corresponding key.
     * @param key A key that identifies the NetworkEntity
     * @return Either the object or null if the key is not found.
     */
    public Iterator getEntities(NetworkEntityHostKey key);
    
    /**
     * Returns an Iterator of NetworkEntity objects based on the IP address. 
     * This method is not thread safe.
     * @param ip Host IP address.
     * @return The NetworkEntity object or null if not found.
     */
    public Iterator getEntities(long ip);

    /**
     * 
     * @param ip1 
     * @param ip2 
     * @param port1 
     * @param port2 
     * @param protocol 
     * @return 
     */
    public Iterator getChannel(long ip1, long ip2, int port1, int port2, int protocol);
    
    public Iterator getChannel(NetworkChannelFlowID flowId);
    
    public Iterator getChannels(SelectSpec frame);
    
    public Iterator getChannelsForEntity(SelectSpec frame, NetworkEntityHostKey key, int op);
    
    public Iterator getChannelsBetweenEntities(SelectSpec frame, NetworkEntityHostKey key1, NetworkEntityHostKey key2);
    
    public Iterator getChannelsExternal(SelectSpec frame, boolean source);
    
    public Iterator getEntities(SelectSpec frame);
    
    public NetworkChannel getFirstChannelFor(NetworkEntityHostKey key);
    
    public NetworkChannel getLastChannelFor(NetworkEntityHostKey key);
    
    public Iterator getDarkAccesses(SelectSpec frame);
    
    public Iterator getDarkAccessesFromEntity(SelectSpec frame, NetworkEntityHostKey key);
}
