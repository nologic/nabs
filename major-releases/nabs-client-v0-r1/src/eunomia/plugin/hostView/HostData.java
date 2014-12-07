/*
 * Summary.java
 *
 * Created on August 10, 2005, 5:18 PM
 *
 */

package eunomia.plugin.hostView;

import eunomia.core.data.flow.*;
import java.util.*;

/**
 *
 * @author Mikhail Sosonkin
 */

public class HostData {
    private long startTime;
    private long totalBytes, inBytes, outBytes;
    
    private double outRate, inRate;
    private long firstTime;
    private long lastTime;
    private long firstInBytes;
    private long firstOutBytes;
    private int[] dataTypes;
    
    //[0][] - IN, [1][] - OUT, [2][] - TOTAL
    private long[][] dataTable;
    private long[] dataTable_in;
    private long[] dataTable_out;
    private long[] dataTable_total;
    
    private HistoryData historyData;
    private ConversationList cList;
    
    public HostData() {
        startTime = System.currentTimeMillis();
        firstTime = -1;
        dataTypes = new int[Flow.NUM_TYPES];
        
        dataTable = new long[3][];
        for(int i = 0; i < 3; ++i){
            dataTable[i] = new long[Flow.NUM_TYPES + 1];
        }
        dataTable_in = dataTable[0];
        dataTable_out = dataTable[1];
        dataTable_total = dataTable[2];
        
        cList = new ConversationList();
        cList.setGCTimeInterval(20000, 15000);
    }
    
    public long getStartTime(){
        return startTime;
    }
    
    public Iterator getConversationIterator(){
        return cList.getIterator();
    }
    
    public Conversation[] getConversationArray(){
        return cList.getArray();
    }
    
    public int conversationCount(){
        return cList.convCount();
    }
    
    public void setHistoryData(HistoryData hData){
        historyData = hData;
    }
    
    private void recordTime(Flow flow){
        long time = System.currentTimeMillis()/1000;
        
        if(firstTime == -1){
            firstTime = time;
            firstInBytes = inBytes;
            firstOutBytes = outBytes;
        }
        
        lastTime = time;
    }
    
    private void updataDataTypesBidirectional(int type, long size){
        synchronized(dataTypes){
            dataTypes[type] += size;
        }
        
        totalBytes += size;
        dataTable_total[type] += size;
        dataTable_total[dataTable_total.length - 1] += size;
    }
    
    public void incoming(Flow flow){
        long size = (long)flow.getSize();
        int type = flow.getType();
        
        inBytes += size;
        dataTable_in[type] += size;
        dataTable_in[dataTable_in.length - 1] += size;
        
        recordTime(flow);
        updataDataTypesBidirectional(type, size);
        
        Conversation conv = cList.findConversation(flow.getSourceIp(), flow.getDestinationPort(), flow.getSourcePort());
        conv.incoming(size, type);
    }
    
    public void outgoing(Flow flow){
        long size = (long)flow.getSize();
        int type = flow.getType();
        
        outBytes += size;
        dataTable_out[type] += size;
        dataTable_out[dataTable_out.length - 1] += size;
        
        recordTime(flow);
        updataDataTypesBidirectional(type, size);

        Conversation conv = cList.findConversation(flow.getDestinationIp(), flow.getSourcePort(), flow.getDestinationPort());
        conv.outgoing(size, type);
    }
    
    public void updateHistory(){
        if(historyData != null){
            synchronized(dataTypes){
                historyData.addEntryCopy(dataTypes);
                for(int i = dataTypes.length - 1; i != -1; --i){
                    dataTypes[i] = 0;
                }
            }
        }
    }
    
    /**
     * Problem: Rates should not be computed using localhost's time and the number of 
     * flows recieved for that time.
     * 
     * Reason: Flows can be buffered and recieved in chunks.
     * Reason: Flow time is a time when the flow collection started on the server and
     * when recieved they are not necesserally increasing. For example the flow might
     * have started 15 mins ago and was filing up very slowly.
     * 
     * Solution: Need a way to compute rates depending on the flow times.
     */
    public void computeRates(){
        long timeDiff = (lastTime - firstTime);
        
        if(timeDiff > 0){
            long inDiff = (inBytes - firstInBytes);
            long outDiff = (outBytes - firstOutBytes);
            
            outRate = (double)outDiff/(double)timeDiff;
            inRate = (double)inDiff/(double)timeDiff;
            
            firstTime = -1;
        }
        
        firstTime = -1;
    }

    public long getTotalBytes() {
        return totalBytes;
    }

    public long getInBytes() {
        return inBytes;
    }

    public long getOutBytes() {
        return outBytes;
    }

    public double getOutRate() {
        return outRate;
    }

    public double getInRate() {
        return inRate;
    }
    
    public long[][] getDataTable(){
        return dataTable;
    }
}