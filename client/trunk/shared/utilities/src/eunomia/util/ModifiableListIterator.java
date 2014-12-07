/*
 * ModifiableListIterator.java
 *
 * Created on January 13, 2008, 2:12 PM
 *
 */

package eunomia.util;

import java.util.Iterator;
import java.util.List;

/**
 *
 * @author Mikhail Sosonkin
 */
public class ModifiableListIterator implements Iterator {
    private List list;
    private int index;
    private Object curObject;
    
    public ModifiableListIterator(List list) {
        this.list = list;
        index = 0;
    }

    public boolean hasNext() {
        return index < list.size();
    }

    public Object next() {
        curObject = null;
        
        try {
            curObject = list.get(index++);
        } catch (Exception e) {
        }
        
        return curObject;
    }

    public void remove() {
        list.remove(curObject);
        index--;
    }
}