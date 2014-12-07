/*
 * LoadBalancer.java
 *
 * Created on January 6, 2007, 1:50 PM
 *
 */

package eunomia.receptor;

import com.vivic.eunomia.module.Flow;
import com.vivic.eunomia.module.receptor.FlowProcessor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * @author Mikhail Sosonkin
 */
public class LoadBalancer {
    private FlowProcessor[] processors;
    private int processorCount;
    private AtomicInteger doneCount;
    private boolean dontProc;
    
    private AtomicBoolean hasError;
    private FlowProcessor errored;
    private Throwable error;
    
    private Load[] loads;
    private Thread recThread;
    private AtomicInteger nextProc;
    
    public LoadBalancer() {
        hasError = new AtomicBoolean(false);
        nextProc = new AtomicInteger(0);
        doneCount = new AtomicInteger(0);
        dontProc = true;
        
        int num_procs = 0;
        String processors = System.getenv("NUMBER_OF_PROCESSORS");
        if(processors != null){
            try {
                num_procs = Integer.parseInt(processors) - 1;
            } catch (Exception e){
            }
        }
        // disable load balancing:
        num_procs = 0;
        loads = new Load[num_procs];
        for (int i = 0; i < loads.length; i++) {
            loads[i] = new Load();
        }
    }
    
    public void setReceptorThread(Thread thread) {
        recThread = thread;
    }
      
    /**
     * 
     * @param proc 
     * @param procCount 
     * @param flow 
     * @return true is there is an error.
     */
    public boolean notify(FlowProcessor[] proc, int procCount, Flow flow) {
        FlowProcessor cProc = null;// = proc[0];
        Load[] lds = loads;
        for (int i = 0, size = lds.length; i < size;) {
            lds[i++].setFlow(flow);
        }

        processorCount = procCount;
        processors = proc;
        
        nextProc.set(0);
        doneCount.set(0);

        dontProc = false;
        
        while(doneCount.get() < procCount) {
            if((cProc = doneGetNext(cProc)) != null) {
                try {
                    cProc.newFlow(flow);
                } catch (Throwable t){
                    errored(cProc, t);
                }
            }
        }
        
        dontProc = true;

        return hasError.get();
    }
    
    public FlowProcessor doneGetNext(FlowProcessor done) {
        if(dontProc || doneCount.get() >= processorCount) {
            return null;
        }

        if(done == null){
            return getNext();
        }

        if(processorCount == doneCount.incrementAndGet()) {
            return null;
        }

        return getNext();
    }
    
    private FlowProcessor getNext() {
        FlowProcessor[] procs = processors;
        if(nextProc.get() >= procs.length) {
            return null;
        }
        
        int ret = nextProc.getAndIncrement();

        if(ret < procs.length){
            return procs[ret];
        }
        
        return null;
    }
    
    public void errored(FlowProcessor proc, Throwable err){
        if(hasError.compareAndSet(false, true)){
            errored = proc;
            error = err;
        }
    }
    
    public FlowProcessor getErrored(){
        FlowProcessor tmp = errored;
        errored = null;
        hasError.compareAndSet(true, false);
        
        return tmp;
    }
    
    public Throwable getError() {
        Throwable thr = error;
        error = null;
        
        return thr;
    }
    
    private class Load implements Runnable {
        private Flow curFlow;
        private Thread thread;
        
        public Load() {
            thread = new Thread(this, "FlowProcessor_Load_Thread");
            thread.setPriority(Thread.MIN_PRIORITY);
            thread.start();
        }
        
        public void setFlow(Flow flow) {
            curFlow = flow;
        }
        
        public void run() {
            FlowProcessor proc = null;
            while(true) {
                if((proc = doneGetNext(proc)) == null){
                    continue;
                }

                try {
                    proc.newFlow(curFlow);
                } catch (Throwable t){
                    errored(proc, t);
                }
            }
        }
    }
}