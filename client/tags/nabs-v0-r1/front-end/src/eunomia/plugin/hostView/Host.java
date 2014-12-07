/*
 * Host.java
 *
 * Created on August 9, 2005, 4:37 PM
 *
 */

package eunomia.plugin.hostView;

import eunomia.config.*;
import eunomia.util.*;
import eunomia.core.data.flow.*;
import eunomia.core.managers.*;
import eunomia.plugin.interfaces.*;

import java.awt.*;
import java.awt.event.*;
import java.net.*;
import java.text.*;
import java.util.*;
import javax.swing.*;

import org.jfree.chart.*;
import org.jfree.chart.axis.*;
import org.jfree.chart.plot.*;
import org.jfree.chart.renderer.category.*;
import org.jfree.ui.*;

/**
 *
 * @author Mikhail Sosonkin
 */

public class Host extends JPanel implements RefreshNotifier, ConfigChangeListener,
                    ActionListener, Runnable {
    private static Format form;
    private static Font labelFont;
    private static Insets tickLabelInset;
    private static RectangleInsets plotInset;
    private static DetailsFrame dView;
    
    private FilterEntry entrySrc;
    private FilterEntry entryDst;
    private HostData hData;
    private InetAddress host;
    
    private JLabel totalData;
    private JLabel inData;
    private JLabel outData;
    private JLabel conCount;
    private JLabel upRate;
    private JLabel downRate;
    
    private JLabel rangeTopVal;
    private JLabel rangeBotVal;
    
    private JButton details;
    
    private JFreeChart chart;
    private NumberAxis valueAxis;
    private CategoryPlot cPlot;
    private HistoryData histryData;
    
    static {
        form = DecimalFormat.getInstance();
        labelFont = new Font("SansSerif", Font.PLAIN, 9);
        tickLabelInset = new Insets(20, 0, 20, 0);
        plotInset = new RectangleInsets(0, 0, 0, 2);
        dView = new DetailsFrame();
        DataManager.ins.registerWithUpdater(dView);
    }

    public Host(long ip, InetAddress hostAddr) {
        host = hostAddr;
        hData = new HostData();
        
        addControls();
        
        Dimension dim = getPreferredSize();
        dim.setSize(-1, 90);
        setPreferredSize(dim);
        
        configurationChanged();
        Settings.addConfigChangeListener(this);
    }
    
    public void hostRemoved(){
        dView.removeHost(host);
    }
    
    public void updateData() {
        hData.computeRates();
        hData.updateHistory();
        
        if(isVisible()){
            rangeTopVal.setText(Util.convertBytes(valueAxis.getUpperBound(), true));
            rangeBotVal.setText(Util.convertBytes(valueAxis.getLowerBound(), true));
            
            totalData.setText(Util.convertBytes(hData.getTotalBytes(), true));
            inData.setText(Util.convertBytes(hData.getInBytes(), true));
            outData.setText(Util.convertBytes(hData.getOutBytes(), true));
            upRate.setText(Util.convertBytesRate(hData.getOutRate(), true));
            downRate.setText(Util.convertBytesRate(hData.getInRate(), true));
            conCount.setText(Integer.toString(hData.conversationCount()));

            histryData.updateData();
        }
    }

    public void actionPerformed(ActionEvent e) {
        Object o = e.getSource();
        
        if(o == details){
            dView.showHost(host, hData);
        }
    }

    public void newFlowSource(Flow flow) {
        hData.outgoing(flow);
    }
    
    public void newFlowDestination(Flow flow) {
        hData.incoming(flow);
    }

    private void addControls(){
        setLayout(new BorderLayout());
        
        JPanel topPanel = new JPanel(new BorderLayout());
        JPanel totalsPanel = new JPanel(new GridLayout(1, 8));
        JPanel consPanel = new JPanel(new GridLayout(2, 4));
        JPanel ratesPanel = new JPanel(new BorderLayout());
        JPanel ratesLabelsPanel = new JPanel(new GridLayout(2, 1));
        JPanel ratesNumbersPanel = new JPanel(new GridLayout(2, 1));
        JPanel graphPanel = new JPanel(new BorderLayout());
        
        totalsPanel.add(makeLabel("Total:"));
        totalsPanel.add(totalData = makeLabel("0"));
        totalsPanel.add(makeLabel("Ingress: "));
        totalsPanel.add(inData = makeLabel("0"));
        totalsPanel.add(makeLabel("Egress: "));
        totalsPanel.add(outData = makeLabel("0"));
        totalsPanel.add(makeLabel("Flows: "));
        totalsPanel.add(conCount = makeLabel("0"));
        
        ratesLabelsPanel.add(makeLabel("UL: "));
        ratesLabelsPanel.add(makeLabel("DL: "));
        ratesNumbersPanel.add(upRate = makeLabel(""));
        ratesNumbersPanel.add(downRate = makeLabel(""));

        ratesPanel.add(ratesLabelsPanel, BorderLayout.WEST);
        ratesPanel.add(ratesNumbersPanel);
        ratesPanel.add(details = new JButton("Details"), BorderLayout.SOUTH);
        
        topPanel.add(totalsPanel, BorderLayout.NORTH);
        
        graphPanel.add(createChart());
        graphPanel.add(ratesPanel, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);
        add(graphPanel);
        
        setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), genHostString("Resolving")));
        borderThread();
        
        totalData.setHorizontalAlignment(JLabel.LEFT);
        inData.setHorizontalAlignment(JLabel.LEFT);
        outData.setHorizontalAlignment(JLabel.LEFT);
        
        ratesPanel.setPreferredSize(new Dimension(80, 0));
        details.addActionListener(this);
        details.setFont(labelFont);
        details.setPreferredSize(new Dimension(0, 20));
    }
    
    private void borderThread(){
        new Thread(this).start();
    }
    
    public void run(){
        setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), genHostString(host.getHostName())));
    }
    
    private String genHostString(String hostName){
        StringBuilder sb = new StringBuilder();
        
        Util.getTimeStamp(sb, hData.getStartTime(), true, true);
        sb.append(" - ");
        sb.append(host.getHostAddress());
        if(!host.getHostAddress().equals(hostName)){
            sb.append(" (");
            sb.append(hostName);
            sb.append(")");
        }
        
        return sb.toString();
    }
    
    private JComponent createChart(){
        histryData = new HistoryData(85);
        chart = ChartFactory.createStackedAreaChart(null, null, null, histryData, PlotOrientation.VERTICAL, false, false, false);
        
        cPlot = chart.getCategoryPlot();
        chart.setAntiAlias(false);
        cPlot.setInsets(plotInset);
        cPlot.setAxisOffset(new RectangleInsets(0, 0, 0, 0));
        cPlot.setRangeGridlinesVisible(false);
        cPlot.setBackgroundAlpha(0.5f);
        cPlot.setDataAreaRatio(1.0);
        
        hData.setHistoryData(histryData);
        
        valueAxis = (NumberAxis)cPlot.getRangeAxis();
        CategoryAxis categoryAxis = cPlot.getDomainAxis();
        valueAxis.setTickLabelsVisible(false);
        categoryAxis.setTickLabelsVisible(false);
        categoryAxis.setUpperMargin(0.0);
        categoryAxis.setLowerMargin(0.0);
        valueAxis.setUpperMargin(0.0);
        valueAxis.setLowerMargin(0.0);
        
        ChartPanel cPanel = new ChartPanel(chart, true, true, true, false, false);
        cPanel.setRefreshBuffer(true);
        cPanel.setMouseZoomable(false);
        
        JPanel mainPanel = new JPanel(new BorderLayout());
        JPanel rangePanel = new JPanel(new BorderLayout());
        
        rangePanel.add(rangeTopVal = makeLabel("0.0B"), BorderLayout.NORTH);
        rangePanel.add(rangeBotVal = makeLabel("0.0B"), BorderLayout.SOUTH);
        mainPanel.add(cPanel);
        mainPanel.add(rangePanel, BorderLayout.WEST);
        
        rangePanel.setPreferredSize(new Dimension(50, 0));
        rangeTopVal.setHorizontalAlignment(JLabel.RIGHT);
        rangeBotVal.setHorizontalAlignment(JLabel.RIGHT);
        
        return mainPanel;
    }

    public FilterEntry getEntrySrc() {
        return entrySrc;
    }

    public void setEntrySrc(FilterEntry entrySrc) {
        this.entrySrc = entrySrc;
    }

    public FilterEntry getEntryDst() {
        return entryDst;
    }

    public void setEntryDst(FilterEntry entryDst) {
        this.entryDst = entryDst;
    }
    
    public void configurationChanged() {
        CategoryItemRenderer renderer = cPlot.getRenderer();
        for(int i = 0; i < Flow.NUM_TYPES; i++){
            renderer.setSeriesPaint(i, Settings.getTypeColor(i));
        }
    }
    
    private JLabel makeLabel(String str){
        JLabel label = new JLabel(str);
        
        label.setFont(labelFont);
        
        return label;
    }
}