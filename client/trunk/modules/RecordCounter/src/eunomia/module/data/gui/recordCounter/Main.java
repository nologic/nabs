/*
 * Main.java
 *
 * Created on November 24, 2006, 3:47 PM
 *
 */

package eunomia.module.data.gui.recordCounter;

import com.vivic.eunomia.module.frontend.GUIStaticAnalysisModule;
import java.awt.BorderLayout;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

/**
 *
 * @author Mikhail Sosonkin
 */
public class Main implements GUIStaticAnalysisModule {
    private JComponent args;
    private JComponent repo;
    private JLabel result;
    private JTextArea rowCount;
    
    public Main() {
        args = newArgsComponent();
        repo = newRepoComponent();
    }

    public void setArguments(DataInputStream in) {
        try {
            rowCount.setText("" + in.readInt());
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public JComponent getArgumentsComponent() {
        return args;
    }

    public JComponent getResultsComponent() {
        return repo;
    }

    public void setResult(DataInputStream in) {
        try {
            byte[] bytes = new byte[in.available()];
            in.read(bytes);
            result.setText("Number of rows: " + new String(bytes));
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public void getArguments(DataOutputStream out) {
    }

    private JComponent newArgsComponent() {
        rowCount = new JTextArea();
        
        return rowCount;
    }

    private JComponent newRepoComponent() {
        result = new JLabel("Number of rows: not calculated");
        
        return result;
    }
}