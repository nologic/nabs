/*
 * ReadDatabase.java
 *
 * Created on September 21, 2008, 5:49 PM
 *
 */

package eunomia.module.receptor.libb.imsCore.db.bdb;

import com.sleepycat.collections.StoredMap;
import com.sleepycat.db.Database;
import com.sleepycat.db.DatabaseConfig;
import com.sleepycat.db.DatabaseException;
import com.sleepycat.db.SecondaryConfig;
import com.sleepycat.db.SecondaryDatabase;
import com.vivic.eunomia.sys.receptor.SieveContext;
import eunomia.module.receptor.libb.imsCore.db.StoreDescriptor;
import java.io.File;
import java.io.FileNotFoundException;

/**
 *
 * @author Mikhail Sosonkin
 */
public class ReadDatabase {
    private Database primary;
    private Database[] secondaries;
    private String file_prefix;
    private StoredMap[] maps;
        
    public ReadDatabase(StoreDescriptor[] desc, DatabaseConfig[] configs, String namePrefix) throws DatabaseException, FileNotFoundException {
        String dir = SieveContext.getModuleProperty("imsCore", BDBDataStore.CFG_DB_DIR);
        file_prefix = namePrefix;
        
        secondaries = new SecondaryDatabase[desc.length - 1];
        maps = new StoredMap[desc.length - 1];
        
        for (int i = 0; i < desc.length; ++i) {
            StoreDescriptor d = desc[i];

            String name = d.getName();
            String file = dir + File.separator + file_prefix + "_" + name;
            
            if(i == 0) {
                primary = new Database(file, name, configs[i]);
            } else {
                secondaries[i - 1] = new SecondaryDatabase(file, name, primary, (SecondaryConfig)configs[i]);
                
                maps[i] = new StoredMap(secondaries[i - 1], d.getKeyBinding(), d.getValueBinding(), false);
            }
        }
    }
    
    public StoredMap getMap(int map) {
        return maps[map];
    }
    
    public void close() throws DatabaseException {
        for (int i = 0; i < secondaries.length; ++i) {
              secondaries[i].close();
        }

        primary.close();
    }
}