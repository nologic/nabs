/*
 * Main.java
 *
 * Created on October 23, 2005, 5:54 PM
 *
 */

package eunomia.plugin.rec.lossyHistogram;

import eunomia.flow.*;
import eunomia.managers.ModuleManager;
import eunomia.messages.Message;
import eunomia.messages.module.ModuleMessage;
import eunomia.messages.module.msg.GenericModuleMessage;
import eunomia.messages.receptor.ModuleHandle;
import eunomia.plugin.alg.*;
import eunomia.plugin.interfaces.*;
import eunomia.plugin.msg.ModifyGraphMessage;
import eunomia.flow.Filter;
import eunomia.receptor.module.NABFlow.NABFilterEntry;
import eunomia.receptor.module.NABFlow.FlowComparator;
import eunomia.util.number.ModInteger;
import java.io.*;
import java.util.*;

/**
 *
 * @author Mikhail Sosonkin
 */
public class Main implements ReceptorModule {
    private FlowProc proc;
    private LossyCounter lc;
    private FlowComparator comparator;
    private HashMap hashToEntry;
    private TableEntry[] lossyTable;
    private ModInteger retriever;
    
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
                entry.getFlow().writeToDataStream(dout);
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
        lc.reset();
    }
    
    public void start() {
        proc.setDoProc(true);
    }

    public void stop() {
        proc.setDoProc(false);
    }
    
    public synchronized void setControlData(InputStream in) throws IOException {
        boolean doReset;
        
        DataInputStream din = new DataInputStream(in);
        comparator.setSrcIp(din.readBoolean());
        comparator.setDstIp(din.readBoolean());
        comparator.setInterHost(din.readBoolean());
        comparator.setConnection(din.readBoolean());
        comparator.setSingleHost(din.readBoolean());
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

    public Message processMessage(ModuleMessage msg) throws IOException {
        if(msg instanceof GenericModuleMessage){
            GenericModuleMessage gmm = (GenericModuleMessage)msg;
            ObjectInputStream oin = new ObjectInputStream(gmm.getInputStream());
            Object o = null;
            try {
                o = oin.readObject();
            } catch (ClassNotFoundException ex){
                ex.printStackTrace();
                return null;
            }
            
            if(o instanceof ModifyGraphMessage){
                modifyGraph((ModifyGraphMessage)o);
            }
        }
        
        return null;
    }
    
    public void setProperty(String name, Object value) {
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
        if(handle != null){
            ReceptorModule mod = ModuleManager.v().getModule(handle);
            int[] ip = null;
            long lip;
            if(e1 != null){
                ip = e1.getSrc_lip();
                if(ip[0] == 0 && ip[1] == 0 && ip[2] == 0 && ip[3] == 0){
                    ip = e1.getDst_lip();
                }
                lip = (((long)ip[0] & 0xFF) << 24) | (((long)ip[1] & 0xFF) << 16) | 
                      (((long)ip[2] & 0xFF) << 8) | ((long)ip[3] & 0xFF);
                mod.setProperty("ah", Long.valueOf(lip));
            }
            
            if(e2 != null){
                ip = e2.getSrc_lip();
                if(ip[0] == 0 && ip[1] == 0 && ip[2] == 0 && ip[3] == 0){
                    ip = e2.getDst_lip();
                }
                lip = (((long)ip[0] & 0xFF) << 24) | (((long)ip[1] & 0xFF) << 16) | 
                      (((long)ip[2] & 0xFF) << 8) | ((long)ip[3] & 0xFF);
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