/*
 * ValuesIterator.java
 *
 * Created on January 22, 2008, 10:40 PM
 *
 */

package eunomia.module.receptor.libb.imsCore.iterators;

import com.sleepycat.collections.StoredIterator;
import java.util.Iterator;
import java.util.Map;

/**
 *
 * @author Mikhail Sosonkin
 */
public class ValuesIterator implements Iterator {
    private StoredIterator iter;
    
    public ValuesIterator(StoredIterator it) {
        iter = it;
    }

    public boolean hasNext() {
        return iter.hasNext();
    }

    public Object next() {
        Map.Entry entry = (Map.Entry)iter.next();
        
        return entry.getValue();
    }

    public void remove() {
        iter.remove();
    }
    
    public void close() {
        iter.close();
    }
}