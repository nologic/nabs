/*
 * ReceptorState.java
 *
 * Created on December 9, 2005, 1:36 PM
 *
 */

package eunomia.core.receptor;

import eunomia.core.managers.listeners.*;
import eunomia.messages.*;
import eunomia.messages.receptor.msg.rsp.*;
import eunomia.messages.receptor.ncm.ServerConnectionStatusMessage;
import eunomia.messages.receptor.protocol.ProtocolDescriptor;
import eunomia.plugin.GUIPlugin;
import java.util.*;
import org.apache.log4j.Logger;

/**
 *
 * @author Mikhail Sosonkin
 */
public class ReceptorState {
    private List listeners;
    private List servers;
    private List modules;
    private List flowModules;
    private List analModules;
    private List databases;
    private List databaseTypes;
    private Receptor receptor;
    
    private static Logger logger;
    
    static {
        logger = Logger.getLogger(ReceptorState.class);
    }
    
    public ReceptorState(Receptor rec) {
        receptor = rec;
        databaseTypes = new LinkedList();
        listeners = new LinkedList();
        servers = new LinkedList();
        modules = new LinkedList();
        analModules = new LinkedList();
        databases = new LinkedList();
        flowModules = new LinkedList();
    }
    
    public void addReceptorStateListener(ReceptorStateListener l){
        listeners.add(l);
    }
    
    public void removeReceptorStateListener(ReceptorStateListener l){
        listeners.remove(l);
    }
    
    public void fireReceptorStateChanged(){
        Iterator it = listeners.iterator();
        
        while(it.hasNext()){
            ((ReceptorStateListener)it.next()).receptorStateChanged();
        }
    }
    
    public List getStreamServers(){
        return Collections.unmodifiableList(servers);
    }
    
    public List getModules(){
        return Collections.unmodifiableList(modules);
    }
    
    public List getFlowModules() {
        return Collections.unmodifiableList(flowModules);
    }
    
    public List getDatabases(){
        return Collections.unmodifiableList(databases);
    }
    
    public List getAnalysisModules(){
        return Collections.unmodifiableList(analModules);
    }
    
    public List getDatabaseTypes() {
        return Collections.unmodifiableList(databaseTypes);
    }
    
    public void addStreamServer(StatusMessage.StreamServer ss){
        servers.add(new StreamServerDesc(ss));
    }
    
    public void addModule(String module){
        modules.add(module);
    }
    
    public void addAnalysisModule(String module){
        if(!analModules.contains(module)){
            analModules.add(module);
        }
    }
    
    public void addDatabaseType(String type){
        if(!databaseTypes.contains(type)){
            databaseTypes.add(type);
        }
    }
    
    public void addFlowModule(String mod){
        if(!flowModules.contains(mod)){
            flowModules.add(mod);
        }
    }
    
    public StreamServerDesc getStreamServer(String name){
        Iterator it = servers.iterator();
        
        while(it.hasNext()){
            StreamServerDesc ssd = (StreamServerDesc)it.next();
            if(ssd.getName().equals(name)){
                return ssd;
            }
        }
        
        return null;
    }
    
    public DatabaseDescriptor getDatabaseDescriptor(String name) {
        Iterator it = databases.iterator();
        
        while (it.hasNext()) {
            DatabaseDescriptor db = (DatabaseDescriptor) it.next();
            if(db.getName().equals(name)){
                return db;
            }
        }
        return null;
    }
    
    public void setStreamServerStatus(String server, int status){
        StreamServerDesc ssd = getStreamServer(server);
        if(ssd != null){
            ssd.setConnected(status == ServerConnectionStatusMessage.CONNECTED);
            StringBuilder bf = new StringBuilder();
            bf.append(server);
            bf.append(" status: ");
            bf.append(ServerConnectionStatusMessage.desc[status]);
            logger.info(bf);
            fireReceptorStateChanged();
        }
    }
    
    public void addStreamServer(String name, String modName, ProtocolDescriptor pDesc, boolean isConnected) {
        StreamServerDesc ssd = getStreamServer(name);
        if(ssd == null){
            ssd = new StreamServerDesc();
            ssd.setName(name);
            ssd.setModName(modName);
            ssd.setProtocol(pDesc);
            ssd.setConnected(isConnected);
            servers.add(ssd);
        } else {
            ssd.setName(name);
            ssd.setModName(modName);
            ssd.setProtocol(pDesc);
            ssd.setConnected(isConnected);
        }
    }
    
    public void removeStreamServer(String name){
        StreamServerDesc ssd = getStreamServer(name);
        servers.remove(ssd);
    }
    
    public void addDatabase(DatabaseDescriptor db){
        DatabaseDescriptor dbOld = getDatabaseDescriptor(db.getName());
        if(dbOld != null){
            dbOld.setAddress(db.getAddress());
            dbOld.setDbName(db.getDbName());
            dbOld.setDbType(db.getDbType());
            dbOld.setPort(db.getPort());
            dbOld.setTableName(db.getTableName());
            dbOld.setUsername(db.getUsername());
        } else {
            databases.add(db);
        }
    }
}