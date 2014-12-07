/*
 * BDBDataStore.java
 *
 * Created on September 21, 2008, 5:41 PM
 *
 */

package eunomia.module.receptor.libb.imsCore.db.bdb;

import com.sleepycat.collections.StoredMap;
import com.sleepycat.db.DatabaseConfig;
import com.sleepycat.db.DatabaseException;
import com.sleepycat.db.DatabaseType;
import com.sleepycat.db.SecondaryConfig;
import com.sleepycat.db.SecondaryMultiKeyCreator;
import com.sleepycat.db.SecondaryKeyCreator;
import eunomia.module.receptor.libb.imsCore.EnvironmentEntry;
import eunomia.module.receptor.libb.imsCore.db.DataStoredMap;
import eunomia.module.receptor.libb.imsCore.db.DatabaseStore;
import eunomia.module.receptor.libb.imsCore.db.RollingDatabaseStore;
import eunomia.module.receptor.libb.imsCore.db.StoreDescriptor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

/**
 *
 * @author Mikhail Sosonkin
 */
public class BDBDataStore implements RollingDatabaseStore, DatabaseStore {
    public static final String CFG_DB_DIR = "dbDir";
    
    private DatabaseConfig[] configs;
    private StoreDescriptor[] descs;
    private List prefixes;
    private Map prefixToReadable;
    private WriteDatabase curWrite;
    private ReentrantLock mapLock;
    
    public BDBDataStore(String name, StoreDescriptor[] descriptors, int db_count) {
        mapLock = new ReentrantLock();
        
        descs = descriptors;
        configs = makeConfigs(descriptors);
        
        prefixes = new ArrayList();
        prefixToReadable = new HashMap();
    }

    public void roll(String prefix) throws Exception {
        mapLock.lock();
        
        System.out.println("Rolling to " + prefix);
        if(curWrite != null) {
            curWrite.close();
            
            String pref = curWrite.getPrefix();
            ReadDatabase newRead = new ReadDatabase(descs, configs, pref);
            prefixToReadable.put(prefix, newRead);
        }
        
        curWrite = new WriteDatabase(descs, configs, prefix);
        
        mapLock.unlock();
    }

    public void close() throws DatabaseException {
        Iterator it = prefixToReadable.values().iterator();
        while (it.hasNext()) {
            ReadDatabase rd = (ReadDatabase) it.next();
            rd.close();
        }
        
        curWrite.close();
    }

    public void putArray(EnvironmentEntry[] arr, int offset, int length) throws DatabaseException {
        if(curWrite == null) {
            throw new RuntimeException("You forgot to do the initial roll. Do a roll with initial prefix before inserting data");
        }
                    
        curWrite.putArray(arr, offset, length);
    }

    public Object getEntry(Object key) {
        throw new UnsupportedOperationException();
    }

    public DataStoredMap getMap(int map, List dbs) {
        mapLock.lock();
        
        StoredMap[] smaps = null;
        
        if(dbs != null) {
            List maps = new LinkedList();

            Iterator it = dbs.iterator();
            while (it.hasNext()) {
                Object prefix = it.next();

                maps.add(prefixToReadable.get(prefix));
            }

            smaps = new StoredMap[maps.size()];
            it = maps.iterator();
            for (int i = 0; i < smaps.length; ++i) {
                smaps[i] = ((ReadDatabase)it.next()).getMap(map);
            }
        } else {
            smaps = new StoredMap[prefixToReadable.size()];
            Iterator it = prefixToReadable.values().iterator();
            for (int i = 0; i < smaps.length; ++i) {
                smaps[i] = ((ReadDatabase)it.next()).getMap(map);
            }
        }
        
        mapLock.unlock();
        
        return new BDBStoredMap(smaps);
    }

    public List getDatabases() {
        return prefixes;
    }
    
    private DatabaseConfig[] makeConfigs(StoreDescriptor[] descriptors) {
        DatabaseConfig[] dcfg = new DatabaseConfig[descriptors.length];
        
        for (int i = 0; i < dcfg.length; ++i) {
            StoreDescriptor d = descriptors[i];
            DatabaseConfig tmp = null;
            
            if(i == 0) {
                // main
                tmp = new DatabaseConfig();
                tmp.setAllowCreate(true);
                tmp.setType(DatabaseType.HASH);
            } else {
                SecondaryConfig s_tmp = new SecondaryConfig();
                s_tmp.setAllowCreate(true);
                s_tmp.setType(DatabaseType.BTREE);
                s_tmp.setSortedDuplicates(true);
                
                Object creator = d.getKeyCreator();
                if(creator instanceof SecondaryMultiKeyCreator) {
                    s_tmp.setMultiKeyCreator((SecondaryMultiKeyCreator)creator);
                } else {
                    s_tmp.setKeyCreator((SecondaryKeyCreator)creator);
                }
                
                tmp = s_tmp;
            }
            
            dcfg[i] = tmp;
        }
        
        return dcfg;
    }
}
