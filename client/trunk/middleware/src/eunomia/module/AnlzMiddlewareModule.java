/*
 * AnalysisModule.java
 *
 * Created on November 27, 2006, 6:26 PM
 *
 */

package eunomia.module;

import eunomia.data.AnalysisThread;
import eunomia.managers.DatabaseManager;
import eunomia.messages.receptor.ModuleHandle;
import com.vivic.eunomia.module.EunomiaModule;
import com.vivic.eunomia.module.receptor.ReceptorAnalysisModule;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Mikhail Sosonkin
 */
public class AnlzMiddlewareModule extends MiddlewareModule implements ReceptorAnalysisModule, ReportingModule {
    private ReceptorAnalysisModule amod;
    private AnalysisThread thread;
    private List databases;
    
    public AnlzMiddlewareModule(ModuleHandle h, EunomiaModule mod) {
        super(h, mod);
        databases = new LinkedList();
        handle = h;
        amod = (ReceptorAnalysisModule)mod;
        
        thread = new AnalysisThread(DatabaseManager.v().getThreadGroup(), this);
    }

    public void destroy() {
        amod.destroy();
    }

    public AnalysisThread getThread() {
        return thread;
    }

    public void setThread(AnalysisThread thread) {
        this.thread = thread;
    }

    public void updateStatus(OutputStream out) throws IOException {
        amod.updateStatus(out);
    }

    public void setControlData(InputStream in) throws IOException {
        amod.setControlData(in);
    }

    public void getControlData(OutputStream out) throws IOException {
        amod.getControlData(out);
    }

    public void processMessage(DataInputStream in, DataOutputStream out) throws IOException {
        amod.processMessage(in, out);
    }

    public void setProperty(String name, Object value) {
        amod.setProperty(name, value);
    }

    public Object getProperty(String name) {
        return amod.getProperty(name);
    }

    public Object[] getCommands() {
        return amod.getCommands();
    }

    public Object executeCommand(Object command, Object[] parameters) {
        return amod.executeCommand(command, parameters);
    }

    public void threadMain() {
        amod.threadMain();
    }
}