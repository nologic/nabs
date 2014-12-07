/*
 * Main.java
 *
 * Created on April 23, 2008, 11:02 PM
 *
 */

package eunomia.module.receptor.anlz.imsSqlReport;

import com.vivic.eunomia.module.Descriptor;
import com.vivic.eunomia.module.receptor.ReceptorAnalysisModule;
import com.vivic.eunomia.module.receptor.ReceptorProcessorModule;
import com.vivic.eunomia.sys.receptor.SieveContext;
import com.vivic.eunomia.sys.util.Util;
import eunomia.messages.receptor.ModuleHandle;
import eunomia.module.receptor.libb.imsCore.util.ComponentRegistry;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

/**
 *
 * @author Mikhail Sosonkin
 */
public class Main implements ReceptorAnalysisModule {
    private SqlReporter rep;
    
    public Main() {
    }

    public void destroy() {
    }

    public void updateStatus(OutputStream out) throws IOException {
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
        // make reporter.
        rep = new SqlReporter();
        
        ComponentRegistry.getInstance().registerReportingComponent(rep);
    }
}