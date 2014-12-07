/*
 * TableEntry.java
 *
 * Created on August 5, 2005, 12:53 PM
 *
 */

package eunomia.plugin.alg;

import eunomia.core.data.flow.*;

/**
 *
 * @author Mikhail Sosonkin
 */
public class TableEntry implements Comparable {
    private Flow flow;
    private long delta;
    private int frequency;
    private int[] typeFreq;
    private boolean isUsed;
    private long timeStamp;
    private long startTime;
    
    public TableEntry(){
        isUsed = true;
        flow = new Flow(null);
        typeFreq = new int[Flow.NUM_TYPES];
    }
    
    public void aquireNew(Flow newFlow, long del){
        startTime = System.currentTimeMillis();
        timeStamp = startTime;
        int len = typeFreq.length;
        for(int i = len - 1; i != -1; --i){
            typeFreq[i] = 0;
        }
        
        frequency = 1;
        flow.takeFrom(newFlow);
        ++typeFreq[newFlow.getType()];
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
    
    public void incFrequency(int type){
        timeStamp = System.currentTimeMillis();
        ++frequency;
        ++typeFreq[type];
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
