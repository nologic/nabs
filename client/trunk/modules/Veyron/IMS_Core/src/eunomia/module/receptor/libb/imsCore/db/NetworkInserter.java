/*
 * NetworkInserter.java
 *
 * Created on September 1, 2008, 6:20 PM
 *
 */

package eunomia.module.receptor.libb.imsCore.db;

import com.sleepycat.db.DatabaseException;
import com.vivic.eunomia.sys.receptor.SieveContext;
import eunomia.module.receptor.libb.imsCore.EnvironmentEntry;
import eunomia.module.receptor.libb.imsCore.listeners.NetworkListenerManager;
import eunomia.module.receptor.libb.imsCore.net.DarkAccess;
import eunomia.module.receptor.libb.imsCore.net.DarkAccessKey;
import eunomia.module.receptor.libb.imsCore.net.NetworkChannel;
import eunomia.module.receptor.libb.imsCore.net.NetworkChannelFlowID;
import eunomia.module.receptor.libb.imsCore.net.NetworkEntity;
import eunomia.module.receptor.libb.imsCore.net.NetworkEntityHostKey;
import eunomia.module.receptor.libb.imsCore.net.NetworkEntryKey;
import eunomia.module.receptor.libb.imsCore.util.MicroTime;

/**
 *
 * @author Mikhail Sosonkin
 */
public class NetworkInserter {
    public static final int TIME_SHIFT    = 10; // 1024 seconds about 17.067 minutes.
    public static final int MAX_TIME_DISTANCE = 85; // 85 intervals is 24.178 hours.

    private NetEnv env;

    private NetworkEntityHostKey neRet;
    private NetworkChannelFlowID chanId;
    private NetworkEntryKey chanKey;
    private NetworkEntityHostKey chanKeyNe1;
    private NetworkEntityHostKey chanKeyNe2;
    private DarkAccessKey dsKey;
    
    private volatile long lastActivity; // in seconds.
    private volatile long firstMarker;
    private volatile long lastMarker;
    private volatile long dsCount;
    private volatile long chCount;
    private volatile long hsCount;
    
    private NetworkListenerManager netLisMan;
    private long largestTime;
    
    public NetworkInserter(NetEnv e) {
        this.env = e;
        
        netLisMan = new NetworkListenerManager();
        
        dsKey = new DarkAccessKey();
        neRet = new NetworkEntityHostKey();
        chanId = new NetworkChannelFlowID();
        chanKey = new NetworkEntryKey();
        chanKeyNe1 = new NetworkEntityHostKey();
        chanKeyNe2 = new NetworkEntityHostKey();
        
        String str = SieveContext.getModuleProperty("imsCore", "reset");
        if(str != null && str.toLowerCase().equals("true")) {
            lastActivity = 0;
            firstMarker = 0;
            lastMarker = 0;
            dsCount = 0;
            chCount = 0;
            hsCount = 0;
        } else {
            lastActivity = env.getMisc("lastActivity");
            firstMarker = env.getMisc("firstMarker");
            lastMarker = env.getMisc("lastMarker");
            dsCount = env.getMisc("dsCount");
            chCount = env.getMisc("chCount");
            hsCount = env.getMisc("hsCount");
        }
    }
    
    public void close() {
        env.putMisc("lastActivity", lastActivity);
        env.putMisc("firstMarker", firstMarker);
        env.putMisc("lastMarker", lastMarker);
        env.putMisc("dsCount", dsCount);
        env.putMisc("chCount", chCount);
        env.putMisc("hsCount", hsCount);

        try {
            env.close();
        } catch (DatabaseException ex) {
            ex.printStackTrace();
        }
    }
    
    public NetworkListenerManager getNetworkListenerManager() {
        return netLisMan;
    }
    
    public long getLastActivity() {
        return lastActivity;
    }
    
    public long getFirstMarker() {
        return firstMarker;
    }
    
    public long getLastMarker() {
        return lastMarker;
    }
    
    public void configureDarkAccessKey(DarkAccess da) {
        NetworkEntryKey ds = da.getEntryKey();

        if(ds.getNum() == -1){
            ds.setNum(dsCount++);
        }
    }
    
    public void configureChannelKey(NetworkChannel chan) {
        NetworkEntryKey ch = chan.getEntryKey();

        if(ch.getNum() == -1){
            ch.setNum(chCount++);
        }
    }
    
    public void configureEntityKey(NetworkEntity ent) {
        NetworkEntryKey ch = ent.getEntryKey();

        if(ch.getNum() == -1){
            ch.setNum(hsCount++);
        }
    }
    
    public void flow_addDarkAccess(DarkAccess[] da, int offset, int count) {
        env.commit(da, NetEnv.MAP_DARKSPACE, offset, count);
        
        for (int i = 0; i < count; ++i) {
            DarkAccess d = da[offset + i];
            
            netLisMan.fileNewDarkAccess(d);
        }
    }

    public long flow_getLargestTime() {
        return largestTime;
    }

    public void flow_addChannels(NetworkChannel[] channels, int offset, int count) {
        largestTime = Long.MIN_VALUE;
        if(lastActivity == 0) {
            // this is our 1st time, so we need to find the largest time first
            //  to perform the initial roll.
            for (int i = 0; i < count; i++) {
                NetworkChannel c = channels[offset + i];
                MicroTime t = c.getEndTime();

                if(t.getSeconds() > largestTime) {
                    largestTime = t.getSeconds();
                }
            }
            
            this.flow_setLastActivity(largestTime);
        }
        
        env.commit(channels, NetEnv.MAP_CHANNEL, offset, count);
        
        for (int i = 0; i < count; i++) {
            NetworkChannel c = channels[offset + i];
            MicroTime t = c.getEndTime();
            
            if(t.getSeconds() > largestTime) {
                largestTime = t.getSeconds();
            }
            
            // Logic: if there were active channel listeners then this channel
            //  must not be new. otherwise it might be new and of interest to
            //  someone
            if(!netLisMan.fireChannelActivity(c)) {
                netLisMan.fireNewChannel(c);
            }
        }
        
        // Don't want to put this into it's own thread, so this seems like a good place
        //  because it runs sometimes but infrequent enough.
        netLisMan.runChecks();
    }
    
    public void flow_addEntities(NetworkEntity[] hosts, int offset, int count) {
        largestTime = Long.MIN_VALUE;
        env.commit(hosts, NetEnv.MAP_ENTITY, offset, count);
        
        for (int i = 0; i < count; i++) {
            NetworkEntity e = hosts[offset + i];
            MicroTime t = e.getEndTime();
            
            if(t.getSeconds() > largestTime) {
                largestTime = t.getSeconds();
            }
            
            if(!netLisMan.fireEntityActivity(e)) {
                netLisMan.fireNewEntity(e);
            }
        }
    }
    
    public void flow_setLastActivity(long time) {
        if(lastActivity < time) {
            lastActivity = time;
        }
        
        if(lastMarker == 0) {
            lastMarker = time >> TIME_SHIFT;
        }
        
        long newMarker = time >> TIME_SHIFT;
        if(newMarker > lastMarker) {
            lastMarker = newMarker;
            
            String prefix = Long.toString(lastMarker);
            env.rollDB(NetEnv.MAP_CHANNEL, prefix);
            env.rollDB(NetEnv.MAP_ENTITY, prefix);
        }
    }
}
