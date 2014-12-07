/*
 * ReceptorManager.java
 *
 * Created on September 9, 2005, 12:26 PM
 *
 */

package eunomia.managers;

import eunomia.config.Config;
import eunomia.exception.ReceptorManagerConnectInProcess;
import eunomia.flow.*;
import eunomia.managers.connectable.ConnectTuple;
import eunomia.messages.receptor.protocol.ProtocolDescriptor;
import eunomia.receptor.*;
import eunomia.receptor.module.interfaces.FlowModule;
import java.io.*;
import java.util.*;
import org.apache.log4j.Logger;

/**
 *
 * @author Mikhail Sosonkin
 */
public class ReceptorManager implements Runnable {
    private static ReceptorManager instance;
    private static Logger logger;
    
    private Receptor receptor;
    private HashMap nameToServer;
    private Object connectLock;
    private List defaultConnect;

    private ReceptorManager() throws IOException {
        Runtime.getRuntime().addShutdownHook(new Thread(this));
        
        nameToServer = new HashMap();
        connectLock = new Object();
        defaultConnect = new LinkedList();
        load();
        receptor = new Receptor();
    }
    
    public void addDefaultConnect(ConnectTuple tuple){
        defaultConnect.add(tuple);
        addFlowProcessor(tuple);
    }
    
    public void removeDefaultConnect(ConnectTuple tuple) {
        defaultConnect.remove(tuple);
        removeFlowProcessor(tuple);
    }
    
    public void run(){
        try {
            save();
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    public String getReceptorStats(){
        return receptor.getStats();
    }
    
    public void connectServer(FlowServer serv) throws IOException, ReceptorManagerConnectInProcess {
        boolean isConnecting = false;
        synchronized(connectLock){
            isConnecting = serv.isInProcess();
            if(!isConnecting){
                serv.setInProcess(true);
            }
        }
        
        if(isConnecting){
            throw new ReceptorManagerConnectInProcess(serv + " is in connection process");
        }
        
        if(!serv.getProtocol().isActive()){
            try {
                receptor.connect(serv);
            } catch (IOException e){
                serv.setInProcess(false);
                throw e;
            }
        } else {
            serv.setInProcess(false);
        }
    }
    
    public void disconnectServer(FlowServer serv) throws IOException, ReceptorManagerConnectInProcess {
        boolean isConnecting = false;
        synchronized(connectLock){
            isConnecting = serv.isInProcess();
            if(!isConnecting){
                serv.setInProcess(true);
            }
        }
        
        if(isConnecting){
            throw new ReceptorManagerConnectInProcess(serv + " is in connection process");
        }
        
        if(serv.getProtocol().isActive()){
            receptor.disconnect(serv);
        } else {
            serv.setInProcess(false);
        }
    }
    
    public void addFlowProcessor(ConnectTuple tuple){
        FlowServer[] servs = getFlowServers();
        for (int i = 0; i < servs.length; i++) {
            servs[i].addFlowProcessor(tuple);
        }
    }
    
    public void removeFlowProcessor(ConnectTuple tuple){
        FlowServer[] servs = getFlowServers();
        for (int i = 0; i < servs.length; i++) {
            servs[i].removeFlowProcessor(tuple);
        }
    }
    
    public void addServer(FlowServer serv) throws IOException {
        addServer(serv, true);
    }
    
    public void addServer(FlowServer serv, boolean save) throws IOException {
        nameToServer.put(serv.getName(), serv);
        serv.addConnectionListener(ClientManager.v());
        Iterator it = defaultConnect.iterator();
        while (it.hasNext()) {
            ConnectTuple tuple = (ConnectTuple) it.next();
            serv.addFlowProcessor(tuple);
        }
        if(save){
            save();
        }
    }
    
    public FlowServer[] getFlowServers(){
        return (FlowServer[])nameToServer.values().toArray(new FlowServer[]{});
    }
    
    public FlowServer getServerByName(String name){
        return (FlowServer)nameToServer.get(name);
    }
    
    public FlowServer addServer(String fmodName, ProtocolDescriptor protocol, String name) throws IOException {
        FlowModule mod = ModuleManager.v().getFlowModuleInstance(fmodName);
        FlowServer serv = new FlowServer(mod, protocol, name);
        addServer(serv);
        
        return serv;
    }
    
    public void removeServer(FlowServer fServ) throws IOException {
        nameToServer.remove(fServ.getName());
        save();
    }
    
    public static ReceptorManager v(){
        if(instance == null){
            try {
                logger = Logger.getLogger(ReceptorManager.class);
                instance = new ReceptorManager();
            } catch(Exception e){
                e.printStackTrace();
            }
        }
        
        return instance;
    }
    
    public void load() throws IOException {
        Config config = Config.getConfiguration("manager.streams");
        Object[] names = config.getArray("StreamNames", null);
        
        if(names != null){
            String name, modName, protocol, descriptor;
            for(int i = 0; i < names.length; i++){
                name = names[i].toString();
                modName = config.getString(name + "MOD", "NABFlow");
                protocol = config.getString(name + "PRO", "TCP");
                descriptor = config.getString(name + "DSC", null);
                FlowModule mod = ModuleManager.v().getFlowModuleInstance(modName);
                if(mod != null){
                    FlowServer fServ = new FlowServer(mod, ReceiverProtocol.getDescriptorObject(protocol, descriptor), name);
                    addServer(fServ, false);
                }
            }
        }
    }
    
    public void save() throws IOException {
        Config config = Config.getConfiguration("manager.streams");
        FlowServer[] fServs = getFlowServers();
        String[] names = new String[fServs.length];
        for(int i = 0; i < names.length; i++){
            FlowServer serv = fServs[i];
            names[i] = serv.getName();
        }
        config.setArray("StreamNames", names);
        for(int i = 0; i < names.length; i++){
            config.setString(names[i] + "PRO", fServs[i].getProtocol().getProtocolDescriptor().protoString());
            config.setString(names[i] + "DSC", ReceiverProtocol.getDescriptorString(fServs[i].getProtocol().getProtocolDescriptor()));
            config.setString(names[i] + "MOD", ModuleManager.v().getFlowModuleName(fServs[i].getFlowModule()));
        }
        config.save();
    }
}