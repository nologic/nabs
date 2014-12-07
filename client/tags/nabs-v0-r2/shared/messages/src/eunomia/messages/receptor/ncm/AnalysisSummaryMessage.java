/*
 * AnalysisSummaryMessage.java
 *
 * Created on November 27, 2006, 5:12 PM
 *
 */

package eunomia.messages.receptor.ncm;

import eunomia.messages.Message;
import eunomia.messages.receptor.ModuleHandle;
import eunomia.messages.receptor.NoCauseMessage;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Mikhail Sosonkin
 */
public class AnalysisSummaryMessage implements NoCauseMessage {
    private String database;
    private List sums;
    
    public AnalysisSummaryMessage() {
        sums = new LinkedList();
    }
    
    public List getSummaries() {
        return sums;
    }
    
    public void addSummary(SUM sum){
        sums.add(sum);
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(database);
        out.writeInt(sums.size());
        Iterator it = sums.iterator();
        while (it.hasNext()) {
            Object o = it.next();
            out.writeObject(o);
        }
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        database = (String)in.readObject();
        int size = in.readInt();
        for(int i = 0; i < size; i++){
            Object o = in.readObject();
            sums.add(o);
        }
    }

    public int getVersion() {
        return 0;
    }

    public void setVersion(int v) {
    }
    
    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public static class SUM implements Message {
        private String module;
        private ModuleHandle handle;
        private double progress;
        
        public SUM(){
        }
        
        public SUM(String mod, ModuleHandle h, double prog){
            module = mod;
            handle = h;
            progress = prog;
        }
        
        public void writeExternal(ObjectOutput out) throws IOException {
            out.writeObject(module);
            out.writeObject(handle);
            out.writeDouble(progress);
        }

        public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
            module = (String)in.readObject();
            handle = (ModuleHandle)in.readObject();
            progress = in.readDouble();
        }

        public int getVersion() {
            return 0;
        }

        public void setVersion(int v) {
        }

        public String getModule() {
            return module;
        }

        public void setModule(String module) {
            this.module = module;
        }

        public ModuleHandle getHandle() {
            return handle;
        }

        public void setHandle(ModuleHandle handle) {
            this.handle = handle;
        }

        public double getProgress() {
            return progress;
        }

        public void setProgress(double progress) {
            this.progress = progress;
        }
    }
}