/*
 * AlertEditor.java
 *
 * Created on July 17, 2007, 9:08 PM
 */

package eunomia.plugin.gui.networkPolicy.alert;

import com.vivic.eunomia.sys.frontend.GlobalSettings;
import eunomia.config.ConfigChangeListener;
import eunomia.plugin.com.networkPolicy.AlertItem;
import eunomia.plugin.com.networkPolicy.PolicyItem;
import eunomia.plugin.gui.networkPolicy.Main;
import eunomia.plugin.gui.networkPolicy.table.AlertsTableModel;
import eunomia.receptor.module.NABFlow.NABFlow;
import eunomia.util.SpringUtilities;
import com.vivic.eunomia.sys.util.Util;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.border.TitledBorder;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.ItemLabelAnchor;
import org.jfree.chart.labels.ItemLabelPosition;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.chart.renderer.category.StackedBarRenderer3D;
import org.jfree.ui.TextAnchor;

/**
 *
 * @author  Mikhail Sosonkin
 */
public class AlertEditor extends JPanel implements ActionListener, ConfigChangeListener {
    private AlertItem curAlert;
    private JTextArea description;
    private JTextField firstSeen;
    private JTextField flow;
    private JTextField lastSeen;
    private JTextField policy;
    private JButton save;
    private JComboBox status;
    private JTextField violations;
    private JTextField dataTransfered;
    private JLabel violatorLabel;
    
    private DistroGraphData gData;
    private JFreeChart dataChart;
    private CategoryPlot cPlot;
    private GlobalSettings gSet;
    private Main main;
    
    public AlertEditor(Main main) {
        this.main = main;
        
        addControls();
    }
    
    public void showAlert(AlertItem item) {
        if(curAlert == item){
            reload();
            return;
        }
        
        curAlert = item;
        if(item != null) {
            description.setText(item.getNotes());
            firstSeen.setText(item.getFirstSeenString());
            lastSeen.setText(item.getLastSeenString());
            flow.setText(item.getFlowId().toString());
            policy.setText(item.getPolicyItem().toString());
            status.setSelectedIndex(item.getStatus());
            violations.setText(Long.toString(item.getViolations()));
            dataTransfered.setText(Util.convertBytes(item.getFlowId().getBytes(), true));
            gData.updateData(item.getFlowId().getByteTypes());
            violatorLabel.setText( item.getPolicyItem().getPolicyType() == PolicyItem.REAL_TIME?"Violating Flow":"Violating IP" );
        } else {
            description.setText("");
            firstSeen.setText("");
            lastSeen.setText("");
            flow.setText("");
            policy.setText("");
            violations.setText("");
            dataTransfered.setText("");
        }
    }
    
    public AlertItem getCurrentAlert() {
        return curAlert;
    }
    
    public void reload() {
        if(curAlert != null) {
            firstSeen.setText(curAlert.getFirstSeenString());
            lastSeen.setText(curAlert.getLastSeenString());
            flow.setText(curAlert.getFlowId().toString());
            violations.setText(Long.toString(curAlert.getViolations()));
            dataTransfered.setText(Util.convertBytes(curAlert.getFlowId().getBytes(), true));
            gData.updateData(curAlert.getFlowId().getByteTypes());
        }
    }
    
    public void setGlobalSettings(GlobalSettings globalSet) {
        gSet = globalSet;
        configurationChanged();
    }
    
    private void saveAlert() {
        curAlert.setNotes(description.getText());
        curAlert.setStatus(status.getSelectedIndex());
        try {
            main.saveAlert(curAlert);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    
    public void actionPerformed(ActionEvent e) {
        Object o = e.getSource();
        
        if(o == save) {
            saveAlert();
        }
    }
    
    private JComponent makeGraph() {
        gData = new DistroGraphData();
        
        dataChart = ChartFactory.createStackedBarChart3D(null, null, null, gData, PlotOrientation.VERTICAL, false, true, false);
        cPlot = dataChart.getCategoryPlot();
        cPlot.setRangeGridlinesVisible(false);
        cPlot.setBackgroundAlpha(0.5f);
        
        NumberAxis valueAxis = (NumberAxis)cPlot.getRangeAxis();
        CategoryAxis categoryAxis = cPlot.getDomainAxis();
        valueAxis.setTickLabelsVisible(false);
        categoryAxis.setTickLabelsVisible(false);
        categoryAxis.setUpperMargin(0.0);
        categoryAxis.setLowerMargin(0.0);
        valueAxis.setUpperMargin(0.0);
        valueAxis.setLowerMargin(0.0);
        
        StackedBarRenderer3D renderer = (StackedBarRenderer3D)cPlot.getRenderer();
        renderer.setItemLabelsVisible(true);
        renderer.setDrawBarOutline(false);
        renderer.setPositiveItemLabelPosition(new ItemLabelPosition(ItemLabelAnchor.OUTSIDE12, TextAnchor.CENTER));
        
        ChartPanel panel = new ChartPanel(dataChart, true);
        panel.setPreferredSize(new Dimension(100, 0));
        panel.setRefreshBuffer(true);
        panel.setMouseZoomable(false);
        
        return panel;
    }

    private void addControls() {
        JPanel mainPanel = new JPanel(new GridLayout(1, 2, 0, 10));
        JPanel bottomPanel;
        JPanel buttonsPanel;
        JPanel topPanel;
        JPanel graphPanel = new JPanel(new BorderLayout());

        bottomPanel = new JPanel(new BorderLayout());
        description = new JTextArea();
        topPanel = new JPanel(new SpringLayout());
        policy = new JTextField();
        violations = new JTextField();
        flow = new JTextField();
        firstSeen = new JTextField();
        lastSeen = new JTextField();
        dataTransfered = new JTextField();
        status = new JComboBox(AlertsTableModel.status);
        buttonsPanel = new JPanel(new BorderLayout());

        setLayout(new BorderLayout());

        buttonsPanel.add(save = new JButton("Save"), BorderLayout.EAST);
        bottomPanel.add(new JScrollPane(description), BorderLayout.CENTER);
        bottomPanel.add(buttonsPanel, BorderLayout.SOUTH);

        JLabel label;
        topPanel.add(label = new JLabel("Policy Violated:"));
        topPanel.add(policy);
        topPanel.add(new JLabel("Number of Violations:"));
        topPanel.add(violations);
        topPanel.add(violatorLabel = new JLabel("Violating Flow:"));
        topPanel.add(flow);
        topPanel.add(new JLabel("Data Transfered:"));
        topPanel.add(dataTransfered);
        topPanel.add(new JLabel("First Seen:"));
        topPanel.add(firstSeen);
        topPanel.add(new JLabel("Last Seen:"));
        topPanel.add(lastSeen);
        topPanel.add(new JLabel("Status:"));
        topPanel.add(status);
        SpringUtilities.makeCompactGrid(topPanel, 7, 2, 2, 2, 8, 2);
        
        policy.setEditable(false);
        policy.setBorder(BorderFactory.createEmptyBorder());
        policy.setBackground(label.getBackground());
        policy.setFont(label.getFont());
        
        violations.setEditable(false);
        violations.setBorder(BorderFactory.createEmptyBorder());
        violations.setBackground(label.getBackground());
        violations.setFont(label.getFont());

        flow.setEditable(false);
        flow.setBorder(BorderFactory.createEmptyBorder());
        flow.setBackground(label.getBackground());
        flow.setFont(label.getFont());

        dataTransfered.setEditable(false);
        dataTransfered.setBorder(BorderFactory.createEmptyBorder());
        dataTransfered.setBackground(label.getBackground());
        dataTransfered.setFont(label.getFont());

        firstSeen.setEditable(false);
        firstSeen.setBorder(BorderFactory.createEmptyBorder());
        firstSeen.setBackground(label.getBackground());
        firstSeen.setFont(label.getFont());

        lastSeen.setEditable(false);
        lastSeen.setBorder(BorderFactory.createEmptyBorder());
        lastSeen.setBackground(label.getBackground());
        lastSeen.setFont(label.getFont());
        
        graphPanel.add(makeGraph());
        
        mainPanel.add(topPanel);
        mainPanel.add(bottomPanel);
        add(mainPanel, BorderLayout.CENTER);
        add(graphPanel, BorderLayout.WEST);
        
        graphPanel.setPreferredSize(new Dimension(60, 0));
        
        Font font = new Font("Tahoma", 1, 16);
        save.setFont(font);
        bottomPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(), "Notes:", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 12)));
        graphPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Content:", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 10)));
        
        save.addActionListener(this);
    }

    public void configurationChanged() {
        CategoryItemRenderer renderer = cPlot.getRenderer();
        for(int i = 0; i < NABFlow.NUM_TYPES; i++){
            renderer.setSeriesPaint(i, gSet.getTypeColor(i));
        }
    }
}