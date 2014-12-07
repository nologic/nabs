/*
 * SendQueue.java
 *
 * Created on October 11, 2006, 8:35 PM
 */

package eunomia.core.receptor.comm.q;

import eunomia.core.receptor.comm.q.listeners.SendQueueListener;
import eunomia.messages.Message;
import eunomia.util.oo.NabObjectOutput;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import org.apache.log4j.Logger;

/**
 *
 * @author Mikhail Sosonkin
 */
public class SendQueue implements Runnable {
    private LinkedList queue; // probably should be changed to an array.
    private LinkedList listeners;
    private Thread thread;
    private NabObjectOutput nout;
    private boolean tRun;
    
    private static Logger logger;
    
    static {
        logger = Logger.getLogger(SendQueue.class);
    }
    
    public SendQueue(NabObjectOutput nout) {
        this.nout = nout;
        queue = new LinkedList();
        listeners = new LinkedList();
        thread = new Thread(this);
        tRun = true;
                
        thread.setPriority(Thread.MAX_PRIORITY);
        thread.start();
    }
    
    public void addReceptorListener(SendQueueListener l){
        listeners.add(l);
    }
    
    public void removeReceptorListener(SendQueueListener l){
        listeners.remove(l);
    }
    
    public void put(Message msg){
        synchronized(queue){
            queue.addLast(msg);
            queue.notifyAll();
        }
    }
    
    public void terminate(){
        tRun = false;
        thread.interrupt();
    }
    
    public void run() {
        while(tRun){
            Object o = null;
            synchronized(queue){
                if(queue.size() == 0){
                    try {
                        queue.wait();
                    } catch (InterruptedException ex) {
                        continue;
                    }
                }
                
                o = queue.removeFirst();
            }
            
            sendMessage(o);
        }
    }
    
    private void sendMessage(Object o){
        try {
            nout.writeObject(o);
        } catch (IOException ex) {
            Iterator it = listeners.iterator();
            while (it.hasNext()) {
                SendQueueListener l = (SendQueueListener) it.next();
                l.caughtException(ex, this);
            }
        }
    }
}