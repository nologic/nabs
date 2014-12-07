/*
 * StreamStatus.java
 *
 * Created on June 14, 2005, 7:55 PM
 */

package eunomia.plugin.rec.feedBack;

import eunomia.config.Config;
import com.vivic.eunomia.filter.Filter;
import com.vivic.eunomia.module.flow.Flow;
import com.vivic.eunomia.module.receptor.FlowProcessor;
import com.vivic.eunomia.module.receptor.ReceptorProcessorModule;
import com.vivic.eunomia.module.flow.FlowModule;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


/**
 *
 * @author  Mikhail Sosonkin
 */

public class Main implements ReceptorProcessorModule, FlowProcessor {
    private final static int CMD_SET_NUM = 0;
    private static String CFG_STR = "eunomia.module.feedback";
    
    private int randNum;
    
    public Main() {
        Config conf = Config.getConfiguration(CFG_STR);
        randNum = conf.getInt("rand", -1);
    }
    
    public void setProperty(String name, Object value) {
    }
    
    public Object getProperty(String name) {
        return null;
    }
    
    public void newFlow(Flow flow) {
    }
    
    public Filter getFilter() {
        return null;
    }
    
    public void setFilter(Filter f){
    }

    public FlowProcessor getFlowPocessor(){
        return this;
    }
    
    public void start() {
    }

    public void stop() {
    }

    public void reset() {
    }

    public FlowProcessor getFlowProcessor() {
        return this;
    }

    public void updateStatus(OutputStream out) throws IOException {
        out.write( (randNum >> 24) & 0xFF);
        out.write( (randNum >> 16) & 0xFF);
        out.write( (randNum >> 8) & 0xFF);
        out.write( (randNum ) & 0xFF);
    }
    
    public void setControlData(InputStream in) throws IOException {
    }
    
    public void getControlData(OutputStream out) throws IOException {
    }

    public void destroy() {
    }

    public Object[] getCommands() {
        return null;
    }

    public Object executeCommand(Object command, Object[] parameters) {
        return null;
    }

    public boolean accept(FlowModule module) {
        return true;
    }

    public void processMessage(DataInputStream in, DataOutputStream out) throws IOException {
        switch(in.readInt()) {
            case CMD_SET_NUM: 
                randNum = in.readInt();
                Config conf = Config.getConfiguration(CFG_STR);
                conf.setInt("rand", randNum);
                conf.save();
        }
    }
}