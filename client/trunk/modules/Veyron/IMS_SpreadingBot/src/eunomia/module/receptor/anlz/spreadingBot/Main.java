package eunomia.module.receptor.anlz.spreadingBot;

import com.vivic.eunomia.module.receptor.ReceptorAnalysisModule;
import com.vivic.eunomia.module.receptor.ReceptorProcessorModule;
import com.vivic.eunomia.sys.receptor.SieveContext;
import com.vivic.eunomia.sys.receptor.SieveModuleManager;
import eunomia.Descriptor;
import eunomia.module.common.anlz.spreadingBot.BotInstance;
import com.vivic.eunomia.sys.util.Util;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import org.apache.log4j.Logger;

/**
 *
 * @author Mikhail Sosonkin
 */
public class Main implements ReceptorAnalysisModule {
    private static Logger logger;
    
    private SpreadingBotTracker tracker;
    
    static {
        logger = Logger.getLogger(Main.class);
    }
    
    public Main() {
        tracker = new SpreadingBotTracker();
    }
    
    public void destroy() {
    }

    public void updateStatus(OutputStream out) throws IOException {
        System.out.println("---- BOTS ----");
        Iterator it = tracker.getBots().iterator();
        while (it.hasNext()) {
            BotInstance b = (BotInstance) it.next();
            System.out.println("Bot: " + Util.ipToString(b.getIP()) + " mailer: " + 
                    (b.getMailer() != null) + " CC: " + b.getControlChannels().size() + " DS: " + b.getDarkspaceAccesses().size());
        }
    }

    public void setControlData(InputStream in) throws IOException {
    }

    public void getControlData(OutputStream out) throws IOException {
    }

    public void processMessage(DataInputStream in, DataOutputStream out) throws IOException {
    }

    public void setProperty(String name, Object value) {
    }

    public Object getProperty(String name) {
        return null;
    }

    public Object[] getCommands() {
        return null;
    }

    public Object executeCommand(Object command, Object[] parameters) {
        return null;
    }

    public void threadMain() {
        SieveModuleManager man = SieveContext.getModuleManager();
        try {
            ReceptorProcessorModule ds = (ReceptorProcessorModule)man.getInstanceEnsure("darkspace", Descriptor.TYPE_PROC);
            ReceptorProcessorModule cc = (ReceptorProcessorModule)man.getInstanceEnsure("ccChannels", Descriptor.TYPE_PROC);
            ReceptorProcessorModule sp = (ReceptorProcessorModule)man.getInstanceEnsure("spammer", Descriptor.TYPE_PROC);
            
            eunomia.module.receptor.proc.ccChannels.Main channels = (eunomia.module.receptor.proc.ccChannels.Main) man.unwrap(cc);
            eunomia.module.receptor.proc.darkspace.Main darkspace = (eunomia.module.receptor.proc.darkspace.Main) man.unwrap(ds);
            eunomia.module.receptor.proc.spammer.Main spammer = (eunomia.module.receptor.proc.spammer.Main) man.unwrap(sp);
            
            channels.addControlChannelListener(tracker);
            darkspace.addDarkspaceListener(tracker);
            spammer.addMailTrackerLintener(tracker);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}