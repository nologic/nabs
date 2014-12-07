/*
 * WriteDatabase.java
 *
 * Created on September 21, 2008, 5:49 PM
 *
 */

package eunomia.module.receptor.libb.imsCore.db.bdb;

import com.sleepycat.bind.EntryBinding;
import com.sleepycat.db.Database;
import com.sleepycat.db.DatabaseConfig;
import com.sleepycat.db.DatabaseEntry;
import com.sleepycat.db.DatabaseException;
import com.sleepycat.db.SecondaryConfig;
import com.sleepycat.db.SecondaryDatabase;
import com.vivic.eunomia.sys.receptor.SieveContext;
import eunomia.module.receptor.libb.imsCore.EnvironmentEntry;
import eunomia.module.receptor.libb.imsCore.EnvironmentKey;
import eunomia.module.receptor.libb.imsCore.db.StoreDescriptor;
import java.io.File;
import java.io.FileNotFoundException;

/**
 *
 * @author Mikhail Sosonkin
 */
public class WriteDatabase {
    private String file_prefix;
    private Database primary;
    private SecondaryDatabase[] secondaries;
    
    private DatabaseEntry entryKey;
    private DatabaseEntry entryValue;
    private EntryBinding primaryKeyBinding;
    private EntryBinding primaryValueBinding;
    
    public WriteDatabase(StoreDescriptor[] desc, DatabaseConfig[] configs, String namePrefix) throws DatabaseException, FileNotFoundException {
        String dir = SieveContext.getModuleProperty("imsCore", BDBDataStore.CFG_DB_DIR);
        file_prefix = namePrefix;
        secondaries = new SecondaryDatabase[desc.length - 1];
        
        for (int i = 0; i < desc.length; ++i) {
            StoreDescriptor d = desc[i];

            String name = d.getName();
            String file = dir + File.separator + file_prefix + "_" + name;
            
            if(i == 0) {
                primaryKeyBinding = d.getKeyBinding();
                primaryValueBinding = d.getValueBinding();

                primary = new Database(file, name, configs[i]);
            } else {
                secondaries[i - 1] = new SecondaryDatabase(file, name, primary, (SecondaryConfig)configs[i]);
            }
        }
    }
    
    public void putArray(EnvironmentEntry[] arr, int offset, int length) throws DatabaseException {
        Database dbase = primary;

        for (int i = 0; i < length; i++) {
            EnvironmentEntry ent = arr[offset + i];
            EnvironmentKey key = ent.getKey();

            primaryKeyBinding.objectToEntry(key, entryKey);
            primaryValueBinding.objectToEntry(ent, entryValue);

            dbase.put(null, entryKey, entryValue);
        }
    }
    
    public void close() throws DatabaseException {
        for (int i = 0; i < secondaries.length; ++i) {
            secondaries[i].close();
        }
        
        primary.close();
    }
    
    public String getPrefix() {
        return file_prefix;
    }
}