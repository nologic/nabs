/*
 * EunomiaArrayList.java
 *
 * Created on January 13, 2008, 2:06 PM
 *
 */

package eunomia.util;

import java.util.ArrayList;
import java.util.Iterator;

/**
 *
 * @author Mikhail Sosonkin
 */
public class EunomiaArrayList extends ArrayList {
    
    public EunomiaArrayList() {
    }
    
    public Iterator iterator() {
        return new ModifiableListIterator(this);
    }
}