/*
 * Task.java
 *
 * Created on February 3, 2008, 7:19 PM
 *
 */

package eunomia.module.receptor.anlz.bootIms;

import eunomia.module.receptor.libb.imsCore.VeyronAnalysisComponent;

/**
 *
 * @author Mikhail Sosonkin
 */
public class Task {
    private boolean isFirst;
    private long lastRun;
    private long interval;
    private long firstRun;
    private long created;
    private VeyronAnalysisComponent component;
    
    public Task(VeyronAnalysisComponent comp, long secInterval, long firstRunInterval) {
        isFirst = true;
        created = System.currentTimeMillis();
        component = comp;
        firstRun = firstRunInterval;
        interval = secInterval;
    }
    
    public boolean doSelect(long time) {
        if(isFirst) {
            return (created + firstRun) < time;
        }
        
        return (lastRun + interval) < time;
    }
    
    public void execute() {
        isFirst = false;
        component.executeAnalysis();
        lastRun = System.currentTimeMillis();
    }
}
