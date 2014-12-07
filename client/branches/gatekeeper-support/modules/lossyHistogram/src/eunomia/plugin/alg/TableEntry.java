/*
 * TableEntry.java
 *
 * Created on August 5, 2005, 12:53 PM
 *
 */

package eunomia.plugin.alg;

import com.vivic.eunomia.module.Flow;
import eunomia.receptor.module.NABFlow.NABFlow;

/**
 *
 * @author Mikhail Sosonkin
 */
public class TableEntry implements Comparable {
    private LossyFlow flow;
    private long delta;
    private int frequency;
    private int[] typeFreq;
    private boolean isUsed;
    private long timeStamp;
    private long startTime;
    
    public TableEntry(){
        isUsed = true;
        flow = new LossyFlow();
        typeFreq = new int[NABFlow.NUM_TYPES];
    }
    
    public void aquireNew(Flow newFlow, int[] types, long del){
        startTime = System.currentTimeMillis();
        timeStamp = startTime;
        int len = typeFreq.length;
        for(int i = len - 1; i != -1; --i){
            typeFreq[i] = types[i];
        }
        
        frequency = 1;
        flow.assign(newFlow);
        delta = del;
        isUsed = true;
    }
    
    public long getStartTimeMilis(){
        return startTime;
    }
    
    public Flow getFlow(){
        return flow;
    }
    
    public void unUsed(){
        isUsed = false;
    }
    
    public boolean isUsed(){
        return isUsed;
    }
    
    public long getTimeStamp(){
        return timeStamp;
    }
    
    public void incFrequency(int[] types){
        timeStamp = System.currentTimeMillis();
        for (int i = 0; i < types.length; ++i) {
            int t = types[i];
            frequency += t;
            typeFreq[i] += t;
        }
    }
    
    public int getFrequency(){
        return frequency;
    }
    
    public int[] getTypeFrequencies(){
        return typeFreq;
    }
    
    public long getDelta(){
        return delta;
    }
    
    public void setDelta(long d){
        delta = d;
    }
    
    public long getDeltaPlusFrequency(){
        return frequency + delta;
    }
    
    public int compareTo(Object o){
        TableEntry te = (TableEntry)o;
        
        if(!isUsed){
            if(te.isUsed){
                return 1;
            } else {
                return 0;
            }
        } else {
            if(!te.isUsed){
                return -1;
            } else {
                int ff = frequency;
                int teff = te.frequency;
                
                if(ff < teff){
                    return 1;
                } else if(ff == teff){
                    return 0;
                } else {
                    return -1;
                }
            }
        }
    }
}
