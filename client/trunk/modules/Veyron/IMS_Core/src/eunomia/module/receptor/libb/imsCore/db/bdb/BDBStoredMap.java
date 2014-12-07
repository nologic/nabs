/*
 * BDBStoredMap.java
 *
 * Created on June 8, 2008, 12:02 PM
 *
 */

package eunomia.module.receptor.libb.imsCore.db.bdb;

import com.sleepycat.collections.StoredMap;
import eunomia.module.receptor.libb.imsCore.db.*;
import eunomia.module.receptor.libb.imsCore.iterators.FilteredMultiIterator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 *
 * @author Mikhail Sosonkin
 */
public class BDBStoredMap implements DataStoredMap {
    private StoredMap[] maps;
    
    public BDBStoredMap(StoredMap[] maps) {
        this.maps = maps;
    }
    
    public int size() {
        int count = 0;
        for (int i = 0; i < maps.length; i++) {
            count += maps[i].size();
        }
        
        return count;
    }
    
    public FilteredMultiIterator valuesIterator() {
        ArrayList its = new ArrayList(maps.length);
        for (int i = 0; i < maps.length; i++) {
            //StoredIterator it = ((StoredEntrySet)maps[i].entrySet()).storedIterator();
            //it.setReadModifyWrite(true);
            
            Iterator it = maps[i].values().iterator();
            
            its.add(it);
        }
        
        return new FilteredMultiIterator(its);
    }
    
    public void remove(Object key){
        for (int i = 0; i < maps.length; i++) {
            maps[i].remove(key);
        }
    }
    
    public Collection duplicates(Object o) {
        Collection[] colls = new Collection[maps.length];
        for (int i = 0; i < colls.length; i++) {
            colls[i] = maps[i].duplicates(o);
        }
        
        return new Dup_Collection(colls);
    }
    
    private class Dup_Collection implements Collection {
        private Collection[] collections;
        
        public Dup_Collection(Collection[] collections) {
            this.collections = collections;
        }
        
        public int size() {
            int size = 0;
            for (int i = 0; i < collections.length; i++) {
                size += collections[i].size();
            }
            
            return size;
        }

        public boolean isEmpty() {
            for (int i = 0; i < collections.length; i++) {
                if(!collections[i].isEmpty()) {
                    return false;
                }
            }
            
            return true;
        }

        public boolean contains(Object o) {
            for (int i = 0; i < collections.length; i++) {
                if(collections[i].contains(o)) {
                    return true;
                }
            }
            
            return false;
        }

        public Iterator iterator() {
            ArrayList its = new ArrayList(maps.length);
            for (int i = 0; i < collections.length; i++) {
                its.add(collections[i].iterator());
            }

            return new FilteredMultiIterator(its);
        }

        public Object[] toArray() {
            throw new UnsupportedOperationException();
        }

        public Object[] toArray(Object[] a) {
            throw new UnsupportedOperationException();
        }

        public boolean add(Object e) {
            throw new UnsupportedOperationException();
        }

        public boolean remove(Object o) {
            throw new UnsupportedOperationException();
        }

        public boolean containsAll(Collection c) {
            throw new UnsupportedOperationException();
        }

        public boolean addAll(Collection c) {
            throw new UnsupportedOperationException();
        }

        public boolean removeAll(Collection c) {
            throw new UnsupportedOperationException();
        }

        public boolean retainAll(Collection c) {
            throw new UnsupportedOperationException();
        }

        public void clear() {
            throw new UnsupportedOperationException();
        }
    }
}
