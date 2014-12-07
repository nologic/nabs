/*
 * AnalysisThread.java
 *
 * Created on November 24, 2006, 3:03 PM
 *
 */

package eunomia.data;

import eunomia.managers.DatabaseManager;
import eunomia.messages.ByteArrayMessage;
import eunomia.module.AnlzMiddlewareModule;
import java.io.DataInputStream;
import java.util.List;

/**
 *
 * @author Mikhail Sosonkin
 */
public class AnalysisThread extends Thread {
    private long startTime;
    private long endTime;
    private AnlzMiddlewareModule module;
    
    public AnalysisThread(ThreadGroup group, AnlzMiddlewareModule mod) {
        super(group, mod.getClass().getName());
        
        startTime = endTime = 0;
        module = mod;
    }
    
    public boolean isWorking() {
        if(startTime != 0 && endTime == 0){
            return true;
        }
        
        return false;
    }

    public void run() {
        startTime = System.currentTimeMillis();
        module.threadMain();
        endTime = System.currentTimeMillis();
    }

    public AnlzMiddlewareModule getModule() {
        return module;
    }

    public void setModule(AnlzMiddlewareModule module) {
        this.module = module;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getEndTime() {
        return endTime;
    }
}
