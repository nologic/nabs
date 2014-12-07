/*
 * FlowServer.java
 *
 * Created on September 9, 2005, 1:49 PM
 *
 */

package eunomia.receptor;

import com.vivic.eunomia.filter.Filter;
import com.vivic.eunomia.module.flow.Flow;
import com.vivic.eunomia.module.receptor.FlowProcessor;
import eunomia.managers.connectable.ConnectTuple;
import eunomia.managers.connectable.ServerLinkListener;
import eunomia.messages.receptor.protocol.ProtocolDescriptor;
import eunomia.receptor.listeners.ConnectionListener;
import com.vivic.eunomia.module.receptor.FlowCreator;
import com.vivic.eunomia.module.flow.FlowModule;
import java.nio.ByteBuffer;
import java.util.*;
import org.apache.log4j.Logger;

/**
 *
 * @author Mikhail Sosonkin
 */
public class FlowServer {
    private FlowCreator flowCreator;
    private ReceiverProtocol protocol;
    private String name;
    private FlowProcessor[] procs;
    private Filter filter;
    private ByteBuffer buffer;
    private FlowModule module;
    private List conListeners;
    private boolean inProcess;
    private int procCount;
    
    private static Logger logger;
    
    static {
        logger = Logger.getLogger(FlowServer.class);
    }
    
    public FlowServer(FlowModule fMod, ProtocolDescriptor desc, String name) {
        procCount = 0;
        conListeners = Collections.synchronizedList(new LinkedList());
        procs = new FlowProcessor[10];
        protocol = new ReceiverProtocol(desc);
        changeFlowModule(fMod);
        
        this.setName(name);
    }
    
    public void addConnectionListener(ConnectionListener con){
        conListeners.add(con);
    }
    
    public void removeConnectionListener(ConnectionListener con){
        conListeners.remove(con);
    }
    
    void fireConnectionDropped(){
        setInProcess(false);
        
        Iterator it = conListeners.iterator();
        while (it.hasNext()) {
            ConnectionListener l = (ConnectionListener) it.next();
            l.connectionDropped(this);
        }
    }
    
    void fireConnectionFailed(){
        setInProcess(false);
        
        Iterator it = conListeners.iterator();
        while (it.hasNext()) {
            ConnectionListener l = (ConnectionListener) it.next();
            l.connectionFailure(this);
        }
    }
    
    void fireConnectionSuccess(){
        setInProcess(false);
        
        Iterator it = conListeners.iterator();
        while (it.hasNext()) {
            ConnectionListener l = (ConnectionListener) it.next();
            l.connectionSuccessful(this);
        }
    }
    
    void fireConnectionClosed(){
        setInProcess(false);
        
        Iterator it = conListeners.iterator();
        while (it.hasNext()) {
            ConnectionListener l = (ConnectionListener) it.next();
            l.connectionClosed(this);
        }
    }
    
    public FlowModule getFlowModule(){
        return module;
    }
    
    public void changeFlowModule(FlowModule mod){
        // do we need more changes for this to work properly?
        if(!protocol.isActive()){
            module = mod;
            flowCreator = module.getNewFlowCreatorInstance();
            buffer = ByteBuffer.allocateDirect(flowCreator.getBufferSize());
        }
    }
    
    public String toString(){
        return name + " (" + getProtocol() + ")";
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ReceiverProtocol getProtocol() {
        return protocol;
    }

    public void setProtocol(ProtocolDescriptor desc) {
        if(!protocol.isActive()){
            protocol = new ReceiverProtocol(desc);
        }
    }
    
    public boolean isInProcess() {
        return inProcess;
    }

    public void setInProcess(boolean inProcess) {
        this.inProcess = inProcess;
    }
    
    public FlowCreator getFlowCreator(){
        return flowCreator;
    }
    
    public void removeFlowProcessor(ConnectTuple tuple){
        if(deregisterProcessor(tuple.getFlowProcessor())) {
            tuple.fireDisconnect(this);
        }
    }
    
    public void addFlowProcessor(ConnectTuple tuple){
        FlowProcessor proc = tuple.getFlowProcessor();
        if(!hasFlowProcessor(proc)) {
            if(proc.accept(module)) {
                registerProcessor(proc);
                tuple.fireConnect(this);
            } else {
                tuple.fireConnectError(this, ServerLinkListener.UNACCEPTABLE_MODULE);
            }
        }
    }
    
    public boolean hasFlowProcessor(FlowProcessor proc) {
        FlowProcessor[] tmp = procs;
        for(int i = tmp.length - 1; i != -1; --i){
            if(tmp[i] == proc){
                return true;
            }
        }
        
        return false;
    }
    
    public void registerProcessor(FlowProcessor fp){
        synchronized(procs){
            FlowProcessor[] tmp = procs;
            for(int i = 0, size = tmp.length; i < size; ++i){
                if(tmp[i] == null){
                    ++procCount;
                    tmp[i] = fp;
                    break;
                }
            }
            
            if(procCount == procs.length){
                FlowProcessor[] newProc = new FlowProcessor[2 * procs.length];
                System.arraycopy(procs, 0, newProc, 0, procs.length);
                procs = newProc;
            }
        }
    }
    
    public boolean deregisterProcessor(FlowProcessor fp){
        boolean retVal = false;
        
        synchronized(procs){
            for(int i = procs.length - 1; i != -1; --i){
                if(procs[i] == fp){
                    --procCount;
                    procs[i] = null;
                    retVal = true;
                    break;
                }
            }
        }
        
        return retVal;
    }
    
    ByteBuffer getByteBuffer(){
        return buffer;
    }
    
    FlowProcessor[] getProcessors(){
        return procs;
    }
    
    void notifyProcessors(LoadBalancer bal, Flow flow){
        Filter f = filter;
        
        if(f != null && !f.allow(flow)){
            return;
        }

        if(bal.notify(procs, procCount, flow)){
            FlowProcessor err = bal.getErrored();
            Throwable thr = bal.getError();
            
            deregisterProcessor(err);
            logger.error("Processor " + err + " produced exceptions while processing flows. Removing from list: " + thr.getMessage());
            thr.printStackTrace();
        }
    }
    
    public Filter getFilter() {
        return filter;
    }

    public void setFilter(Filter filter) {
        this.filter = filter;
    }
}