/*
 * Main.java
 *
 * Created on October 23, 2005, 5:54 PM
 *
 */

package eunomia.plugin.rec.lossyHistogram;

import eunomia.messages.receptor.ModuleHandle;
import eunomia.plugin.msg.ModifyGraphMessage;
import com.vivic.eunomia.filter.Filter;
import com.vivic.eunomia.module.flow.Flow;
import com.vivic.eunomia.module.receptor.FlowProcessor;
import eunomia.plugin.alg.LossyCounter;
import eunomia.plugin.alg.TableEntry;
import com.vivic.eunomia.module.receptor.ReceptorProcessorModule;
import com.vivic.eunomia.sys.receptor.SieveContext;
import eunomia.plugin.msg.RestoreGraphMessage;
import eunomia.receptor.module.NABFlow.NABFilterEntry;
import eunomia.receptor.module.NABFlow.FlowComparator;
import eunomia.util.io.EunomiaObjectInputStream;
import eunomia.util.number.ModInteger;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.util.HashMap;

/**
 *
 * @author Mikhail Sosonkin
 */
public class Main implements ReceptorProcessorModule {
    public static String CMD_DEST_HOST_MV = "CMD_DEST_HOST_MV";
    
    private FlowProc proc;
    private LossyCounter lc;
    private FlowComparator comparator;
    private HashMap hashToEntry;
    private TableEntry[] lossyTable;
    private ModInteger retriever;
    private ReceptorProcessorModule mvDestModule;
    
    public Main() {
        hashToEntry = new HashMap();
        retriever = new ModInteger();
        lc = new LossyCounter(comparator = new FlowComparator());
        proc = new FlowProc(lc);
    }

    public FlowProcessor getFlowProcessor() {
        return proc;
    }

    public synchronized void updateStatus(OutputStream out) throws IOException {
        lossyTable = lc.getTable();
        DataOutputStream dout = new DataOutputStream(out);
        if(lossyTable == null){
            dout.writeInt(0);
            return;
        }
        
        // clean the map;
        Object[] it = hashToEntry.values().toArray();
        for(int i = it.length - 1; i != -1; --i){
            TableEntry entry = (TableEntry)it[i];
            
            if(!entry.isUsed()){
                retriever.setInt(entry.hashCode());
                hashToEntry.remove(retriever);
            }
        }
        
        long time = System.currentTimeMillis();
        dout.writeInt(lossyTable.length);
        for(int i = 0; i < lossyTable.length; ++i){
            TableEntry entry = lossyTable[i];
            dout.writeBoolean(entry != null);
            if(entry != null){
                Flow flow = entry.getFlow();
                dout.writeLong(flow.getSourceIP());
                dout.writeLong(flow.getDestinationIP());
                dout.writeInt(flow.getSourcePort());
                dout.writeInt(flow.getDestinationPort());
                
                dout.writeInt(entry.hashCode());
                dout.writeInt( (int)(time - entry.getTimeStamp()) / 1000);
                dout.writeInt(entry.getFrequency());
                dout.writeLong(entry.getStartTimeMilis());

                int[] tf = entry.getTypeFrequencies();
                for(int k = 0; k < tf.length; ++k){
                    dout.writeInt(tf[k]);
                }
                
                retriever.setInt(entry.hashCode());
                if(!hashToEntry.containsKey(retriever)){
                    ModInteger id = new ModInteger();
                    id.setInt(entry.hashCode());
                    hashToEntry.put(id, entry);
                }
            }
        }
    }
    
    public void reset() {
        stop();
        lc.reset();
        start();
    }
    
    public void start() {
        proc.setDoProc(true);
    }

    public void stop() {
        proc.setDoProc(false);
    }
    
    public void setComparator(boolean setSrcIp, boolean setDstIp, boolean setInterHost, boolean setConnection, boolean setSingleHost) {
        comparator.setSrcIp(setSrcIp);
        comparator.setDstIp(setDstIp);
        comparator.setInterHost(setInterHost);
        comparator.setConnection(setConnection);
        comparator.setSingleHost(setSingleHost);
    }
    
    public synchronized void setControlData(InputStream in) throws IOException {
        boolean doReset;
        
        DataInputStream din = new DataInputStream(in);
        comparator.setSrcIp(din.readBoolean());
        comparator.setDstIp(din.readBoolean());
        comparator.setInterHost(din.readBoolean());
        comparator.setConnection(din.readBoolean());
        comparator.setSingleHost(din.readBoolean());
        proc.setDoubleSource(comparator.isSingleHost());
        
        doReset = din.readBoolean();
        
        double ss = din.readDouble();
        double ee = din.readDouble();
        int nh = din.readInt();
        int to = din.readInt();
        
        lc.setParams(ee, ss, nh, to, doReset);
    }

    public void getControlData(OutputStream out) throws IOException {
        DataOutputStream dout = new DataOutputStream(out);
        dout.writeBoolean(comparator.isSrcIp());
        dout.writeBoolean(comparator.isDstIp());
        dout.writeBoolean(comparator.isInterHost());
        dout.writeBoolean(comparator.isConnection());
        dout.writeBoolean(comparator.isSingleHost());
        
        dout.writeDouble(lc.getS());
        dout.writeDouble(lc.getE());
        dout.writeInt(lc.getTableSize());
        dout.writeInt(lc.getTimeout());
    }
    
    public void processMessage(DataInputStream in, DataOutputStream out) throws IOException {
        ObjectInputStream oin = new EunomiaObjectInputStream(in);
        Object o = null;
        try {
            o = oin.readObject();
        } catch (ClassNotFoundException ex){
            ex.printStackTrace();
            return;
        }

        if(o instanceof ModifyGraphMessage){
            modifyGraph((ModifyGraphMessage)o);
        } else if(o instanceof RestoreGraphMessage) {
            restoreGraph();
        }
    }
    
    private void restoreGraph() {
        proc.getFilter().clearFilter();
    }
    
    public void setProperty(String name, Object value) {
        if(name.equals(CMD_DEST_HOST_MV)) {
            mvDestModule = (ReceptorProcessorModule)value;
        }
    }
    
    public Object getProperty(String name) {
        return null;
    }
    
    private void modifyGraph(ModifyGraphMessage mgm){
        Filter filter = proc.getFilter();
        NABFilterEntry e1 = mgm.getEntry1();
        NABFilterEntry e2 = mgm.getEntry2();
        
        if(e1 != null){
            filter.addFilterBlack(e1);
        }

        if(e2 != null){
            filter.addFilterBlack(e2);
        }
        
        retriever.setInt(mgm.getFlowID());
        TableEntry entry = (TableEntry)hashToEntry.get(retriever);
        entry.unUsed();
        
        ModuleHandle handle = mgm.getHandle();
        ReceptorProcessorModule mod = mvDestModule;
        if(mod == null) {
            mod = SieveContext.getModuleManager().getProcessorModule(handle);
        }

        if(mod != null){
            int[] ip = null;
            long lip;
            if(e1 != null){
                ip = e1.getSrc_lip();
                if(ip[0] == 0 && ip[1] == 0 && ip[2] == 0 && ip[3] == 0){
                    ip = e1.getDst_lip();
                }
                lip = (((long)ip[0] & 0xFF) << 24) | (((long)ip[1] & 0xFF) << 16) | 
                      (((long)ip[2] & 0xFF) << 8 ) | ((long)ip[3] & 0xFF);
                mod.setProperty("ah", Long.valueOf(lip));
            }
            
            if(e2 != null){
                ip = e2.getSrc_lip();
                if(ip[0] == 0 && ip[1] == 0 && ip[2] == 0 && ip[3] == 0){
                    ip = e2.getDst_lip();
                }
                lip = (((long)ip[0] & 0xFF) << 24) | (((long)ip[1] & 0xFF) << 16) | 
                      (((long)ip[2] & 0xFF) << 8 ) | ((long)ip[3] & 0xFF);
                mod.setProperty("ah", Long.valueOf(lip));
            }
        }
    }

    public void initialize() {
    }

    public void destroy() {
    }

    public Object[] getCommands() {
        return null;
    }

    public Object executeCommand(Object command, Object[] parameters) {
        return null;
    }
}