/*
 * LCTableEntry.java
 *
 * Created on October 31, 2005, 9:37 PM
 *
 */

package eunomia.plugin.gui.lossyHistogram;

import eunomia.plugin.alg.LossyFlow;
import eunomia.receptor.module.NABFlow.NABFlow;

/**
 *
 * @author Mikhail Sosonkin
 */
public class LCTableEntry {
    private LossyFlow flow;
    private int frequency;
    private int[] typeFreq;
    private int idleTime;
    private long startTime;
    private int flowId;
    
    public LCTableEntry(){
        flow = new LossyFlow(0, 0, 0, 0);
        typeFreq = new int[NABFlow.NUM_TYPES];
    }
    
    public LossyFlow getFlow(){
        return flow;
    }
    
    public int getFrequency(){
        return frequency;
    }
    
    public int[] getTypeFrequencies(){
        return typeFreq;
    }
    
    public void setFrequency(int f){
        frequency = f;
    }
    
    public void setFrequencyType(int type, int f){
        typeFreq[type] = f;
    }
    
    public int getIdleTime() {
        return idleTime;
    }

    public void setIdleTime(int idleTime) {
        this.idleTime = idleTime;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public int getFlowId() {
        return flowId;
    }

    public void setFlowId(int flowId) {
        this.flowId = flowId;
    }
}