/*
 * StreamDataSource.java
 *
 * Created on June 7, 2005, 2:51 PM
 */

package eunomia.core.data.streamData;

import eunomia.*;
import eunomia.config.*;
import eunomia.core.data.staticData.*;
import eunomia.core.data.*;
import eunomia.core.charter.*;
import eunomia.core.data.flow.*;
import eunomia.core.data.streamData.client.*;
import eunomia.core.data.streamData.client.listeners.*;
import eunomia.core.managers.*;

import java.io.*;
import java.net.*;
import java.util.*;
import org.apache.log4j.Logger;
import eunomia.util.Util;

/**
 *
 * @author  Mikhail Sosonkin
 */

public class StreamDataSource extends DataSource {
    private static int fileVersion = 0;
    
    private int serial;
    private String name;

    private NabsClient nc;
    private List sources;
    private List databases;
    private Set processors;
    
    private boolean isActive;
    private boolean isCollect;
    private boolean useServer;
    
    private static Logger logger;
    
    static {
        logger = Logger.getLogger(StreamDataSource.class);
    }

    public StreamDataSource(String name) throws Exception {
        serial = Util.getRandomInt(null);
        this.name = name;
        
        nc = new RawNabsClient();
        sources = new LinkedList();
        databases = new LinkedList();
        processors = new HashSet();
        useServer = true;
    }
    
    public StreamDataSource() throws Exception {
        this(null);
    }
    
    public boolean registerRaw(FlowProcessor proc){
        if(processors.contains(proc)){
            return false;
        }
        
        processors.add(proc);
        return nc.registerProcessor(proc);
    }
    
    public boolean deregisterRaw(FlowProcessor proc){
        if(processors.contains(proc)){
            processors.remove(proc);
            return nc.deregisterProcessor(proc);
        }
        
        return false;
    }
    
    public void initiate() throws Exception {
    }
    
    public void terminate() throws Exception {
    }
    
    public void setName(String n){
        name = n;
    }
    
    public String toString(){
        return name;
    }
    
    public List getOtherStreams(){
        return sources;
    }
    
    public boolean getActive(){
        return isActive;
    }
    
    public void setUseServer(boolean b){
        useServer = b;
    }
    
    public boolean getUseServer(){
        return useServer;
    }
    
    public void setActive(boolean b) throws IOException {
        if(useServer){
            if(b){
                nc.connect();
            } else {
                nc.disconnect();
            }
        }
        if(b){
            nc.activate();
        } else {
            nc.deactivate();
        }
        isActive = b;
    }   
    
    public void setCollect(boolean b){
        isCollect = b;
    }
    
    public boolean getCollect(){
        return isCollect;
    }
    
    public void setServer(String ip, int port){
        nc.setServer(ip, port);
    }
    
    public int getPort(){
        return nc.getPort();
    }
    
    public String getIP(){
        return nc.getIP();
    }
    
    public void removeStream(StreamDataSource sds){
        sources.remove(sds);
        sds.deregisterRaw(nc);
    }
    
    public void addStream(StreamDataSource sds){
        sources.add(sds);
        sds.registerRaw(nc);
    }
    
    public void addDatabase(Database db) throws Exception {
        if(!databases.contains(db)){
            if(!registerRaw(db.getCollector())){
                logger.error("Error registering the database collector");
            } else {
                databases.add(db);
            }
        }
    }
    
    public void removeDatabase(Database db) throws Exception {
        if(!deregisterRaw(db.getCollector())){
            logger.error("Error deregistering the database collector");
        } else {
            databases.remove(db);
        }
    }
    
    public List getDatabaseList(){
        return Collections.unmodifiableList(databases);
    }
    
    public int getSerial(){
        return serial;
    }
    
    public void load() throws IOException {
        Config config = Config.getConfiguration("streams." + name);
        
        useServer = config.getBoolean("useServer", true);
        serial = config.getInt("Serial", -1);
        String ip = config.getString("IP", "127.0.0.1");
        int port = config.getInt("Port", -1);
        Object[] sers = config.getArray("sources", null);
        Object[] db_sers = config.getArray("databases", null);
        
        nc.setServer(ip, port);
        sources = new LinkedList();
        if(sers != null){
            for(int i = 0; i < sers.length; i++){
                try {
                    int ser = Integer.parseInt(sers[i].toString());
                    StreamManager.ins.addRequestForStream(this, ser);
                } catch(Exception e){
                    e.printStackTrace();
                }
            }
        }

        if(db_sers != null){
            databases = new LinkedList();
            for(int i = 0; i < db_sers.length; i++){
                try {
                    int ser = Integer.parseInt(db_sers[i].toString());
                    DatabaseManager.ins.putRequestForDB(this, ser);
                } catch(Exception e){
                    e.printStackTrace();
                }
            }
        }
    }
    
    public void save() throws IOException {
        Config config = Config.getConfiguration("streams." + name);
        
        String[] sers = new String[sources.size()];
        int i = 0;
        Iterator it = sources.iterator();
        while(it.hasNext()){
            StreamDataSource sds = (StreamDataSource)it.next();
            sers[i++] = sds.getSerial() + "";
        }

        String[] db_sers = new String[databases.size()];
        i = 0;
        it = databases.iterator();
        while(it.hasNext()){
            Database db = (Database)it.next();
            db_sers[i++] = db.getSerial() + "";
        }

        config.setInt("Version", fileVersion);
        config.setString("name", name);
        config.setInt("Serial", serial);
        config.setString("IP", nc.getIP());
        config.setInt("Port", nc.getPort());
        config.setArray("sources", sers);
        config.setArray("databases", db_sers);
        config.setBoolean("useServer", useServer);
        config.save();
    }
}