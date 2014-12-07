/*
 * StartUpSH.java
 *
 * Created on December 27, 2006, 7:48 PM
 *
 */

package eunomia.gui;

import eunomia.core.managers.ModuleManager;
import eunomia.core.managers.ReceptorManager;
import eunomia.core.managers.event.state.module.ModuleAddedEvent;
import eunomia.core.managers.event.state.module.ModuleListChangedEvent;
import eunomia.core.managers.event.state.module.ModuleRemovedEvent;
import eunomia.core.managers.listeners.ModuleManagerListener;
import eunomia.core.managers.listeners.ReceptorManagerListener;
import eunomia.core.receptor.Receptor;
import eunomia.core.receptor.StreamServerDesc;
import eunomia.core.receptor.comm.ReceptorOutComm;
import eunomia.messages.receptor.protocol.impl.TCPProtocol;
import java.beans.PropertyVetoException;
import java.io.FileInputStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.Set;
import org.apache.log4j.Logger;

/**
 *
 * @author Mikhail Sosonkin
 */
public class StartCode implements ReceptorManagerListener, ModuleManagerListener {
    private static Logger logger;
    private static StartCode inst;
    
    private Set done;

    static {
        logger = Logger.getLogger(StartCode.class);
    }
    
    private StartCode() {
        done = new HashSet();
    }
    
    public static void startup() throws Exception {
        Config.load("install.config");
        
        inst = new StartCode();
        
        Iterator it = ReceptorManager.ins.getReceptors().iterator();
        while (it.hasNext()) {
            Receptor r = (Receptor) it.next();
            inst.receptorAdded(r);
        }
        
        ReceptorManager.ins.addReceptorManagerListener(inst);
        
        try {
            if(!Config.getReceptor()) {
                return;
            }
        } catch(Exception e){
            e.printStackTrace();
            return;
        }
        
        if(ReceptorManager.ins.getReceptors().size() == 0){
            ReceptorManager.ins.addReceptor(Config.getReceptorName(), Config.getReceptorIp(), Config.getReceptorPort(), 1500);
        }
    }

    public void receptorAdded(Receptor rec) {
        rec.getManager().addModuleManagerListener(this);
        
        if(rec.getName().equals(Config.getReceptorName())) {
            String username = "root";
            String password = "toor";

            try {
                rec.setCredentials(username, password);
                logger.info(rec + " -> Connecting...");

                rec.connect();
            } catch (Exception e){
                logger.info(e.getMessage());
            }
        }
    }

    public void receptorRemoved(Receptor rec) {
        rec.getManager().removeModuleManagerListener(this);
    }

    public void moduleAdded(ModuleAddedEvent e) {

    }

    public void moduleListChanged(ModuleListChangedEvent e) {
        System.out.println("Hello??");
        Receptor r = e.getReceptor();
        ReceptorOutComm out = r.getOutComm();
        ModuleManager manager = r.getManager();
        
        if(!done.contains(e.getReceptor())) {
            done.add(e.getReceptor());
            try {
                MainGui.v().getRealtimeFrameManager().getReceptorFrame(r).setMaximum(true);
            } catch (PropertyVetoException ex) {
            }
        }
    
        if(manager.getSieveModuleInstanceCount() <= 1) {
            logger.info("Creating Toolbox");
            String[] startup = Config.getStartupModules();
            for (int i = 0; i < startup.length; i++) {
                out.instantiateModule(startup[i]);
            }
        } else {
            logger.info("Openning Toolbox");
        }
        
        try {
            if(r.getName().equals(Config.getReceptorName())) {
                logger.info("Connecting to Sensors");
                if(Config.getSensor()) {
                    if(r.getState().getStreamServers().size() == 0) {
                        TCPProtocol proto = new TCPProtocol();
                        proto.setIp(Config.getSensorIp());
                        proto.setPort(Config.getSensorPort());
                        out.addStream(Config.getSensorName(), Config.getSensorModule(), proto);
                        out.connectStream(Config.getSensorName(), true);
                    } else {
                        StreamServerDesc desc = r.getState().getStreamServer(Config.getSensorName());
                        if(desc != null) {
                            out.connectStream(Config.getSensorName(), true);
                        }
                    }
                }
            }
        } catch (Exception ex){
            ex.printStackTrace();
        }
    }

    public void moduleRemoved(ModuleRemovedEvent e) {
    }
    
    private static class Config {
        private static Properties properties;
        
        public static final String SENSOR = "Sensor";
        public static final String SENSOR_IP = "Sensor.IP";
        public static final String SENSOR_PORT = "Sensor.Port";
        public static final String SENSOR_NAME = "Sensor.Name";
        public static final String SENSOR_MOD = "Sensor.Module";
        public static final String RECEPTOR = "Receptor";
        public static final String RECEPTOR_IP = "Receptor.IP";
        public static final String RECEPTOR_PORT = "Receptor.Port";
        public static final String RECEPTOR_NAME = "Receptor.Name";
        public static final String MODULES = "Modules";
        
        public static void load(String file) {
            properties = new Properties();
            
            try {
                FileInputStream fin = new FileInputStream(file);
                properties.load(fin);
                fin.close();
            } catch (Exception e){
                e.printStackTrace();
            }
        }

        public static boolean getSensor() {
            return getBoolean(SENSOR);
        }
        
        public static String getSensorIp() {
            return getString(SENSOR_IP);
        }
        
        public static int getSensorPort() {
            return getInt(SENSOR_PORT);
        }
        
        public static String getSensorName()  {
            return getString(SENSOR_NAME);
        }
        
        public static String getSensorModule() {
            return getString(SENSOR_MOD);
        }
        
        public static boolean getReceptor() {
            return getBoolean(RECEPTOR);
        }
        
        public static String getReceptorIp() {
            return getString(RECEPTOR_IP);
        }
        
        public static int getReceptorPort() {
            return getInt(RECEPTOR_PORT);
        }
        
        public static String getReceptorName()  {
            return getString(RECEPTOR_NAME);
        }
        
        public static String[] getStartupModules() {
            String[] mods = new String[] {
                "feedBack", "networkPolicy", "networkStatus"
            };

            return mods;
        }

        private static String getString(String prop) {
            if(properties.containsKey(prop)) {
                return (String)properties.getProperty(prop);
            }
            
            throw new NoSuchElementException(prop);
        }
        
        private static int getInt(String prop) {
            String str = getString(prop);
            
            return Integer.parseInt(str);
        }
        
        private static boolean getBoolean(String prop) {
            String str = getString(prop);
            
            return Boolean.valueOf(str);
        }
    }
}