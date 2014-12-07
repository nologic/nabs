/*
 * Environment.java
 *
 * Created on March 8, 2008, 6:47 PM
 *
 */

package eunomia.module.receptor.libb.imsCore;

import eunomia.module.receptor.libb.imsCore.db.DataStoredMap;
import eunomia.module.receptor.libb.imsCore.iterators.FilteredMultiIterator;
import java.io.PrintStream;
import java.util.Collection;
import java.util.List;

/**
 *
 * @author Mikhail Sosonkin
 */
public interface StoreEnvironment {
    public void commit(EnvironmentEntry[] ent, int map, int offset, int len);
    public DataStoredMap getMap(int map, List dbs);

    public void extractStatistics(PrintStream out, boolean clear, boolean fast) throws Exception;
}