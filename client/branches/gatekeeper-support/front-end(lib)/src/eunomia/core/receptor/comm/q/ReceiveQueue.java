/*
 * ReceiveQueue.java
 *
 * Created on October 11, 2006, 8:36 PM
 *
 */

package eunomia.core.receptor.comm.q;

import eunomia.core.receptor.comm.q.listeners.ReceiveQueueListener;
import eunomia.messages.BlockingMessage;
import eunomia.messages.Message;
import eunomia.util.oo.NabObjectInput;
import java.util.Iterator;
import java.util.LinkedList;
import org.apache.log4j.Logger;

/**
 *
 * @author Mikhail Sosonkin
 */
public class ReceiveQueue implements Runnable {
    private LinkedList queue; // probably should be changed to an array.
    private LinkedList listeners;
    private Thread thread;
    private NabObjectInput nin;
    private BlockingMessage bmsg;
    private boolean tRun;
    private boolean bmsgDispatched;
    
    private static Logger logger;
    
    static {
        logger = Logger.getLogger(SendQueue.class);
    }
    
    public ReceiveQueue(NabObjectInput nin) {
        this.nin = nin;
        queue = new LinkedList();
        listeners = new LinkedList();
        thread = new Thread(this);
        tRun = true;
        bmsgDispatched = false;
        
        thread.setPriority(Thread.MAX_PRIORITY);
        thread.start();
    }
    
    public void setAllowUnknowns(boolean v){
        nin.setAllowUnknowns(v);
    }
    
    public void addReceptorListener(ReceiveQueueListener l){
        listeners.add(l);
    }
    
    public void removeReceptorListener(ReceiveQueueListener l){
        listeners.remove(l);
    }
    
    public Message get(){
        Object msg = null;
        synchronized(queue){
            if(bmsgDispatched) {
                bmsg = null;
                queue.notifyAll();
                bmsgDispatched = false;
            }
            
            while(queue.size() == 0 && bmsg == null){
                try {
                    queue.wait();
                } catch (InterruptedException ex) {
                    return null;
                }
            }
            
            if(bmsg != null) {
                msg = bmsg;
                bmsgDispatched = true;
            } else {
                msg = queue.removeFirst();
            }
        }
        
        return (Message)msg;
    }
    
    public void terminate(){
        tRun = false;
        thread.interrupt();
    }
    
    public void run() {
        while(tRun){
            Object o = null;
            try {
                o = nin.readObject();
            } catch (Exception e){
                tRun = false;
                Iterator it = listeners.iterator();
                while (it.hasNext()) {
                    ReceiveQueueListener l = (ReceiveQueueListener) it.next();
                    l.caughtException(e, this);
                }
            }
            
            synchronized(queue){
                if(o instanceof BlockingMessage) {
                    bmsg = (BlockingMessage)o;
                    queue.notifyAll();
                    while(bmsg != null && tRun) {
                        try {
                            queue.wait();
                        } catch (InterruptedException ex) {
                            //ex.printStackTrace();
                        }
                    }
                } else {
                    queue.addLast(o);
                    queue.notifyAll();
                }
            }
        }
    }
}