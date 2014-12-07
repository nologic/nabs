/*
 * GUIStaticAnalysisModule.java
 *
 * Created on November 21, 2006, 9:47 PM
 *
 */

package eunomia.plugin.interfaces;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import javax.swing.JComponent;

/**
 *
 * @author Mikhail Sosonkin
 */
public interface GUIStaticAnalysisModule {
    public void setArguments(DataInputStream in);
    public void getArguments(DataOutputStream out);
    public void setResult(DataInputStream in);
    public JComponent getArgumentsComponent();
    public JComponent getResultsComponent();
}