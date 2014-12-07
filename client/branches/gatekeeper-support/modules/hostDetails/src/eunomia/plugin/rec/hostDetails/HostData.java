/*
 * Summary.java
 *
 * Created on August 10, 2005, 5:18 PM
 *
 */

package eunomia.plugin.rec.hostDetails;

import eunomia.flow.*;
import eunomia.receptor.module.NABFlow.NABFlow;
import java.io.*;

/**
 *
 * @author Mikhail Sosonkin
 */

public class HostData {
    //send
    private long startTime;
    private long totalBytes, inBytes, outBytes;
    private double outRate, inRate;
    private long[][] dataTable; //[0][] - IN, [1][] - OUT, [2][] - TOTAL

    //not send to client
    private long firstTime;
    private long lastTime;
    private long firstInBytes;
    private long firstOutBytes;
    private long[] dataTable_in;
    private long[] dataTable_out;
    private long[] dataTable_total;
    
    public HostData() {
        startTime = System.currentTimeMillis();
        firstTime = -1;

        dataTable = new long[3][];
        for(int i = 0; i < 3; ++i){
            dataTable[i] = new long[NABFlow.NUM_TYPES + 1];
        }
        dataTable_in = dataTable[0];
        dataTable_out = dataTable[1];
        dataTable_total = dataTable[2];
    }
    
    public long getStartTime(){
        return startTime;
    }
    
    private void recordTime(long milis){
        long time = milis/1000;//System.currentTimeMillis()/1000;
        
        if(firstTime == -1){
            firstTime = time;
            firstInBytes = inBytes;
            firstOutBytes = outBytes;
        }
        
        lastTime = time;
    }
    
    private void updataDataTypesBidirectional(int[] types, long size){
        totalBytes += size;
        for (int i = types.length - 1; i != -1; --i) {
            long t = types[i] * Main.PAYLOAD_SIZE;
            dataTable_total[i] += t;
            dataTable_total[dataTable_total.length - 1] += t;
        }
    }
    
    public void incoming(int[] types, long size, long time){
        inBytes += size;
        
        for (int i = types.length - 1; i != -1; --i) {
            int t = types[i] * Main.PAYLOAD_SIZE;
            dataTable_in[i] += t;
            dataTable_in[dataTable_in.length - 1] += t;
        }
        
        recordTime(time);
        updataDataTypesBidirectional(types, size);
    }
    
    public void outgoing(int[] types, long size, long time){
        outBytes += size;
        
        for (int i = types.length - 1; i != -1; --i) {
            int t = types[i] * Main.PAYLOAD_SIZE;
            dataTable_out[i] += t;
            dataTable_out[dataTable_out.length - 1] += t;
        }
        
        recordTime(time);
        updataDataTypesBidirectional(types, size);
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
    
    public void writeOut(DataOutputStream dout) throws IOException {
        computeRates();
        dout.writeLong(startTime);
        dout.writeLong(totalBytes);
        dout.writeLong(inBytes);
        dout.writeLong(outBytes);
        dout.writeDouble(outRate);
        dout.writeDouble(inRate);
        
        for(int i = 0; i < dataTable.length; i++){
            long[] dTable = dataTable[i];
            for(int k = 0; k < dTable.length; k++){
                dout.writeLong(dTable[k]);
            }
        }
    }
}