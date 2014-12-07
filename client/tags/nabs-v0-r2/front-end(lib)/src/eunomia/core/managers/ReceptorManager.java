/*
 * ReceptorManager.java
 *
 * Created on October 20, 2005, 8:09 PM
 *
 */

package eunomia.core.managers;

import eunomia.config.Config;
import eunomia.core.managers.listeners.ReceptorManagerListener;
import eunomia.core.receptor.*;
import java.io.IOException;
import java.util.*;
import org.apache.log4j.Logger;

/**
 *
 * @author Mikhail Sosonkin
 */
public class ReceptorManager {
    public static final ReceptorManager ins = new ReceptorManager();
    private static Logger logger;
    
    private HashMap nameToReceptor;
    private List listeners;
    
    static {
        logger = Logger.getLogger(ReceptorManager.class);
    }

    private ReceptorManager() {
        nameToReceptor = new HashMap();
        listeners = new LinkedList();
    }
    
    public void addReceptorManagerListener(ReceptorManagerListener l){
        listeners.add(l);
    }
    
    public void removeReceptorManagerListener(ReceptorManagerListener l){
        listeners.remove(l);
    }
    
    private void fireRemovedEvent(Receptor rec){
        Iterator it = listeners.iterator();
        
        while(it.hasNext()){
            ((ReceptorManagerListener)it.next()).receptorRemoved(rec);
        }
    }
    
    private void fireAddedEvent(Receptor rec){
        Iterator it = listeners.iterator();
        
        while(it.hasNext()){
            ((ReceptorManagerListener)it.next()).receptorAdded(rec);
        }
    }
    
    public List getReceptors(){
        return Arrays.asList(nameToReceptor.values().toArray());
    }
    
    public void addReceptor(Receptor rec){
        nameToReceptor.put(rec.getName(), rec);
        fireAddedEvent(rec);
    }
    
    public void removeReceptor(Receptor rec) throws IOException {
        rec.disconnect();
        nameToReceptor.remove(rec.getName());
        fireRemovedEvent(rec);
    }
    
    public Receptor addDefaultReceptor(String name) throws IOException {
        return addReceptor(name, "127.0.0.1", 4185, 1500);
    }
    
    public Receptor addReceptor(String name, String ip, int port, int refresh) throws IOException {
        if(getByName(name) == null){
            Receptor rec = new Receptor(name);

            rec.setIPPort(ip, port);
            rec.setRefreshRate(refresh);
            addReceptor(rec);
            return rec;
        }
        
        return null;
    }
    
    public Receptor getByName(String name){
        return (Receptor)nameToReceptor.get(name);
    }
    
    public void load() throws IOException {
        Config config = Config.getConfiguration("manager.receptors");
        
        Object[] names = config.getArray("receptors", null);
        if(names != null){
            for(int i = 0; i < names.length; i++){
                try {
                    Receptor rec = new Receptor(names[i].toString(), true);
                    addReceptor(rec);
                } catch(IOException e){
                    logger.error("Error loading Receptor data: " + names[i].toString());
                    e.printStackTrace();
                }
            }
        }
    }
    
    public void save() throws IOException {
        Config config = Config.getConfiguration("manager.receptors");
        
        List receptors = getReceptors();
        config.setArray("receptors", receptors.toArray());
        Iterator it = receptors.iterator();
        while(it.hasNext()){
            ((Receptor)it.next()).save();
        }
        config.save();
    }
}