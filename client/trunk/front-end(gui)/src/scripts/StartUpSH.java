/*
 * StartUpSH.java
 *
 * Created on December 27, 2006, 7:48 PM
 *
 */

package scripts;

import eunomia.core.managers.ModuleManager;
import eunomia.core.managers.ReceptorManager;
import eunomia.core.receptor.Receptor;
import eunomia.core.receptor.comm.ReceptorOutComm;
import eunomia.gui.MainGui;
import eunomia.messages.receptor.protocol.impl.TCPProtocol;
import eunomia.shell.Shell;
import java.io.FileInputStream;
import java.util.NoSuchElementException;
import java.util.Properties;
import org.apache.log4j.Logger;

/**
 *
 * @author Mikhail Sosonkin
 */
public class StartUpSH {
    private static Logger logger;

    static {
        logger = Logger.getLogger(StartUpSH.class);
    }
    
    public static void startup(Shell sh) throws Exception {
        Config.load("install.config");
        
        try {
            if(!Config.getReceptor()) {
                return;
            }
        } catch(Exception e){
            e.printStackTrace();
            return;
        }
        
        String username = "root";
        String password = "toor";
        Receptor r;
        
        if(ReceptorManager.ins.getReceptors().size() == 0){
            r = ReceptorManager.ins.addReceptor(Config.getReceptorName(), Config.getReceptorIp(), Config.getReceptorPort(), 1500);
        } else {
            r = ReceptorManager.ins.getByName(Config.getReceptorName());
        }

        if(r != null) {
            r.setCredentials(username, password);
            logger.info(r + " -> Connecting...");
            try {
                r.connect();
            } catch (Exception e){
                logger.info(e.getMessage());
            }

            while(!r.isAuthenticated() && r.isConnected()) {
                Thread.sleep(200);
            }
            
            if(r.isConnected()) {
                ReceptorOutComm out = r.getOutComm();
                ModuleManager manager = r.getManager();
                
                MainGui.v().getRealtimeFrameManager().getReceptorFrame(r).setMaximum(true);
                
                try {
                    Thread.sleep(2000);
                } catch(Exception e){}

                try {
                    if(Config.getSensor() && r.getState().getStreamServers().size() <= 1) {
                        TCPProtocol proto = new TCPProtocol();
                        proto.setIp(Config.getSensorIp());
                        proto.setPort(Config.getSensorPort());
                        out.addStream(Config.getSensorName(), Config.getSensorModule(), proto);
                        out.connectStream(Config.getSensorName(), true);
                    }
                } catch (Exception e){
                    e.printStackTrace();
                }

                if(manager.getHandlesList().size() == 1) {
                    logger.info("Starting modules");
                    String[] startup = Config.getStartupModules();
                    for (int i = 0; i < startup.length; i++) {
                        out.instantiateModule(startup[i]);
                    }
                }
            } else {
                logger.info(r + " -> Unable to connect");
            }
        }
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
            String mod = getString(MODULES);
            String[] split = mod.split(",");
            for (int i = 0; i < split.length; i++) {
                split[i] = split[i].trim();
            }
            
            return split;
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