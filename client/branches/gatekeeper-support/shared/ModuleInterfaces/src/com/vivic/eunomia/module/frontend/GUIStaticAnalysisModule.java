/*
 * GUIStaticAnalysisModule.java
 *
 * Created on November 21, 2006, 9:47 PM
 *
 */

package com.vivic.eunomia.module.frontend;

import com.vivic.eunomia.module.EunomiaModule;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import javax.swing.JComponent;

/**
 *
 * @author Mikhail Sosonkin
 */
public interface GUIStaticAnalysisModule extends EunomiaModule {
    public void setArguments(DataInputStream in);
    public void getArguments(DataOutputStream out);
    public void setResult(DataInputStream in);
    public JComponent getArgumentsComponent();
    public JComponent getResultsComponent();
}