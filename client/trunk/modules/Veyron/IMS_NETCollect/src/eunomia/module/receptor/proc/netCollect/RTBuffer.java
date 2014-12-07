/*
 * rtBuffer.java
 *
 * Created on August 3, 2008, 2:37 PM
 *
 */

package eunomia.module.receptor.proc.netCollect;

import eunomia.module.receptor.libb.imsCore.net.NetworkChannel;
import eunomia.module.receptor.libb.imsCore.net.NetworkChannelFlowID;
import eunomia.module.receptor.libb.imsCore.net.NetworkEntity;
import eunomia.module.receptor.libb.imsCore.net.NetworkEntityHostKey;
import eunomia.module.receptor.libb.imsCore.net.NetworkEntryKey;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

/**
 * - Maintains a list of open (non-timed out) connections
 * - Queues flows before commiting to DB.
 * - Exists in the realtime processing thread.
 *
 * @author Mikhail Sosonkin
 */
public class RTBuffer {
    private static int MAX_CHANNEL_ARCH_ENTRIES = 50000;
    private static int MAX_ENTITY_ARCH_ENTRIES = 20000;
    
    private long chan_timeoutSeconds = 6*60; // 5 mins is the timeout on the sensor.
    private long ent_timeoutSeconds = 6*60;
    
    private HashMap channelMap;
    private HashMap hostsMap;
    
    private NetworkChannel bChan;
    private NetworkEntity bEnt;
    
    private long lastScan;
    
    private BoundObjectDiskQueue channelQueue;
    private BoundObjectDiskQueue entityQueue;
    
    public RTBuffer() {
        channelMap = new HashMap();
        hostsMap = new HashMap();
        
        bEnt = new NetworkEntity(null);
        
        bChan = new NetworkChannel(null);
        bChan.setKey(new NetworkEntryKey());
        
        bChan.clearData();
        
        channelQueue = new BoundObjectDiskQueue(MAX_CHANNEL_ARCH_ENTRIES, bChan);
        entityQueue = new BoundObjectDiskQueue(MAX_ENTITY_ARCH_ENTRIES, bEnt);
    }
    
    public NetworkChannel lookup(NetworkChannelFlowID id) {
        NetworkChannel chan = (NetworkChannel)channelMap.get(id);
        if(chan == null) {
            // try again in the other direction.
            id.flip();
            chan = (NetworkChannel)channelMap.get(id);
            id.flip();
        }
        
        return chan;
    }
    
    public NetworkEntity lookup(NetworkEntityHostKey ip) {
        return (NetworkEntity)hostsMap.get(ip);
    }
    
    public void put(NetworkEntity ent, boolean isNew) {
        if(isNew) {
            NetworkEntityHostKey ip = ent.getHostKey();
            hostsMap.put(ip, ent);
        }
    }
    
    public void put(NetworkChannel ch, boolean isNew, boolean isFlipped) {
        NetworkChannelFlowID id = ch.getChannelFlowID();
        
        if(isNew) {
            channelMap.put(id, ch);
        } else if(isFlipped) {
            id.flip();
            channelMap.remove(id);
            id.flip();
            channelMap.put(id, ch);
        }
    }
    
    public NetworkChannel getCleanChannel(NetworkChannelFlowID flowId) {
        bChan.setFlowID(flowId);
        
        return bChan.clone();
    }
    
    public NetworkEntity getCleanEntity(NetworkEntityHostKey ip) {
        bEnt.setHostKey(ip);
        
        return bEnt.clone();
    }
    
    public int getSize() {
        return channelMap.size();
    }
    
    public void scanTimeOuts(long time) {
        if(time - lastScan < chan_timeoutSeconds) {
            return;
        }

        Iterator it = channelMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry) it.next();
            NetworkChannel ch = (NetworkChannel)entry.getValue();
            
            if(time - ch.getEndTime().getSeconds() > chan_timeoutSeconds) {
                it.remove();
                channelQueue.putArch(ch);
            }
        }
        
        it = hostsMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry) it.next();
            NetworkEntity ent = (NetworkEntity)entry.getValue();
            
            if(time - ent.getStartTime().getSeconds() > ent_timeoutSeconds) {
                it.remove();
                entityQueue.putArch(ent);
            }
        }
        
        lastScan = time;
    }
    
    public void flagForArchiving(NetworkChannel chan, boolean isFlipped) {
        NetworkChannelFlowID id = chan.getChannelFlowID();
        
        if(isFlipped) {
            id.flip();
            channelMap.remove(id);
            id.flip();
        } else {
            channelMap.remove(id);
        }
        
        channelQueue.putArch(chan);
    }
    
    public NetworkChannel[] getChannelArchArray() {
        return (NetworkChannel[]) channelQueue.getNextArray();
    }
    
    public NetworkEntity[] getEntityArchArray() {
        return (NetworkEntity[]) entityQueue.getNextArray();
    }
    
    private long lastAsked;
    public boolean isArchReady() {
        long time = System.currentTimeMillis();
        if(time - lastAsked > 10000L) {
            // if not at 20% within 10s then empty it anyway.
            lastAsked = time;
            
            return channelQueue.getQueueSize() > 0;
        }
        
        return channelQueue.getQueueSize() >= MAX_CHANNEL_ARCH_ENTRIES * 0.2;
    }
}