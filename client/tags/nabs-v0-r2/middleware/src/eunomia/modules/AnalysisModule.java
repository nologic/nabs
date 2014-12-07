/*
 * AnalysisModule.java
 *
 * Created on November 27, 2006, 6:26 PM
 *
 */

package eunomia.modules;

import eunomia.data.Database;
import eunomia.messages.receptor.ModuleHandle;
import eunomia.plugin.interfaces.StaticAnalysisModule;
import java.io.DataInputStream;
import java.io.DataOutputStream;

/**
 *
 * @author Mikhail Sosonkin
 */
public class AnalysisModule implements StaticAnalysisModule {
    private StaticAnalysisModule module;
    private String moduleName;
    private ModuleHandle handle;
    
    public AnalysisModule(StaticAnalysisModule mod, ModuleHandle h, String modName) {
        module = mod;
        moduleName = modName;
        handle = h;
    }

    public double getProgress() {
        return module.getProgress();
    }

    public void beginAnalysis(DataInputStream arg, Database db) {
        module.beginAnalysis(arg, db);
    }

    public void getArguments(DataOutputStream dout) {
        module.getArguments(dout);
    }

    public void getResult(DataOutputStream dout) {
        module.getResult(dout);
    }

    public StaticAnalysisModule getModule() {
        return module;
    }

    public void setModule(StaticAnalysisModule module) {
        this.module = module;
    }

    public ModuleHandle getHandle() {
        return handle;
    }

    public void setHandle(ModuleHandle handle) {
        this.handle = handle;
    }

    public String getModuleName() {
        return moduleName;
    }
}