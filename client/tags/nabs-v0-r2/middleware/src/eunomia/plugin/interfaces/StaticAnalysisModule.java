/*
 * StaticAnalysisModule.java
 *
 * Created on November 21, 2006, 8:59 PM
 *
 */

package eunomia.plugin.interfaces;

import eunomia.data.Database;
import java.io.DataInputStream;
import java.io.DataOutputStream;

/**
 *
 * @author Mikhail Sosonkin
 */
public interface StaticAnalysisModule {
    /**
     * 
     * @return 1 for finished, or not doing anything. 
     * -1 for busy and can't tell the progress.
     * fraction value for the percent done.
     */
    public double getProgress();
    
    /**
     * 
     * @param arg Message that will specify parameters to the processing function.
     * @param db Database to work on.
     * @return If null, then agruments were accepted. If not then the message has error
     * report.
     */
    public void beginAnalysis(DataInputStream in, Database db);
    public void getArguments(DataOutputStream out);
    public void getResult(DataOutputStream out);
}