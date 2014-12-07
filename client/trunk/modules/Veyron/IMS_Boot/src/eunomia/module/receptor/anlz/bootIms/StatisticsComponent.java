/*
 * StatisticsTask.java
 *
 * Created on February 26, 2008, 5:24 PM
 *
 */

package eunomia.module.receptor.anlz.bootIms;

import eunomia.module.receptor.libb.imsCore.Reporter;
import eunomia.module.receptor.libb.imsCore.db.NetEnv;
import eunomia.module.receptor.libb.imsCore.NetworkSymbols;
import eunomia.module.receptor.libb.imsCore.NetworkTopology;
import eunomia.module.receptor.libb.imsCore.VeyronAnalysisComponent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;

/**
 *
 * @author Mikhail Sosonkin
 */
public class StatisticsComponent implements VeyronAnalysisComponent {
    private NetEnv env;
    private File file;
    private PrintStream out;
    
    private boolean fast;
    private boolean clear;
    
    public StatisticsComponent(NetEnv env, String filename) throws FileNotFoundException {
        fast = true;
        clear = true;
        
        this.env = env;
        
        if(filename != null) {
            file = new File(filename);
            
            FileOutputStream fout = new FileOutputStream(file, true);
            out = new PrintStream(fout);
        } else {
            out = System.out;
        }
    }

    public void initialize(NetworkTopology net, NetworkSymbols syms) {
    }

    public void executeAnalysis() {
        try {
            env.extractStatistics(out, clear, fast);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public boolean isFast() {
        return fast;
    }

    public void setFast(boolean fast) {
        this.fast = fast;
    }

    public boolean isClear() {
        return clear;
    }

    public void setClear(boolean clear) {
        this.clear = clear;
    }

    public void setReporter(Reporter report) {
    }
}