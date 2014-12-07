/*
 * StreamManager.java
 *
 * Created on June 15, 2005, 4:26 PM
 */

package eunomia.core.managers;

import java.util.*;
import java.io.*;

import eunomia.config.*;
import eunomia.core.data.*;
import eunomia.core.data.streamData.*;
import eunomia.core.data.streamData.client.listeners.*;
import eunomia.core.managers.listeners.*;

import org.apache.log4j.*;

/**
 *
 * @author  Mikhail Sosonkin
 */
public class StreamManager {
    public static final StreamManager ins = new StreamManager();
    
    private List streams;
    private List listeners;
    private List defaultProcs;
    private HashMap afterLoadMap;
    private HashMap serialToStream;
    
    private static Logger logger;
    
    static {
        logger = Logger.getLogger(StreamManager.class);
    }
    
    private StreamManager() {
        streams = new LinkedList();
        listeners = new LinkedList();
        defaultProcs = new LinkedList();
        serialToStream = new HashMap();
    }
    
    public void addDefaultProcs(FlowProcessor proc){
        if(proc != null){
            defaultProcs.add(proc);
        }
    }
    
    public void removeDefaultProcs(FlowProcessor proc){
        defaultProcs.remove(proc);
    }
    
    public void addStreamManagerListener(StreamManagerListener l){
        listeners.add(l);
    }
    
    public void removeStreamManagerListener(StreamManagerListener l){
        listeners.remove(l);
    }
    
    private void fireRemovedEvent(StreamDataSource sds){
        Iterator it = listeners.iterator();
        
        while(it.hasNext()){
            ((StreamManagerListener)it.next()).streamRemoved(sds);
        }
    }
    
    private void fireAddedEvent(StreamDataSource sds){
        Iterator it = listeners.iterator();
        
        while(it.hasNext()){
            ((StreamManagerListener)it.next()).streamAdded(sds);
        }
    }

    public List getStreamList(){
        return Collections.unmodifiableList(streams);
    }
    
    public void removeStream(StreamDataSource stream){
        Iterator it = streams.iterator();
        while(it.hasNext()){
            StreamDataSource sds = (StreamDataSource)it.next();
            sds.removeStream(stream);
        }
        
        streams.remove(stream);
        fireRemovedEvent(stream);
    }
    
    public void addStream(StreamDataSource stream){
        addStreamNoFire(stream);
        fireAddedEvent(stream);
    }
    
    private void addStreamNoFire(StreamDataSource stream){
        streams.add(stream);
        serialToStream.put(new Integer(stream.getSerial()), stream);
    }
    
    private void addDefault(StreamDataSource sds){
        Iterator it = defaultProcs.iterator();
        while(it.hasNext()){
            FlowProcessor proc = (FlowProcessor)it.next();
            sds.registerRaw(proc);
        }
    }

    public StreamDataSource createDefaultStream(String name) throws Exception {
        StreamDataSource sds = new StreamDataSource(name);
        addStream(sds);
        addDefault(sds);
        
        return sds;
    }
    
    public StreamDataSource getStream(int serial){
        return (StreamDataSource)serialToStream.get(new Integer(serial));
    }
    
    public void saveStreams() throws IOException {
        Config config = Config.getConfiguration("manager.streams");
        
        config.setArray("streams", streams.toArray());
                
        Iterator it = streams.iterator();
        while(it.hasNext()){
            DataSource ds = (DataSource)it.next();
            ds.save();
        }
        
        config.save();
    }
    
    public void loadStreams() throws Exception {
        Config config = Config.getConfiguration("manager.streams");
        afterLoadMap = new HashMap();
        Object[] names = config.getArray("streams", null);
        
        if(names == null){
            return;
        }
        
        for(int i = 0; i < names.length; i++){
            StreamDataSource sds = new StreamDataSource(names[i].toString());
            sds.load();
            sds.initiate();
            addStreamNoFire(sds);
            addDefault(sds);
        }
        
        Iterator it = afterLoadMap.keySet().iterator();
        while(it.hasNext()){
            StreamDataSource sds = (StreamDataSource)it.next();
            List sList = (List)afterLoadMap.get(sds);
            
            Iterator addIt = sList.iterator();
            while(addIt.hasNext()){
                Integer serial = (Integer)addIt.next();
                StreamDataSource addStream = getStream(serial.intValue());
                if(addStream != null){
                    sds.addStream(addStream);
                } else {
                    logger.warn("Unable to find source (" + serial + ") for " + sds);
                }
            }
        }
        
        afterLoadMap = null;
    }
    
    public void addRequestForStream(StreamDataSource sds, int serial){
        if(afterLoadMap.containsKey(sds)){
            List sList = (List)afterLoadMap.get(sds);
            sList.add(new Integer(serial));
        } else {
            List sList = new LinkedList();
            sList.add(new Integer(serial));
            afterLoadMap.put(sds, sList);
        }
    }
}