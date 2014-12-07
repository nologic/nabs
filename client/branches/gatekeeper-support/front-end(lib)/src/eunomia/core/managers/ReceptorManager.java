/*
 * ReceptorManager.java
 *
 * Created on October 20, 2005, 8:09 PM
 *
 */

package eunomia.core.managers;

import eunomia.config.Config;
import eunomia.core.managers.listeners.ReceptorManagerListener;
import eunomia.core.receptor.Receptor;
import eunomia.util.Util;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
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
    
    private int getNewSerial() {
        int num = Util.getRandomIntEx(null);
        Iterator it = nameToReceptor.values().iterator();
        while (it.hasNext()) {
            Receptor r = (Receptor) it.next();
            if(r.getSerialNumber() == num) {
                return getNewSerial();
            }
        }
        
        return num;
    }
    
    public Receptor addDefaultReceptor(String name) throws IOException {
        return addReceptor(name, "127.0.0.1", 4185, 1500);
    }
    
    public Receptor addReceptor(String name, String ip, int port, int refresh) throws IOException {
        if(getByName(name) == null){
            Receptor rec = new Receptor(getNewSerial());

            rec.setName(name);
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
    
    public void load() {
        Config config = Config.getConfiguration("manager.receptors");
        
        Object[] serials = config.getArray("receptors", null);
        if(serials != null){
            for(int i = 0; i < serials.length; i++){
                try {
                    Receptor rec = new Receptor(Integer.parseInt(serials[i].toString()));

                    rec.load();
                    if(rec.getName() != null) {
                        addReceptor(rec);
                    }
                } catch(Exception e) {
                }
            }
        }
    }
    
    public void save() {
        Config config = Config.getConfiguration("manager.receptors");
        
        List receptors = getReceptors();
        String[] serials = new String[receptors.size()];
        for (int i = 0; i < serials.length; i++) {
            Receptor rec = (Receptor)receptors.get(i);
            rec.save();
            
            serials[i] = Integer.toString(rec.getSerialNumber());
        }
        
        config.setArray("receptors", serials);
        config.save();
    }
    
    public static ReceptorManager v() {
        return ins;
    }
}