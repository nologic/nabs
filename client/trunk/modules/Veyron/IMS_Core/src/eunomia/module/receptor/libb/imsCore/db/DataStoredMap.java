/*
 * DS_StoredMap.java
 *
 * Created on March 29, 2008, 7:07 PM
 *
 */

package eunomia.module.receptor.libb.imsCore.db;

import com.sleepycat.collections.StoredEntrySet;
import com.sleepycat.collections.StoredIterator;
import com.sleepycat.collections.StoredMap;
import eunomia.module.receptor.libb.imsCore.iterators.FilteredMultiIterator;
import eunomia.module.receptor.libb.imsCore.iterators.ValuesIterator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 *
 * @author Mikhail Sosonkin
 */
public interface DataStoredMap {
    public int size();
    public FilteredMultiIterator valuesIterator();
    public void remove(Object key);
    public Collection duplicates(Object o);
}
