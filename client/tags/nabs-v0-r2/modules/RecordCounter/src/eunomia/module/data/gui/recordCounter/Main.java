/*
 * Main.java
 *
 * Created on November 24, 2006, 3:47 PM
 *
 */

package eunomia.module.data.gui.recordCounter;

import eunomia.plugin.interfaces.GUIStaticAnalysisModule;
import java.awt.GridLayout;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/**
 *
 * @author Mikhail Sosonkin
 */
public class Main implements GUIStaticAnalysisModule {
    private JComponent args;
    private JComponent repo;
    private JTextArea result;
    private JTextField rowCount;
    
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
            rowCount.setText(new String(bytes));
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public void getArguments(DataOutputStream out) {
        int rows = Integer.parseInt(rowCount.getText());
        try {
            out.writeInt(rows);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private JComponent newArgsComponent() {
        JPanel arg = new JPanel(new GridLayout(1, 2));
        arg.add(new JLabel("Enter row count: "));
        arg.add(rowCount = new JTextField());
        
        return arg;
    }

    private JComponent newRepoComponent() {
        result = new JTextArea();
        
        return result;
    }
}