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
import eunomia.plugin.interfaces.StaticAnalysisModule;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Mikhail Sosonkin
 */
public class AnlzMiddlewareModule extends MiddlewareModule implements StaticAnalysisModule {
    private StaticAnalysisModule module;
    private AnalysisThread thread;
    private List databases;
    
    public AnlzMiddlewareModule(ModuleHandle h, EunomiaModule mod) {
        super(h, mod);
        databases = new LinkedList();
        handle = h;
        
        thread = new AnalysisThread(DatabaseManager.v().getThreadGroup(), this);
    }

    public void destroy() {
        module.destroy();
    }

    public double getProgress() {
        return module.getProgress();
    }

    public void beginAnalysis() {
        module.beginAnalysis();
    }

    public void getArguments(DataOutputStream dout) {
        module.getArguments(dout);
    }

    public void getResult(DataOutputStream dout) {
        module.getResult(dout);
    }

    public void setAndCheckParameters(DataInputStream in, List dbs) throws Exception {
        databases = dbs;
        module.setAndCheckParameters(in, dbs);
    }

    public AnalysisThread getThread() {
        return thread;
    }

    public void setThread(AnalysisThread thread) {
        this.thread = thread;
    }

    public List getDatabases() {
        return databases;
    }
}