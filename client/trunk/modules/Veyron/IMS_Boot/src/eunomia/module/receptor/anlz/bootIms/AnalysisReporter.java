/*
 * AnalysisReporter.java
 *
 * Created on April 23, 2008, 11:36 PM
 *
 */

package eunomia.module.receptor.anlz.bootIms;

import eunomia.module.receptor.libb.imsCore.Reporter;
import eunomia.module.receptor.libb.imsCore.net.NetworkChannel;

/**
 *
 * @author Mikhail Sosonkin
 */
public class AnalysisReporter implements Reporter {
    private Reporter[] reporters;
    
    public AnalysisReporter() {
        reporters = new Reporter[1];
    }
    
    public void addReporter(Reporter rep) {
        for (int i = 0; i < reporters.length; i++) {
            if(reporters[i] == null) {
                reporters[i] = rep;
                return;
            }
        }
        
        Reporter[] newRep = new Reporter[reporters.length + 1];
        System.arraycopy(reporters, 0, newRep, 0, reporters.length);
        newRep[reporters.length] = rep;
        
        reporters = newRep;
    }

    public void commandAndControlChannel(NetworkChannel channel) {
        Reporter[] reps = reporters;
        
        for (int i = 0; i < reps.length; i++) {
            if(reps[i] != null) {
                reps[i].commandAndControlChannel(channel);
            }
        }
    }

    public void executeSql(String sql) {
        Reporter[] reps = reporters;
        
        for (int i = 0; i < reps.length; i++) {
            if(reps[i] != null) {
                reps[i].executeSql(sql);
            }
        }
    }
}