/*
 * Main.java
 *
 * Created on September 21, 2008, 4:00 PM
 *
 */

package bdbTest;

import com.sleepycat.db.Database;
import com.sleepycat.db.DatabaseConfig;
import com.sleepycat.db.DatabaseEntry;
import com.sleepycat.db.DatabaseException;
import com.sleepycat.db.DatabaseType;
import com.sleepycat.db.LockMode;
import com.sleepycat.db.OperationStatus;
import com.sleepycat.db.SecondaryConfig;
import com.sleepycat.db.SecondaryDatabase;
import com.sleepycat.db.SecondaryKeyCreator;
import com.sleepycat.db.SecondaryMultiKeyCreator;
import com.sleepycat.db.SecondaryKeyCreator;
import java.io.FileNotFoundException;
import java.util.Set;

/**
 *
 * @author Mikhail Sosonkin
 */
public class Main {
    private static byte[] data1 = "ABCDEFGHI".getBytes();
    private static byte[] key = "K".getBytes();
    private static byte[] key1 = "K1".getBytes();
    private static byte[] key2 = "K2".getBytes();

    public static void main(String[] args) throws DatabaseException, FileNotFoundException {
        DatabaseConfig dbConfig = new DatabaseConfig();
        
        dbConfig.setAllowCreate(true);
        dbConfig.setType(DatabaseType.HASH);
        //dbConfig.setRecordLength(20);
        Database mainDb = new Database("d_file", null, dbConfig);
        
        SecondaryConfig secondary = new SecondaryConfig();
        secondary.setType(DatabaseType.BTREE);
        secondary.setAllowCreate(true);
        secondary.setSortedDuplicates(true);
        secondary.setMultiKeyCreator(new MultiKeyCreator());
        
        SecondaryDatabase sdb = new SecondaryDatabase("s_file", "s_name", mainDb, secondary);
        
        SecondaryConfig secondary2 = new SecondaryConfig();
        secondary2.setType(DatabaseType.BTREE);
        secondary2.setAllowCreate(true);
        secondary2.setSortedDuplicates(true);
        secondary2.setKeyCreator(new KeyCreator());
        
        SecondaryDatabase sdb2 = new SecondaryDatabase("s_file2", "s_name2", mainDb, secondary2);

        DatabaseEntry entry = new DatabaseEntry(data1);
        DatabaseEntry d_key = new DatabaseEntry(key);
        
        mainDb.put(null, d_key, entry);
        
        sdb.close();
        sdb2.close();
        mainDb.close();
        
        System.out.println("Reopening");
        
        mainDb = new Database("d_file", null, dbConfig);
        sdb = new SecondaryDatabase("s_file", "s_name", mainDb, secondary);
        sdb2 = new SecondaryDatabase("s_file2", "s_name2", mainDb, secondary2);

        DatabaseEntry d_res = new DatabaseEntry();
        DatabaseEntry g_key = new DatabaseEntry();
        
        g_key.setData(key1);
        OperationStatus status = sdb.get(null, g_key, d_res, LockMode.DEFAULT);
        if(status == OperationStatus.SUCCESS) {
            System.out.println("Using Key1 (" + new String(g_key.getData()) + "): " + new String(d_res.getData()));
        } else {
            System.out.println("Op: " + status);
        }
        
        g_key.setData(key2);
        status = sdb.get(null, g_key, d_res, LockMode.DEFAULT);
        if(status == OperationStatus.SUCCESS) {
            System.out.println("Using Key2 (" + new String(g_key.getData()) + "): " + new String(d_res.getData()));
        } else {
            System.out.println("Op: " + status);
        }

        g_key.setData(key1);
        status = sdb2.get(null, g_key, d_res, LockMode.DEFAULT);
        if(status == OperationStatus.SUCCESS) {
            System.out.println("S2 Using Key1 (" + new String(g_key.getData()) + "): " + new String(d_res.getData()));
        } else {
            System.out.println("Op: " + status);
        }

        sdb.close();
        sdb2.close();
        mainDb.close();
    }
    
    public static class MultiKeyCreator implements SecondaryMultiKeyCreator {
        public void createSecondaryKeys(SecondaryDatabase secondary, DatabaseEntry key, DatabaseEntry data, Set results) throws DatabaseException {
            DatabaseEntry k1 = new DatabaseEntry(key1);
            DatabaseEntry k2 = new DatabaseEntry(key2);
            
            System.out.println("Adding keys");
            results.add(k1);
            results.add(k2);
        }
    }
    
    public static class KeyCreator implements SecondaryKeyCreator {
        public boolean createSecondaryKey(SecondaryDatabase secondary, DatabaseEntry key, DatabaseEntry data, DatabaseEntry result) throws DatabaseException {
            result.setData(key1);
            
            System.out.println("Setting key");
            return true;
        }
    }
}