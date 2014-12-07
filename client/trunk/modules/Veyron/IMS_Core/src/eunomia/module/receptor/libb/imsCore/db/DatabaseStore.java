/*
 * DatabaseStore.java
 *
 * Created on May 10, 2008, 1:58 PM
 *
 */

package eunomia.module.receptor.libb.imsCore.db;

import com.sleepycat.db.DatabaseException;
import eunomia.module.receptor.libb.imsCore.EnvironmentEntry;
import java.util.List;

/**
 *
 * @author Mikhail Sosonkin
 */
public interface DatabaseStore {
    public void close() throws DatabaseException;
    
    public void putArray(EnvironmentEntry[] arr, int offset, int length) throws DatabaseException;
    public Object getEntry(Object key);
    
    public DataStoredMap getMap(int map, List dbs);
    
    public List getDatabases();
}