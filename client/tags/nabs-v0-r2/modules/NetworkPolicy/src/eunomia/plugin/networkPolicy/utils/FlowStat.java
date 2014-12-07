/*
 * FlowStat.java
 *
 * Created on December 16, 2006, 12:12 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package eunomia.plugin.networkPolicy.utils;

/**
 *
 * @author kulesh
 */
public class FlowStat {

    private long startTime;
    private long lastUpdate;
    private long bytes;
    
    /** Creates a new instance of FlowStat */
    public FlowStat() {
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(long lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public long getBytes() {
        return bytes;
    }

    public void setBytes(long bytes) {
        this.bytes = bytes;
    }
    
    public void incrementBytes(long bytes){
        this.bytes += bytes;
    }
    
}