/*
 * ImsEnv.java
 *
 * Created on December 6, 2007, 7:05 PM
 *
 */

package eunomia.module.receptor.libb.imsCore.db;

import com.sleepycat.bind.EntryBinding;
import com.sleepycat.bind.tuple.ByteBinding;
import com.sleepycat.bind.tuple.LongBinding;
import com.sleepycat.db.DatabaseException;
import com.sleepycat.db.DatabaseType;
import com.sleepycat.db.SecondaryKeyCreator;
import com.sleepycat.db.SecondaryMultiKeyCreator;
import com.sleepycat.db.StatsConfig;
import eunomia.config.Config;
import eunomia.module.receptor.libb.imsCore.*;
import eunomia.module.receptor.libb.imsCore.bind.BoundObject;
import eunomia.module.receptor.libb.imsCore.bind.SerialObjectBinding;
import eunomia.module.receptor.libb.imsCore.creators.ChannelEndTimeKeyCreator;
import eunomia.module.receptor.libb.imsCore.creators.ChannelEntityKeyCreator;
import eunomia.module.receptor.libb.imsCore.creators.ChannelFlowIDKeyCreator;
import eunomia.module.receptor.libb.imsCore.creators.ChannelProtocolKeyCreator;
import eunomia.module.receptor.libb.imsCore.creators.ChannelStartTimeKeyCreator;
import eunomia.module.receptor.libb.imsCore.creators.DarkAccessSourceKeyCreator;
import eunomia.module.receptor.libb.imsCore.creators.DarkAccessTimeKeyCreator;
import eunomia.module.receptor.libb.imsCore.creators.EntityEndTimeKeyCreator;
import eunomia.module.receptor.libb.imsCore.creators.EntityHostKeyCreator;
import eunomia.module.receptor.libb.imsCore.creators.EntityStartTimeKeyCreator;
import eunomia.module.receptor.libb.imsCore.db.bdb.BDBDataStore;
import eunomia.module.receptor.libb.imsCore.db.sql.SqlRollingDataStore;
import eunomia.module.receptor.libb.imsCore.net.DarkAccess;
import eunomia.module.receptor.libb.imsCore.net.DarkAccessKey;
import eunomia.module.receptor.libb.imsCore.net.NetworkChannel;
import eunomia.module.receptor.libb.imsCore.net.NetworkChannelFlowID;
import eunomia.module.receptor.libb.imsCore.net.NetworkEntryKey;
import eunomia.module.receptor.libb.imsCore.net.NetworkEntity;
import eunomia.module.receptor.libb.imsCore.net.NetworkEntityHostKey;
import java.io.PrintStream;
import java.util.List;
import org.apache.log4j.Logger;

/**
 *
 * @author Mikhail Sosonkin
 */
public class NetEnv implements StoreEnvironment {
    public static final int MAP_ENTITY                   = 0 << 16;
    public static final int MAP_CHANNEL                  = 1 << 16;
    public static final int MAP_DARKSPACE                = 2 << 16;
    public static final int MAP_COUNT                    = 3;
    
    public static final int SEC_HOST_ENTITY              = MAP_ENTITY | 1;
    public static final int SEC_LAST_ACT_TIME_ENTITY     = MAP_ENTITY | 2;
    public static final int SEC_START_TIME_ENTITY        = MAP_ENTITY | 3;
    
    public static final int SEC_FLOW_CHANNEL             = MAP_CHANNEL | 1;
    public static final int SEC_LAST_ACT_TIME_CHANNEL    = MAP_CHANNEL | 2;
    public static final int SEC_START_TIME_CHANNEL       = MAP_CHANNEL | 3;
    public static final int SEC_ENTITY_CHANNEL           = MAP_CHANNEL | 4;
    public static final int SEC_PROTO_CHANNEL            = MAP_CHANNEL | 5;
    
    public static final int SEC_TIME_DARKSPACE           = MAP_DARKSPACE | 1;
    public static final int SEC_ENTITY_DARKSPACE         = MAP_DARKSPACE | 2;
    
    public static final int BIN_NET_ENT_KEY   = 0;
    public static final int BIN_NET_ENT       = 1;
    public static final int BIN_NET_CHAN_KEY  = 2;
    public static final int BIN_NET_CHAN      = 3;
    public static final int BIN_ENT_HOST      = 4;
    public static final int BIN_DS_KEY        = 5;
    public static final int BIN_DS            = 6;
    public static final int BIN_NET_CHAN_ID   = 7;
    public static final int BIN_COUNT       = 8;
    
    private EntryBinding[] bindings;
    private DatabaseStore[] stores;
    private Config miscConfig;
    
    private static Logger logger;
    
    static {
        logger = logger.getLogger(NetEnv.class);
    }
    
    public NetEnv(String dbString) throws Exception {
        bindings = new EntryBinding[BIN_COUNT];
        stores = new DatabaseStore[MAP_COUNT];
        
        openBindings(dbString);
        openDatabases(dbString);
    }
    
    public void close() throws DatabaseException {
        for (int i = 0; i < stores.length; ++i) {
            stores[i].close();
        }
        
        miscConfig.save();
    }
    
    public void extractStatistics(PrintStream out, boolean clear, boolean fast) throws Exception {
        StatsConfig stats = new StatsConfig();
        
        stats.setClear(clear);
        stats.setFast(fast);
        
        /*for (int i = 0; i < MAP_COUNT; i++) {
            Database[] dbs = stores[i].getPrimaryDatabases();
            out.println(i + ": " + new Date(System.currentTimeMillis()));
            
            for (int j = 0; j < dbs.length; j++) {
                out.println(dbs[j].getStats(null, stats));
            }
        }*/
        out.println("-----------------------------------------------------");
    }
    
    private void openBindings(String prefix) throws Exception {
        BoundObject[] samples = new BoundObject[BIN_COUNT];
        samples[BIN_NET_ENT_KEY] = new NetworkEntryKey();
        samples[BIN_NET_ENT] = new NetworkEntity(null);
        samples[BIN_NET_CHAN_KEY] = new NetworkEntryKey();
        samples[BIN_NET_CHAN] = new NetworkChannel(null);
        samples[BIN_ENT_HOST] = new NetworkEntityHostKey();
        samples[BIN_DS_KEY] = new DarkAccessKey();
        samples[BIN_DS] = new DarkAccess(null);
        samples[BIN_NET_CHAN_ID] = new NetworkChannelFlowID();
        
        for(int i = 0; i < BIN_COUNT; i++) {
            bindings[i] = new SerialObjectBinding(samples[i]);
        }
    }
    
    private void openDatabases(String dbString) throws DatabaseException, Exception {
        logger.info("Opening Flow databases.");
        LongBinding longBind = new LongBinding();
        ByteBinding byteBind = new ByteBinding();
        SecondaryKeyCreator creator;
        SecondaryMultiKeyCreator creatorMulti;
        StoreDescriptor[][] descs = new StoreDescriptor[MAP_COUNT][];
        int mapNum;
        
        // Entity Databases.
        mapNum = MAP_ENTITY >> 16;
        descs[mapNum] = new StoreDescriptor[] {
            new StoreDescriptor(dbString + "MAP_ENTITY", null, bindings[BIN_NET_ENT_KEY], bindings[BIN_NET_ENT], true),
            new StoreDescriptor("SEC_HOST_ENTITY", new EntityHostKeyCreator(), bindings[BIN_ENT_HOST], bindings[BIN_NET_ENT], false),
            new StoreDescriptor("SEC_LAST_ACT_TIME_ENTITY", new EntityEndTimeKeyCreator(), longBind, bindings[BIN_NET_ENT], true),
            new StoreDescriptor("SEC_START_TIME_ENTITY", new EntityStartTimeKeyCreator(), longBind, bindings[BIN_NET_ENT], false)
        };
        
        stores[mapNum] = new BDBDataStore("ent", descs[mapNum], 100);
        /*descs[mapNum] = new StoreDescriptor[] {
            new StoreDescriptor(null, bindings[BIN_NET_ENT]),
            new StoreDescriptor(new EntityHostKeyCreator(), bindings[BIN_NET_ENT]),
            new StoreDescriptor(new EntityEndTimeKeyCreator(), bindings[BIN_NET_ENT]),
            new StoreDescriptor(new EntityStartTimeKeyCreator(), bindings[BIN_NET_ENT])
        };
        
        stores[mapNum] = new SqlRollingDataStore("ent", descs[mapNum], 100);*/
        
        // Channels databases.
        mapNum = MAP_CHANNEL >> 16;
        descs[mapNum] = new StoreDescriptor[] {
            new StoreDescriptor(dbString + "MAP_CHANNEL", null, bindings[BIN_NET_CHAN_KEY], bindings[BIN_NET_CHAN], true),
            new StoreDescriptor("SEC_FLOW_CHANNEL", new ChannelFlowIDKeyCreator(), bindings[BIN_NET_CHAN_ID], bindings[BIN_NET_CHAN], false),
            new StoreDescriptor("SEC_LAST_ACT_TIME_CHANNEL", new ChannelEndTimeKeyCreator(), longBind, bindings[BIN_NET_CHAN], true),
            new StoreDescriptor("SEC_START_TIME_CHANNEL", new ChannelStartTimeKeyCreator(), longBind, bindings[BIN_NET_CHAN], false),
            new StoreDescriptor("SEC_ENTITY_CHANNEL", new ChannelEntityKeyCreator(), bindings[BIN_NET_ENT_KEY], bindings[BIN_NET_CHAN], false),
            new StoreDescriptor("SEC_PROTO_CHANNEL", new ChannelProtocolKeyCreator(), byteBind, bindings[BIN_NET_CHAN], false)
        };
        
        stores[mapNum] = new BDBDataStore("chn", descs[mapNum], 100);
        /*descs[mapNum] = new StoreDescriptor[] {
            new StoreDescriptor(null, bindings[BIN_NET_CHAN]),
            new StoreDescriptor(new ChannelFlowIDKeyCreator(), bindings[BIN_NET_CHAN]),
            new StoreDescriptor(new ChannelEndTimeKeyCreator(), bindings[BIN_NET_CHAN]),
            new StoreDescriptor(new ChannelStartTimeKeyCreator(), bindings[BIN_NET_CHAN]),
            new StoreDescriptor(new ChannelEntityKeyCreator(), bindings[BIN_NET_CHAN]),
            new StoreDescriptor(new ChannelProtocolKeyCreator(), bindings[BIN_NET_CHAN])
        };
        
        stores[mapNum] = new SqlRollingDataStore("chn", descs[mapNum], 100);*/

        // Dark space access databases.
        mapNum = MAP_DARKSPACE >> 16;
        descs[mapNum] = new StoreDescriptor[] {
            new StoreDescriptor(dbString + "MAP_DARKSPACE", null, bindings[BIN_DS_KEY], bindings[BIN_DS], true),
            new StoreDescriptor("SEC_TIME_DARKSPACE", new DarkAccessTimeKeyCreator(), longBind, bindings[BIN_DS], true),
            new StoreDescriptor("SEC_ENTITY_DARKSPACE", new DarkAccessSourceKeyCreator(), longBind, bindings[BIN_DS], false)
        };
        
        stores[mapNum] = new RollingDataStore("dark", descs[mapNum], 1, DatabaseType.BTREE);
        
        // Other data, whatever.
        miscConfig = Config.getConfiguration("imsCore.misc");
        
        logger.info("Network state databases opened.");
    }
    
    public void rollDB(int map, String prefix) {
        DatabaseStore s = stores[map >> 16];
        if(s instanceof RollingDatabaseStore) {
            try {
                ((RollingDatabaseStore)s).roll(prefix);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
    
    public void putMisc(String key, long value) {
        miscConfig.setString(key, Long.toString(value));
    }
    
    public long getMisc(String key) {
        String val = miscConfig.getString(key, null);
        
        if(val != null) {
            return Long.valueOf(val);
        }
        
        return 0;
    }
    
    public DataStoredMap getMap(int map, List dbs) {
        return stores[map >> 16].getMap(map & 0x0000FFFF, dbs);
    }
    
    public DataStoredMap getMap(int map) {
        return getMap(map, null);
    }
    
    public void commit(EnvironmentEntry[] ent, int map, int offset, int len) {
        DatabaseStore ds = stores[map >> 16];
        
        try {
            ds.putArray(ent, offset, len);
        } catch (DatabaseException ex) {
            ex.printStackTrace();
        }
    }
}