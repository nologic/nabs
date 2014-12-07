/*
 * LossyNabsClient.java
 *
 * Created on June 9, 2005, 12:44 PM
 */

package eunomia.plugin.alg;

import com.vivic.eunomia.module.Flow;
import eunomia.receptor.module.NABFlow.FlowComparator;
import eunomia.receptor.module.NABFlow.NABFlow;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicReference;

/**
 *
 * @author  Mikhail Sosonkin
 */

public class LossyCounter {
    private static TableEntry[] tblArr = new TableEntry[]{};
    private static int initialSize = 1 << 10;
    
    private int firstNull;
    private boolean doReset;
    private AtomicReference tableRef;
    
    private TableEntry[] flows;
    private int bucketWidth;
    private int curBucket;
    private long streamLen;
    private long lastStreamLen;
    private int timeout;
    private FlowComparator comp;
    
    private double e;
    private double s;
    private double seDiff;
    
    public LossyCounter(FlowComparator fc) {
        comp = fc;
        timeout = -1;
        tableRef = new AtomicReference();
        
        firstNull = 0;
        tableRef.getAndSet(new TableEntry[initialSize]);

        setParams(0.0005, 0.0001, 15, -1, false);
    }
    
    public void setParams(double ee, double ss, int tableSize, int to, boolean doreset){
        e = ee;
        s = ss;
        seDiff = s - e;
        timeout = to;
        
        flows = new TableEntry[tableSize];
        doReset = doreset;
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
        TableEntry[] entryTable = (TableEntry[])tableRef.get();
        LinkedList selected = new LinkedList();
        double qual = seDiff * (double)streamLen;

        for (int i = 0; i < firstNull; ++i) {
            TableEntry entry = entryTable[i];
            if(entry.isUsed()){
                if(entry.getFrequency() >= qual){
                    selected.addLast(entry);
                }
            }
        }
        
        TableEntry[] entries = (TableEntry[])selected.toArray(tblArr);
        Arrays.sort(entries);
        for(int i = 0; i < flows.length; ++i){
            if(i < entries.length) {
                flows[i] = entries[i];
            } else {
                flows[i] = null;
            }
        }
        
        return flows;
    }

    public void newFlow(Flow flow, int[] types) {
        insertFlow(flow, types);
        for (int i = 0; i < types.length; i++) {
            streamLen += types[i];
        }
        
        if((streamLen - lastStreamLen) > bucketWidth){
            lastStreamLen = streamLen;
            ++curBucket;
            prune();
        }
    }
    
    public void deleteEntry(TableEntry entry){
        entry.unUsed();
    }
    
    private void insertFlow(Flow flow, int[] types){
        if(doReset){
            // if missed the first time, then process and do reset next time.
            doReset = false;
            firstNull = 0;
            tableRef.getAndSet(new TableEntry[initialSize]);
        }
        
        TableEntry unUsed = null;
        int unUsedIndex = -1;
        TableEntry[] entryTable = (TableEntry[])tableRef.get();
        int tableSize = entryTable.length;
        
        for (int i = 0; i < firstNull; ++i) {
            TableEntry entry = entryTable[i];
            if(entry.isUsed()){
                Flow eFlow = entry.getFlow();
                if(comp.areEqual(eFlow, flow)){
                    entry.incFrequency(types);
                    if(unUsed != null) {
                        entryTable[unUsedIndex] = entry;
                        entryTable[i] = unUsed;
                    }
                    
                    return;
                }
            } else {
                if(unUsed == null){
                    unUsed = entry;
                    unUsedIndex = i;
                }
            }
        }
        
        //inserting
        TableEntry newEntry;
        if(unUsed == null){
            newEntry = new TableEntry();
            entryTable[firstNull++] = newEntry;
            
            // expand table
            if(firstNull == entryTable.length){
                TableEntry[] lcTable = entryTable;
                tableSize <<= 1;
                entryTable = new TableEntry[tableSize];
                System.arraycopy(lcTable, 0, entryTable, 0, lcTable.length);
                tableRef.getAndSet(entryTable);
            }
        } else {
            newEntry = unUsed;
        }
        
        newEntry.aquireNew(flow, types, curBucket - 1);
    }
    
    private void prune(){
        int removed = 0;
        TableEntry[] entryTable = (TableEntry[])tableRef.get();
        int tableSize = entryTable.length;
        long time = System.currentTimeMillis();

        for (int i = 0; i < firstNull; ++i) {
            TableEntry entry = entryTable[i];
            if(entry.isUsed()) {
                if( (entry.getDeltaPlusFrequency() <= curBucket && (tableSize - removed) >= bucketWidth) ){
                    entry.unUsed();
                    ++removed;
                } else if(timeout != -1 && ((int)(time - entry.getTimeStamp())) > timeout) {
                    entry.unUsed();
                }
            }
        }
    }
}