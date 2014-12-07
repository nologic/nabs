/*
 * HistogramController.java
 *
 * Created on July 14, 2005, 8:28 PM
 *
 */

package eunomia.plugin.lossyHistogram;
import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import eunomia.core.data.flow.FlowComparator;
import eunomia.plugin.alg.LossyCounter;

/**
 *
 * @author Mikhail Sosonkin
 */

public class HistogramController extends JPanel implements ActionListener {
    private FlowComparator comp;
    private JRadioButton source;
    private JRadioButton destination;
    private JRadioButton interHost;
    private JRadioButton connection;
    private JTextField eField, sField, numHosts, timeout;
    private JButton setFields;
    private JButton setReset;
    private LossyCounter counter;
    private StreamCategoryData graphModel;
    private LossyHistogramModule mod;
    
    public HistogramController(FlowComparator fc, LossyCounter c, StreamCategoryData scd, LossyHistogramModule lhm) {
        counter = c;
        graphModel = scd;
        comp = fc;
        mod = lhm;

        setComp(fc);
        addControls();
        
        source.setSelected(true);
    }

    public FlowComparator getComp() {
        return comp;
    }

    public void setComp(FlowComparator comp) {
        this.comp = comp;
    }
    
    private void reset(boolean reset){
        comp.allFalse();
        if(source.isSelected()){
            comp.setSrcIp(true);
            graphModel.setType(StreamCategoryData.SRC_IP);
        } else if(destination.isSelected()){
            comp.setDstIp(true);
            graphModel.setType(StreamCategoryData.DST_IP);
        } else if(interHost.isSelected()){
            comp.setInterHost(true);
            graphModel.setType(StreamCategoryData.HOST_TO_HOST);
        } else if(connection.isSelected()){
            comp.setConnection(true);
            graphModel.setType(StreamCategoryData.PER_FLOW);
        }

        try {
            double ss = Double.parseDouble(sField.getText());
            double ee = Double.parseDouble(eField.getText());
            int tableSize = Integer.parseInt(numHosts.getText());
            int to = Integer.parseInt(timeout.getText());
            if(to != -1){
                to *= 1000;
            }
            counter.setParams(ee, ss, tableSize, to, reset);
        } catch(Exception ex){
            ex.printStackTrace();
        }
        mod.setGenTitle();
    }
    
    public void actionPerformed(ActionEvent e) {
        Object o = e.getSource();

        if(o == setFields){
            reset(false);
        } else if(o == setReset){
            reset(true);
        }
    }

    private void addControls(){
        JPanel choicePanel = new JPanel(new GridLayout(4, 1));
        JPanel mainPanel = new JPanel();
        JPanel paramsPanel = new JPanel(new GridLayout(4, 2));
        JPanel buttonsPanel = new JPanel(new GridLayout(1, 2));

        setLayout(new BorderLayout());
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        
        choicePanel.add(source = new JRadioButton("Most active uploaders"));
        choicePanel.add(destination = new JRadioButton("Most active downloaders"));
        choicePanel.add(interHost = new JRadioButton("Most active host pairs"));
        choicePanel.add(connection = new JRadioButton("Most active flows"));
        
        paramsPanel.add(new JLabel("Error Rate"));
        paramsPanel.add(eField = new JTextField(counter.getE() + ""));
        paramsPanel.add(new JLabel("Threshold"));
        paramsPanel.add(sField = new JTextField(counter.getS() + ""));
        paramsPanel.add(new JLabel("Number of Top Heavy-Hitters"));
        paramsPanel.add(numHosts = new JTextField(counter.getTableSize() + ""));
        paramsPanel.add(new JLabel("Idle Timeout (Seconds)"));
        paramsPanel.add(timeout = new JTextField(counter.getTimeout() + ""));

        buttonsPanel.add(setReset = new JButton("Set Values (Reset the graph)"));
        buttonsPanel.add(setFields = new JButton("Set Values (Don't reset the graph)"));
        
        mainPanel.add(choicePanel);
        mainPanel.add(paramsPanel);
        add(mainPanel, BorderLayout.NORTH);
        add(buttonsPanel, BorderLayout.SOUTH);
        
        ButtonGroup group = new ButtonGroup();
        group.add(source);
        group.add(destination);
        group.add(interHost);
        group.add(connection);
        group.add(setFields);
        
        source.addActionListener(this);
        destination.addActionListener(this);
        interHost.addActionListener(this);
        connection.addActionListener(this);
        setFields.addActionListener(this);
        setReset.addActionListener(this);
        
        choicePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Graph Information"));
        paramsPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Heavy-Hitter Settings"));
    }
}