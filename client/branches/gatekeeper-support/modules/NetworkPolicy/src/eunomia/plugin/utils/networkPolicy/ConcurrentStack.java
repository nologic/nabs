/*
 * ConcurrentStack.java
 *
 * Created on December 28, 2006, 3:00 PM
 *
 */

package eunomia.plugin.utils.networkPolicy;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 *
 * @author kulesh
 */
public class ConcurrentStack {
    static class Node{
        final Object item;
        Node next;
        
        public Node(Object item){this.item = item;}
    }
    
    private AtomicReference head = new AtomicReference();
    private AtomicInteger count = new AtomicInteger(0);
    
    /** Creates a new instance of ConcurrentStack */
    public ConcurrentStack() {
    }
    
    public void push(Object item){
        Node oldHead;
        Node newHead = new Node(item);
        
        do {
            oldHead = (Node)head.get();
            newHead.next= oldHead;
        } while(!head.compareAndSet(oldHead, newHead));
        
        count.incrementAndGet();
    }
    
    public int getSize() {
        return count.get();
    }
    
    public Object pop(){
        Node newHead;
        Node oldHead;
        
        do {
            if((oldHead = (Node)head.get()) == null)
                return null;
            
            newHead = oldHead.next;
        } while(!head.compareAndSet(oldHead, newHead));
        
        count.decrementAndGet();
        
        return oldHead.item;
    }
}