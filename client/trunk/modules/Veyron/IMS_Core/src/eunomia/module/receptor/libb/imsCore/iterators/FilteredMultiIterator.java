/*
 * MultiIterator.java
 *
 * Created on January 22, 2008, 9:25 PM
 *
 */

package eunomia.module.receptor.libb.imsCore.iterators;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Mikhail Sosonkin
 */
public class FilteredMultiIterator implements Iterator {
    private Iterator iters;
    private Iterator curIt;
    private IteratorFilter[] filter;
    private Object next;
    
    public FilteredMultiIterator(List its) {
        this(its, new IteratorFilter[]{});
    }
    
    public FilteredMultiIterator(List its, IteratorFilter[] filter) {
        this.filter = filter;
        iters = its.iterator();
        
        if(iters.hasNext()) {
            curIt = (Iterator)iters.next();
        }
        
        next = prepareNext();
    }
    
    public FilteredMultiIterator(List its, IteratorFilter filter) {
        this(its, new IteratorFilter[]{filter});
    }
    
    public FilteredMultiIterator(Iterator its, IteratorFilter[] filter) {
        this.filter = filter;
        curIt = its;
        
        next = prepareNext();
    }
    
    public FilteredMultiIterator(Iterator its, IteratorFilter filter) {
        this(its, new IteratorFilter[]{filter});
    }

    private void closeIterator(Iterator it) {
        if(it == null) {
        } if(it instanceof ValuesIterator) {
            ((ValuesIterator)it).close();
        } else if(it instanceof FilteredMultiIterator) {
            ((FilteredMultiIterator)it).close();
        }
    }
    
    public void close() {
        closeIterator(curIt);
        while(iters.hasNext()) {
            curIt = (Iterator)iters.next();
            closeIterator(curIt);
        }
    }

    public boolean hasNext() {
        return next != null;
    }

    public Object next() {
        Object ret = next;
        
        next = prepareNext();
        
        return ret;
    }
    
    private Object prepareNext() {
        int retry = 4;
        Exception ex = null;
        
        while(retry > 0) {
            try {
                return prepareNext_ex();
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(0);
                System.out.println("Let try that again: " + retry);
                retry--;
                ex = e;
            }
            
            Thread.yield();
        }
        
        ex.printStackTrace();
        throw new RuntimeException("Keep retrying", ex);
    }
    
    private Object prepareNext_ex() {
        Object ret = null;
        
        do {
            if(curIt != null && curIt.hasNext()) {
                ret = curIt.next();
                
                // Is this a good place to do this?
                if(ret instanceof Map.Entry) {
                    ret = ((Map.Entry)ret).getValue();
                }
                
                if(filter == null || filter.length == 0) {
                    //System.out.println(hashCode() + " No filters, returning: " + ret);
                    return ret;
                }
                
                for (int i = 0; i < filter.length; i++) {
                    //System.out.println("Testing filter: " + filter[i]);
                    if(filter[i].allow(ret)) {
                        return ret;
                    }
                }
            } else {
                if(iters != null && iters.hasNext()) {
                    if(curIt instanceof ValuesIterator) {
                        ((ValuesIterator)curIt).close();
                    }
                    
                    curIt = (Iterator)iters.next();
                } else {
                    curIt = null;
                    
                    return null;
                }
            }
        } while(curIt != null);
        
        return ret;
    }

    public void remove() {
        throw new UnsupportedOperationException("We don't support remove in database operations");
    }
}