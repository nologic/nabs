/*
 * ModuleWrap.java
 *
 * Created on January 18, 2006, 1:21 PM
 *
 */

package eunomia.module;

import com.vivic.eunomia.module.receptor.FlowProcessor;
import eunomia.managers.connectable.ConnectTuple;
import eunomia.managers.connectable.ServerLinkListener;
import eunomia.messages.receptor.ModuleHandle;
import com.vivic.eunomia.module.receptor.ReceptorModule;
import eunomia.receptor.FlowServer;
import java.io.*;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.apache.log4j.Logger;

/**
 *
 * @author Mikhail Sosonkin
 */
public class ProcMiddlewareModule extends MiddlewareModule implements ReceptorModule, ServerLinkListener {
    private ReceptorModule module;
    private List streams;
    private ConnectTuple tuple;
    
    private static Logger logger;
    static {
        logger = Logger.getLogger(ProcMiddlewareModule.class);
    }
    
    public ProcMiddlewareModule(ModuleHandle hdl, ReceptorModule mod) {
        super(hdl, mod);
        
        module = mod;
        streams = new LinkedList();
        tuple = new ConnectTuple();
        tuple.setFlowProcessor(getFlowProcessor());
        tuple.addServerLinkListener(this);
    }
    
    public void addFlowServer(FlowServer serv){
        streams.add(serv);
    }
    
    public void removeFlowServer(FlowServer serv){
        streams.remove(serv);
    }
    
    public String[] getFlowServerList(){
        String[] list = new String[streams.size()];
        Iterator it = streams.iterator();
        for(int i = 0; it.hasNext(); ++i){
            list[i] = ((FlowServer)it.next()).getName();
        }
        
        return list;
    }
    
    public ConnectTuple getConnectTuple(){
        return tuple;
    }

    // Module wrapper functions.
    public FlowProcessor getFlowProcessor(){
        return module.getFlowProcessor();
    }
    
    public void updateStatus(OutputStream out) throws IOException {
        synchronized(module){
            module.updateStatus(out);
        }
    }
    
    public void setControlData(InputStream in) throws IOException {
        synchronized(module){
            module.setControlData(in);
        }
    }
    
    public void getControlData(OutputStream out) throws IOException {
        synchronized(module){
            module.getControlData(out);
        }
    }

    public void processMessage(DataInputStream in, DataOutputStream out) throws IOException {
        module.processMessage(in, out);
    }
    
    public void start() {
        module.start();
    }
    
    public void stop() {
        module.stop();
    }
    
    public void reset() {
        module.reset();
    }
    
    public void setProperty(String name, Object value) {
        module.setProperty(name, value);
    }
    
    public Object getProperty(String name) {
        return module.getProperty(name);
    }

    public void destroy() {
        module.destroy();
    }

    public Object[] getCommands() {
        return module.getCommands();
    }

    public Object executeCommand(Object command, Object[] parameters) {
        return module.executeCommand(command, parameters);
    }

    public void connectedTo(FlowServer serv) {
        streams.add(serv);
    }

    public void disconnectedFrom(FlowServer serv) {
        streams.remove(serv);
    }

    public void connectError(FlowServer serv, int err) {
        logger.info("Connect error: " + this.getHandle() + " to " + serv + " error " + err);
    }
}