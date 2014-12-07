/*
 * ModuleWrap.java
 *
 * Created on January 18, 2006, 1:21 PM
 *
 */

package eunomia.modules;

import eunomia.flow.FlowProcessor;
import eunomia.managers.connectable.ConnectTuple;
import eunomia.managers.connectable.ServerLinkListener;
import eunomia.messages.Message;
import eunomia.messages.module.ModuleMessage;
import eunomia.messages.receptor.ModuleHandle;
import eunomia.plugin.interfaces.ReceptorModule;
import eunomia.receptor.FlowServer;
import java.io.*;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Mikhail Sosonkin
 */
public class FlowProcessorModule implements ReceptorModule, ServerLinkListener {
    private ReceptorModule module;
    private ModuleHandle handle;
    private List streams;
    private ConnectTuple tuple;
    
    public FlowProcessorModule(ReceptorModule mod, ModuleHandle hdl) {
        module = mod;
        handle = hdl;
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

    public ReceptorModule getModule() {
        return module;
    }

    public ModuleHandle getHandle() {
        return handle;
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
    
    public Message processMessage(ModuleMessage msg) throws IOException {
        synchronized(module){
            return module.processMessage(msg);
        }
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

    public void initialize() {
    }

    public void destroy() {
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
}