/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package eunomia.module.receptor.proc.NeoDB;

import com.vivic.eunomia.filter.Filter;
import com.vivic.eunomia.module.flow.Flow;
import com.vivic.eunomia.module.flow.FlowModule;
import com.vivic.eunomia.module.receptor.FlowProcessor;
import com.vivic.eunomia.module.receptor.ReceptorProcessorModule;
import eunomia.module.receptor.proc.NeoDB.database.NeoflowDatabase;
import eunomia.receptor.module.NEOFlow.NEOFlow;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 *
 * @author justin
 */
public class Main implements FlowProcessor, ReceptorProcessorModule {
    private boolean doProc;
    private NeoflowDatabase db;
    private long flowCount;
    private long startTime;
    
    public Main() {
        doProc = false;
        db = new NeoflowDatabase();
        if (!db.open()) {
            System.out.println("NeoDB: ERROR: Unable to open NeoflowDatabase!");
        }
    }

    public void setFilter(Filter filter) {
    }

    public Filter getFilter() {
        return null;
    }

    public void newFlow(Flow flow) {
        if (!(doProc && flow instanceof NEOFlow)) {
            return;
        }
        
        NEOFlow neo = (NEOFlow) flow;
        
        if (neo != null) {
            db.add(neo);
            ++flowCount;
        }
    }

    public boolean accept(FlowModule module) {
        return module.getNewFlowInstance() instanceof NEOFlow;
    }

    public void destroy() {
        db.close();
    }

    public FlowProcessor getFlowProcessor() {
        return this;
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
        doProc = true;
        flowCount = 0;
        startTime = System.currentTimeMillis();
        System.out.println("NeoDB: Starting processing");
    }

    public void stop() {
        doProc = false;
        System.out.println("NeoDB: Stopping processing");
        long curTime = System.currentTimeMillis();
        System.out.println("NeoDB Status: " + flowCount + " flows inserted in " + (double) (curTime - startTime) / 1000 + " seconds");
        System.out.println("NeoDB Status: Average flow insertion rate: " +
                           ((double) flowCount) / (((double) (System.currentTimeMillis() - startTime)) / 1000));
    }

    public void reset() {
        /*
        long beginTime = System.currentTimeMillis();
        long endTime;
        long i = 0;
        NEOFlow f = new NEOFlow();
        for (Object o : db.getValues()) {
            f.takeFrom((NEOFlow) o);
            // f.toString();
            ++i;
        }
        endTime = System.currentTimeMillis();
        System.out.println("NeoDB Status: Iterated through " + i + " flows " +
                           "in " + ((double) (endTime - beginTime)) / 1000 +
                           " seconds");
        */
        long curTime = System.currentTimeMillis();
        System.out.println("NeoDB Status: " + flowCount + " flows inserted in " + (double) (curTime - startTime) / 1000 + " seconds");
        System.out.println("NeoDB Status: Average flow insertion rate: " +
                           ((double) flowCount) / (((double) (System.currentTimeMillis() - startTime)) / 1000));
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
