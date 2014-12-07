/*
 * ReceptorState.java
 *
 * Created on December 9, 2005, 1:36 PM
 *
 */

package eunomia.core.receptor;

import com.vivic.eunomia.sys.frontend.ConsoleReceptorState;
import eunomia.core.data.staticData.DatabaseTerminal;
import eunomia.core.managers.event.state.AddDatabaseEvent;
import eunomia.core.managers.event.state.AddDatabaseTypeEvent;
import eunomia.core.managers.event.state.AddModuleEvent;
import eunomia.core.managers.event.state.AddStreamServerEvent;
import eunomia.core.managers.event.state.ReceptorStateEvent;
import eunomia.core.managers.event.state.ReceptorUserAddedEvent;
import eunomia.core.managers.event.state.ReceptorUserRemovedEvent;
import eunomia.core.managers.event.state.RemoveDatabaseEvent;
import eunomia.core.managers.event.state.RemoveStreamServerEvent;
import eunomia.core.managers.event.state.StreamStatusChangedEvent;
import eunomia.core.managers.listeners.ReceptorStateListener;
import eunomia.messages.DatabaseDescriptor;
import eunomia.messages.receptor.ModuleHandle;
import eunomia.messages.receptor.ncm.ServerConnectionStatusMessage;
import eunomia.messages.receptor.protocol.ProtocolDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;

/**
 *
 * @author Mikhail Sosonkin
 */
public class ReceptorState implements ConsoleReceptorState {
    private List listeners;

    private List servers;
    private List modules;
    private List flowModules;
    private List analModules;
    private List databases;
    private List databaseTypes;
    private List collectors;
    
    //admin
    private List receptorUsers;
    private List receptorModules;
    
    private Map dbToTerm;
    private Receptor receptor;
    
    //events
    private AddDatabaseEvent addDatabaseEvent;
    private RemoveDatabaseEvent removeDatabaseEvent;
    private AddDatabaseTypeEvent addDatabaseTypeEvent;
    private AddModuleEvent addModuleEvent;
    private AddStreamServerEvent addStreamServerEvent;
    private RemoveStreamServerEvent removeStreamServerEvent;
    private StreamStatusChangedEvent streamStatusChangedEvent;
    private ReceptorUserAddedEvent receptorUserAddedEvent;
    private ReceptorUserRemovedEvent receptorUserRemovedEvent;
    
    private Map eventToMethod;
    
    private static Logger logger;
    
    static {
        logger = Logger.getLogger(ReceptorState.class);
    }
    
    public ReceptorState(Receptor rec) {
        receptor = rec;
        dbToTerm = new HashMap();
        databaseTypes = new LinkedList();
        listeners = new LinkedList();
        servers = new LinkedList();
        modules = new LinkedList();
        analModules = new LinkedList();
        databases = new LinkedList();
        flowModules = new LinkedList();
        collectors = new LinkedList();
        receptorUsers = new LinkedList();
        receptorModules = new LinkedList();
        
        addDatabaseEvent = new AddDatabaseEvent(rec);
        removeDatabaseEvent = new RemoveDatabaseEvent(rec);
        addDatabaseTypeEvent = new AddDatabaseTypeEvent(rec);
        addModuleEvent = new AddModuleEvent(rec);
        addStreamServerEvent = new AddStreamServerEvent(rec);
        removeStreamServerEvent = new RemoveStreamServerEvent(rec);
        streamStatusChangedEvent = new StreamStatusChangedEvent(rec);
        receptorUserAddedEvent = new ReceptorUserAddedEvent(rec);
        receptorUserRemovedEvent = new ReceptorUserRemovedEvent(rec);
        
        Class klass = ReceptorStateListener.class;
        eventToMethod = new HashMap();
        try {
            eventToMethod.put(addDatabaseEvent, klass.getMethod("databaseAdded", AddDatabaseEvent.class));
            eventToMethod.put(removeDatabaseEvent, klass.getMethod("databaseRemoved", RemoveDatabaseEvent.class));
            eventToMethod.put(addDatabaseTypeEvent, klass.getMethod("databaseTypeAdded", AddDatabaseTypeEvent.class));
            eventToMethod.put(addModuleEvent, klass.getMethod("moduleAdded", AddModuleEvent.class));
            eventToMethod.put(addStreamServerEvent, klass.getMethod("streamServerAdded", AddStreamServerEvent.class));
            eventToMethod.put(removeStreamServerEvent, klass.getMethod("streamServerRemoved", RemoveStreamServerEvent.class));
            eventToMethod.put(streamStatusChangedEvent, klass.getMethod("streamStatusChanged", StreamStatusChangedEvent.class));
            eventToMethod.put(receptorUserAddedEvent, klass.getMethod("receptorUserAdded", ReceptorUserAddedEvent.class));
            eventToMethod.put(receptorUserRemovedEvent, klass.getMethod("receptorUserRemoved", ReceptorUserRemovedEvent.class));
        } catch (NoSuchMethodException ex) {
            ex.printStackTrace();
        }
    }
    
    public void reset() {
        dbToTerm.clear();
        databaseTypes.clear();
        servers.clear();
        modules.clear();
        analModules.clear();
        databases.clear();
        flowModules.clear();
        collectors.clear();
        receptorUsers.clear();
        receptorModules.clear();
        System.gc();
    }
    
    public void addTerminal(DatabaseTerminal term, String db) {
        dbToTerm.put(db, term);
    }
    
    public DatabaseTerminal getTerminal(String db) {
        return (DatabaseTerminal)dbToTerm.get(db);
    }
    
    public void addReceptorStateListener(ReceptorStateListener l){
        listeners.add(l);
    }
    
    public void removeReceptorStateListener(ReceptorStateListener l){
        listeners.remove(l);
    }
    
    private void fireReceptorStateEvent(ReceptorStateEvent e){
        Method method = (Method)eventToMethod.get(e);
        if(method != null) {
            Iterator it = listeners.iterator();        
            while(it.hasNext()){
                Object l = it.next();
                try {
                    method.invoke(l, e);
                } catch (IllegalArgumentException ex) {
                    ex.printStackTrace();
                } catch (InvocationTargetException ex) {
                    ex.printStackTrace();
                } catch (IllegalAccessException ex) {
                    ex.printStackTrace();
                }
            }
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
    
    public List getCollectors() {
        return Collections.unmodifiableList(collectors);
    }
    
    public void addCollector(String coll) {
        if(!collectors.contains(coll)) {
            collectors.add(coll);
            
            addModuleEvent.setModule(coll);
            addModuleEvent.setType(ModuleHandle.TYPE_COLL);
            fireReceptorStateEvent(addModuleEvent);
        }
    }

    public void addModule(String module){
        if(!modules.contains(module)) {
            modules.add(module);

            addModuleEvent.setModule(module);
            addModuleEvent.setType(ModuleHandle.TYPE_PROC);
            fireReceptorStateEvent(addModuleEvent);
        }
    }
    
    public void addAnalysisModule(String module){
        if(!analModules.contains(module)){
            analModules.add(module);

            addModuleEvent.setModule(module);
            addModuleEvent.setType(ModuleHandle.TYPE_ANLZ);
            fireReceptorStateEvent(addModuleEvent);
        }
    }
    
    public void addFlowModule(String mod){
        if(!flowModules.contains(mod)){
            /*if(receptor.getLinker().getMapping(mod, Descriptor.TYPE_FLOW) == null) {
                receptor.getLinker().downloadFlowModule(mod);
                receptor.getOutComm().getModuleJar(mod, Descriptor.TYPE_FLOW);
            }*/
            flowModules.add(mod);

            addModuleEvent.setModule(mod);
            addModuleEvent.setType(ModuleHandle.TYPE_FLOW);
            fireReceptorStateEvent(addModuleEvent);

        }
    }
    
    public void addDatabaseType(String type){
        if(!databaseTypes.contains(type)){
            databaseTypes.add(type);
            
            addDatabaseTypeEvent.setType(type);
            fireReceptorStateEvent(addDatabaseTypeEvent);
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
            streamStatusChangedEvent.setOldStatus(ssd.isConnected()?ServerConnectionStatusMessage.CONNECTED:ServerConnectionStatusMessage.CLOSED);
            streamStatusChangedEvent.setOldStatus(status);
            streamStatusChangedEvent.setServer(ssd);
                    
            ssd.setConnected(status == ServerConnectionStatusMessage.CONNECTED);
            StringBuilder bf = new StringBuilder();
            bf.append(server);
            bf.append(" status: ");
            bf.append(ServerConnectionStatusMessage.desc[status]);
            logger.info(bf);
            
            fireReceptorStateEvent(streamStatusChangedEvent);
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
        
        addStreamServerEvent.setServer(ssd);
        fireReceptorStateEvent(addStreamServerEvent);
    }
    
    public void removeStreamServer(String name){
        StreamServerDesc ssd = getStreamServer(name);
        servers.remove(ssd);
        
        removeStreamServerEvent.setServer(ssd);
        fireReceptorStateEvent(removeStreamServerEvent);
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
            
            db = dbOld;
        } else {
            databases.add(db);
        }
        
        addDatabaseEvent.setDatabase(db);
        fireReceptorStateEvent(addDatabaseEvent);
    }

    public List getReceptorUsers() {
        return Collections.unmodifiableList(receptorUsers);
    }
    
    public void setUsers(String[] users) {
        for (int i = 0; i < users.length; i++) {
            if(!receptorUsers.contains(users[i])) {
                receptorUsers.add(users[i]);
                
                receptorUserAddedEvent.setUser(users[i]);
                fireReceptorStateEvent(receptorUserAddedEvent);
            }
        }
    }
    
    public void setReceptorModules(List list) {
        receptorModules = list;
    }
    
    public List getReceptorModules() {
        return receptorModules;
    }
}