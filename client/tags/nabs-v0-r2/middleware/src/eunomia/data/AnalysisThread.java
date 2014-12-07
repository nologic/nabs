/*
 * AnalysisThread.java
 *
 * Created on November 24, 2006, 3:03 PM
 *
 */

package eunomia.data;

import eunomia.messages.ByteArrayMessage;
import eunomia.modules.AnalysisModule;
import eunomia.plugin.interfaces.StaticAnalysisModule;
import java.io.DataInputStream;

/**
 *
 * @author Mikhail Sosonkin
 */
public class AnalysisThread extends Thread {
    private AnalysisModule module;
    private ByteArrayMessage arguments;
    private Database database;
    
    public AnalysisThread(ThreadGroup group, AnalysisModule mod, ByteArrayMessage arg, Database db) {
        super(group, mod.getClass().getName());
        db.addAnalysisThread(this);
        module = mod;
        arguments = arg;
        database = db;
    }

    public void run() {
        module.beginAnalysis(new DataInputStream(arguments.getInputStream()), database);
    }

    public AnalysisModule getModule() {
        return module;
    }

    public void setModule(AnalysisModule module) {
        this.module = module;
    }

    public ByteArrayMessage getArguments() {
        return arguments;
    }

    public void setArguments(ByteArrayMessage arguments) {
        this.arguments = arguments;
    }

    public Database getDatabase() {
        return database;
    }

    public void setDatabase(Database database) {
        this.database = database;
    }
    
}
