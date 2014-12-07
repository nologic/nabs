/*
 * LargeTransferSendThread.java
 *
 * Created on February 14, 2007, 1:38 PM
 *
 */

package eunomia.util.oo;

import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
//import java.util.concurrent.ConcurrentSkipListSet;

/**
 *
 * @author Mikhail Sosonkin
 */
public class LargeTransferSendThread implements Runnable {
    private Thread thread;
    //private ConcurrentSkipListSet states; // only for 1.6, waiting for the MAC people!
    private List states;
    private NabObjectOutput nout;
    private boolean notClosed;
    
    public LargeTransferSendThread(NabObjectOutput out) {
        //states = new ConcurrentSkipListSet();
        states = new LinkedList();
        nout = out;
        notClosed = true;
        thread = new Thread(this);
        thread.start();
    }
    
    public void close() {
        notClosed = true;
    }
    
    public void addLargeTransfer(LargeTransfer lt) {
        synchronized(states){
            states.add(new LargeTransferState(lt, null));
        }
        //states.add(new LargeTransferState(lt, null));
    }

    public void run() {
        while(notClosed) {
            if(states.size() == 0) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                    //ex.printStackTrace();
                }
                continue;
            }
            Object[] list = null;
            synchronized(states){
                list = states.toArray();
            }
            //Iterator it = states.iterator();
            Iterator it = Arrays.asList(list).iterator();
            while(it.hasNext()) {
                LargeTransferState lts = (LargeTransferState)it.next();
                try {
                    if(lts.isWritePortionReady()) {
                        nout.writeObject(lts);
                        if(lts.isWriteDone()) {
                            states.remove(lts);
                        }
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }
}