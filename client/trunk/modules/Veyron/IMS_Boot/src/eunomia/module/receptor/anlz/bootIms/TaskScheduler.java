/*
 * TaskScheduler.java
 *
 * Created on February 3, 2008, 6:52 PM
 *
 */

package eunomia.module.receptor.anlz.bootIms;

import eunomia.module.receptor.libb.imsCore.VeyronAnalysisComponent;
import java.util.ArrayList;

/**
 *
 * @author Mikhail Sosonkin
 */
public class TaskScheduler {
    private Task[] tasks;
    private int taskCount;
    
    public TaskScheduler() {
        taskCount = 0;
        tasks = new Task[10];
    }
    
    public void addComponent(VeyronAnalysisComponent comp, long secInterval, long firstRunInterval) {
        if(taskCount == tasks.length) {
            Task[] tmp = new Task[tasks.length * 2];
            System.arraycopy(tasks, 0, tmp, 0, tasks.length);
            tasks = tmp;
        }
        
        tasks[taskCount++] = new Task(comp, secInterval, firstRunInterval);
    }
    
    public void runTasks() {
        for (int i = 0; i < taskCount; i++) {
            if(tasks[i].doSelect(System.currentTimeMillis())) {
                tasks[i].execute();
            }
        }
    }
}