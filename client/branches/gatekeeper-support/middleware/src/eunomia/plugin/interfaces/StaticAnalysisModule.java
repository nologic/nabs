/*
 * StaticAnalysisModule.java
 *
 * Created on November 21, 2006, 8:59 PM
 *
 */

package eunomia.plugin.interfaces;

import com.vivic.eunomia.module.EunomiaModule;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.List;

/**
 *
 * @author Mikhail Sosonkin
 */
public interface StaticAnalysisModule extends EunomiaModule {
    public void destroy();
    /**
     * 
     * @return 1 for finished, or not doing anything. 
     * -1 for busy and can't tell the progress.
     * fraction value for the percent done.
     */
    public double getProgress();
    public void setAndCheckParameters(DataInputStream in, List dbs) throws Exception ;
    public void beginAnalysis();
    public void getArguments(DataOutputStream out);
    public void getResult(DataOutputStream out);
}