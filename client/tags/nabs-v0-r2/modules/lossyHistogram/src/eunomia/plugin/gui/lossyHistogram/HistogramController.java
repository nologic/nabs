/*
 * HistogramController.java
 *
 * Created on July 14, 2005, 8:28 PM
 *
 */

package eunomia.plugin.gui.lossyHistogram;

import eunomia.plugin.interfaces.GUIModule;
import java.awt.*;
import java.io.*;

import javax.swing.*;

/**
 *
 * @author Mikhail Sosonkin
 */

public class HistogramController extends JPanel {
    private JRadioButton source;
    private JRadioButton destination;
    private JRadioButton interHost;
    private JRadioButton connection;
    private JRadioButton singleHost;
    private JCheckBox resetBox;
    private JTextField eField, sField, numHosts, timeout;
    private StreamCategoryData scd;
    
    public HistogramController(StreamCategoryData cd) {
        scd = cd;
        addControls();
        
        source.setSelected(true);
    }
    
    public void getControlData(OutputStream out) throws IOException {
        DataOutputStream dout = new DataOutputStream(out);
        dout.writeBoolean(source.isSelected());
        dout.writeBoolean(destination.isSelected());
        dout.writeBoolean(interHost.isSelected());
        dout.writeBoolean(connection.isSelected());
        dout.writeBoolean(singleHost.isSelected());
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
        
        source.setSelected(din.readBoolean());
        destination.setSelected(din.readBoolean());
        interHost.setSelected(din.readBoolean());
        connection.setSelected(din.readBoolean());
        singleHost.setSelected(din.readBoolean());
        
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
        if(source.isSelected()){
            scd.setType(StreamCategoryData.SRC_IP);
        } else if(destination.isSelected()){
            scd.setType(StreamCategoryData.DST_IP);
        } else if(interHost.isSelected()){
            scd.setType(StreamCategoryData.HOST_TO_HOST);
        } else if(connection.isSelected()){
            scd.setType(StreamCategoryData.PER_FLOW);
        } else if(singleHost.isSelected()){
            scd.setType(StreamCategoryData.SINGLE_HOST);
        }
    }
   
    private void addControls(){
        JPanel choicePanel = new JPanel(new GridLayout(4, 2));
        JPanel mainPanel = new JPanel();
        JPanel paramsPanel = new JPanel(new GridLayout(4, 2));

        setLayout(new BorderLayout());
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        
        choicePanel.add(resetBox = new JCheckBox("Reset on commit"));
        choicePanel.add(new JPanel());
        choicePanel.add(source = new JRadioButton("Most active uploaders"));
        choicePanel.add(destination = new JRadioButton("Most active downloaders"));
        choicePanel.add(interHost = new JRadioButton("Most active host pairs"));
        choicePanel.add(connection = new JRadioButton("Most active flows"));
        choicePanel.add(singleHost = new JRadioButton("Most active hosts"));
        
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
        
        ButtonGroup group = new ButtonGroup();
        group.add(source);
        group.add(destination);
        group.add(interHost);
        group.add(connection);
        group.add(singleHost);

        resetBox.setSelected(true);
        
        choicePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Graph Information"));
        paramsPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Top Talkers Settings"));
    }
}