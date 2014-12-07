/*
 * LossyNabsClient.java
 *
 * Created on June 9, 2005, 12:44 PM
 */

package eunomia.plugin.alg;

import eunomia.flow.*;
import eunomia.receptor.module.NABFlow.FlowComparator;
import eunomia.receptor.module.NABFlow.NABFlow;
import java.util.*;

/**
 *
 * @author  Mikhail Sosonkin
 */

// should convert table from list to an array.

public class LossyCounter {
    private static TableEntry[] tblArr = new TableEntry[]{};
    
    private LinkedList table;
    private TableEntry[] flows;
    private int bucketWidth;
    private int curBucket;
    private long streamLen;
    private int timeout;
    private FlowComparator comp;
    
    private double e;
    private double s;
    private double seDiff;
    
    public LossyCounter(FlowComparator fc) {
        comp = fc;
        timeout = -1;
        setParams(0.0005, 0.0001, 15, -1, true);
    }
    
    public void setParams(double ee, double ss, int tableSize, int to, boolean doreset){
        e = ee;
        s = ss;
        seDiff = s - e;
        timeout = to;
        
        flows = new TableEntry[tableSize];
        if(doreset){
            table = new LinkedList();
        }
        bucketWidth = (int)Math.ceil(1.0/e);
        curBucket = 1;
        streamLen = 0;
    }
    
    public int getTimeout(){
        return timeout;
    }
    
    public double getE(){
        return e;
    }
    
    public double getS(){
        return s;
    }
    
    public int getTableSize(){
        return flows.length;
    }
    
    public void reset(){
        setParams(e, s, flows.length, timeout, true);
    }
    
    public TableEntry[] getTable(){
        int flowLen = flows.length;
        int eLen = 0;
        
        LinkedList selected = new LinkedList();
        synchronized(table){
            double qual = seDiff * (double)streamLen;
            Iterator it = table.iterator();
            while(it.hasNext()){
                TableEntry entry = (TableEntry)it.next();
                if(entry.isUsed()){
                    if(entry.getFrequency() >= qual){
                        selected.addLast(entry);
                    }
                }
            }
        }
        
        TableEntry[] entries = (TableEntry[])selected.toArray(tblArr);
        Arrays.sort(entries);
        eLen = entries.length;
        long time = System.currentTimeMillis();
        
        //I think timeout should be move to prune.
        for(int i = 0; i < flowLen; ++i){
            flows[i] = null;
            if(i < eLen){
                int timeDiff = (int)(time - entries[i].getTimeStamp());
                if(timeout != -1 && timeDiff > timeout){
                    deleteEntry(entries[i]);
                    continue;
                }

                flows[i] = entries[i];
            }
        }
        return flows;
    }

    public void newFlow(NABFlow flow) {
        insertFlow(flow);
        ++streamLen;
        
        if(streamLen % bucketWidth == 0){
            ++curBucket;
            prune();
        }
    }
    
    public void deleteEntry(TableEntry entry){
        entry.unUsed();
    }
    
    private void insertFlow(NABFlow flow){
        TableEntry unUsed = null;
        
        synchronized(table){
            Iterator it = table.iterator();
            while(it.hasNext()){
                TableEntry entry = (TableEntry)it.next();
                if(entry.isUsed()){
                    NABFlow eFlow = entry.getFlow();
                    if(comp.areEqual(eFlow, flow)){
                        entry.incFrequency(flow.getType());
                        return;
                    }
                } else {
                    if(unUsed == null){
                        unUsed = entry;
                    }
                }
            }
        }
        
        //inserting
        TableEntry newEntry;
        if(unUsed == null){
            newEntry = new TableEntry();
            synchronized(table){
                table.addLast(newEntry);
            }
        } else {
            newEntry = unUsed;
        }
        
        newEntry.aquireNew(flow, curBucket - 1);
    }
    
    private void prune(){
        int removed = 0;
        
        synchronized(table){
            int tableSize = table.size();
            Iterator it = table.iterator();
            while(it.hasNext()){
                TableEntry entry = (TableEntry)it.next();
                if(entry.getDeltaPlusFrequency() <= curBucket && (tableSize - removed) >= bucketWidth){
                    entry.unUsed();
                    ++removed;
                }
            }
        }
    }


}