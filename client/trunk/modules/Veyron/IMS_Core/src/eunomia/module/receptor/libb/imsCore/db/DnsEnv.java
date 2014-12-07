package eunomia.module.receptor.libb.imsCore.db;

import com.sleepycat.bind.EntryBinding;
import com.sleepycat.db.DatabaseException;
import com.sleepycat.db.DatabaseType;
import eunomia.module.receptor.libb.imsCore.EnvironmentEntry;
import eunomia.module.receptor.libb.imsCore.StoreEnvironment;
import eunomia.module.receptor.libb.imsCore.bind.SerialObjectBinding;
import eunomia.module.receptor.libb.imsCore.dns.DNSFlowKey;
import eunomia.module.receptor.libb.imsCore.dns.DNSFlowRecord;
import eunomia.module.receptor.libb.imsCore.iterators.FilteredMultiIterator;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.Collection;
import java.util.List;

/***************************************************
 * TODO RollingDataStore issues  *
 ***************************************************/


/**
 *
 * @author Justin Stallard
 */
public class DnsEnv implements StoreEnvironment {
    
    // primaries
    public static final int PRI_FLOWS         = 0 << 16;
    
    public static final int PRI_COUNT         = 1;
    
    // bindings
    public static final int BIN_FLOW_KEY      = 0;
    public static final int BIN_FLOW_DATA     = 1;
    
    public static final int BIN_COUNT         = 2;
    
    
    private EntryBinding[] bindings;
    private DatabaseStore[] stores;
    
    public DnsEnv(String dbString) throws Exception {
        bindings = new EntryBinding[BIN_COUNT];
        stores = new DatabaseStore[PRI_COUNT];
        
        openBindings();
        openDatabases(dbString);
    }
    
    public void close() throws DatabaseException {
        for (int i = 0; i < stores.length; ++i) {
            stores[i].close();
        }
    }
    
    private void openBindings() {
        bindings[BIN_FLOW_KEY]  = new SerialObjectBinding(new DNSFlowKey());
        bindings[BIN_FLOW_DATA] = new SerialObjectBinding(new DNSFlowRecord(null));
    }
    
    
    private void openDatabases(String dbString) throws DatabaseException, Exception {
        StoreDescriptor[][] descs = new StoreDescriptor[PRI_COUNT][];
                
        // DNS Flow Record database
        descs[PRI_FLOWS >> 16] = new StoreDescriptor[] {
            new StoreDescriptor(dbString + "PRI_FLOWS",
                                null,
                                bindings[BIN_FLOW_KEY],
                                bindings[BIN_FLOW_DATA],
                                true)
        };
        
        System.err.println("DNSEnv: openDatabases: descs.length: " + descs.length);
        for (int i = 0; i < descs.length; i++) {
            stores[i] = new RollingDataStore("dns",
                                             descs[PRI_FLOWS >> 16],
                                             100,
                                             DatabaseType.BTREE);
        }
    }
    
    public void rollDB(int map) {
        try {
            // System.err.println("DNSEnv: rollDB: rolling...");
            ((RollingDataStore) stores[map >> 16]).roll(null);
            // System.err.println("DNSEnv: rollDB: done rolling.");
        } catch (DatabaseException ex) {
            ex.printStackTrace();
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        }
    }
    
    public void deleteEntries(Object key, int map) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void commitEntry(EnvironmentEntry ent, int map) {
        try {
            stores[map >> 16].putArray(new EnvironmentEntry[] {ent}, 0, 1);
        } catch (DatabaseException ex) {
            ex.printStackTrace();
        }
    }
    
    public void commit(EnvironmentEntry[] ent, int map, int offset, int len) {
        try {
            // System.err.println("DNSEnv: commitEntries: inserting " + count + " records...");
            ((RollingDataStore) stores[map >> 16]).putArray(ent, 0, len);
            // System.err.println("DNSEnv: commitEntries: successfully inserted " + count + " records.");
        } catch (DatabaseException ex) {
            ex.printStackTrace();
        }
    }
    
    public DataStoredMap getMap(int map, List dbs) {
        return stores[map >> 16].getMap(map & 0x0000FFFF, dbs);
    }

    public Collection getEntryDups(Object key, int map) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public EnvironmentEntry getEntry(Object key, int map) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public FilteredMultiIterator getValuesIterator(int map) {
        return getMap(map, null).valuesIterator();
    }

    public int getEntryCount(int map) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void extractStatistics(PrintStream out, boolean clear, boolean fast) throws Exception {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}