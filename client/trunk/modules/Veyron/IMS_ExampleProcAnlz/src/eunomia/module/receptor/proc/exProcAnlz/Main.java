/*
 * Main.java
 *
 * Created on February 11, 2008, 8:35 PM
 *
 */

package eunomia.module.receptor.proc.exProcAnlz;

import com.vivic.eunomia.module.EunomiaModule;
import com.vivic.eunomia.module.receptor.FlowProcessor;
import com.vivic.eunomia.module.receptor.ReceptorProcessorModule;
import com.vivic.eunomia.sys.receptor.SieveContext;
import eunomia.Descriptor;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 *
 * @author Mikhail Sosonkin
 */
public class Main implements ReceptorProcessorModule {
    private ProcComponent pc;
    private AnlzComponent ac;
    
    public Main() {
        pc = new ProcComponent();
        ac = new AnlzComponent();
        
        EunomiaModule mod = null;
        try {
            mod = SieveContext.getModuleManager().getInstanceEnsure("bootIms", Descriptor.TYPE_ANLZ);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        eunomia.module.receptor.anlz.bootIms.Main main = (eunomia.module.receptor.anlz.bootIms.Main)SieveContext.getModuleManager().unwrap(mod);
        
        main.registerAnalysisComponent(ac, 10000, 1000);
        main.registerProcessingComponent(pc);
    }

    public void destroy() {
    }

    public FlowProcessor getFlowProcessor() {
        return pc;
    }

    public void updateStatus(OutputStream out) throws IOException {
    }

    public void setControlData(InputStream in) throws IOException {
    }

    public void getControlData(OutputStream out) throws IOException {
    }

    public void processMessage(DataInputStream in, DataOutputStream out) throws IOException {
    }

    public void start() {
    }

    public void stop() {
    }

    public void reset() {
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
}