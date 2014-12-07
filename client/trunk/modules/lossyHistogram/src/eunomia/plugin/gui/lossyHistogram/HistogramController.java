/*
 * HistogramController.java
 *
 * Created on July 14, 2005, 8:28 PM
 *
 */

package eunomia.plugin.gui.lossyHistogram;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

/**
 *
 * @author Mikhail Sosonkin
 */

public class HistogramController extends JPanel {
    private static String[] choices = new String[] {
        "Most active uploaders", "Most active downloaders",
        "Most active host pairs", "Most active flows", "Most active hosts"};

    private JCheckBox resetBox;
    private JTextField eField, sField, numHosts, timeout;
    private StreamCategoryData scd;
    private JComboBox showType;
    
    public HistogramController(StreamCategoryData cd) {
        scd = cd;
        addControls();
        
        showType.setSelectedIndex(0);
    }
    
    public void getControlData(OutputStream out) throws IOException {
        DataOutputStream dout = new DataOutputStream(out);
        int type = showType.getSelectedIndex();
        for (int i = 0; i < 5; i++) {
            dout.writeBoolean(type == i);
        }
        
        dout.writeBoolean(resetBox.isSelected());
        
        dout.writeDouble(Double.parseDouble(sField.getText()));
        dout.writeDouble(Double.parseDouble(eField.getText()));
        dout.writeInt(Integer.parseInt(numHosts.getText()));
        int to = Integer.parseInt(timeout.getText());
        if(to != -1){
            to *= 1000;
        }
        dout.writeInt(to);
        
        updateType();
    }
    
    public void setControlData(InputStream in) throws IOException {
        DataInputStream din = new DataInputStream(in);
        
        int type = 0;
        for (int i = 0; i < 5; i++) {
            if(din.readBoolean()) {
                type = i;
            }
        }
        showType.setSelectedIndex(type);
        
        sField.setText(Double.toString(din.readDouble()));
        eField.setText(Double.toString(din.readDouble()));
        numHosts.setText(Integer.toString(din.readInt()));
        int to = din.readInt();
        if(to != -1){
            timeout.setText(Integer.toString(to/1000));
        } else {
            timeout.setText("-1");
        }
        
        updateType();
    }
    
    private void updateType(){
        scd.setType(showType.getSelectedIndex());
    }
   
    private void addControls(){
        JPanel choicePanel = new JPanel(new GridLayout(2, 1));
        JPanel dispType = new JPanel(new BorderLayout());
        JPanel mainPanel = new JPanel();
        JPanel paramsPanel = new JPanel(new GridLayout(4, 2));

        setLayout(new BorderLayout());
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        
        dispType.add(new JLabel("Display Information: "), BorderLayout.WEST);
        dispType.add(showType = new JComboBox(choices));
        choicePanel.add(resetBox = new JCheckBox("Reset on commit"));
        choicePanel.add(dispType);
        
        paramsPanel.add(new JLabel("Error Rate"));
        paramsPanel.add(eField = new JTextField());
        paramsPanel.add(new JLabel("Threshold"));
        paramsPanel.add(sField = new JTextField());
        paramsPanel.add(new JLabel("Number of Top Heavy-Hitters"));
        paramsPanel.add(numHosts = new JTextField());
        paramsPanel.add(new JLabel("Idle Timeout (Seconds, -1 for none)"));
        paramsPanel.add(timeout = new JTextField());

        mainPanel.add(choicePanel);
        mainPanel.add(paramsPanel);
        add(mainPanel, BorderLayout.NORTH);
        
        resetBox.setSelected(true);
        
        choicePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Graph Information"));
        paramsPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Top Talkers Settings"));
    }
}