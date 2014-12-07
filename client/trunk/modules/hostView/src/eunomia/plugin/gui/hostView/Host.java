/*
 * Host.java
 *
 * Created on August 9, 2005, 4:37 PM
 *
 */

package eunomia.plugin.gui.hostView;

import eunomia.receptor.module.NABFlow.NABFlow;
import eunomia.messages.receptor.ModuleHandle;
import com.vivic.eunomia.module.frontend.FrontendProcessorModule;
import com.vivic.eunomia.sys.frontend.ConsoleModuleManager;
import com.vivic.eunomia.sys.frontend.GlobalSettings;
import eunomia.config.ConfigChangeListener;
import com.vivic.eunomia.sys.util.Util;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.text.DecimalFormat;
import java.text.Format;

import java.util.HashMap;
import java.util.Iterator;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.ui.RectangleInsets;


/**
 *
 * @author Mikhail Sosonkin
 */

public class Host extends JPanel implements ConfigChangeListener,
        ActionListener, Runnable {
    private static Format form;
    private static Font labelFont;
    private static Insets tickLabelInset;
    private static RectangleInsets plotInset;

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
    private JPopupMenu detailsMenu;
    
    private JFreeChart chart;
    private NumberAxis valueAxis;
    private CategoryPlot cPlot;
    private HashMap itemToHandle;
    private FrontendProcessorModule module;
    private GlobalSettings gSet;
    
    private boolean firstUpdate;
    
    //data
    private HistoryDisplay historyData;
    private long startTime;
    private long totalBytes, inBytes, outBytes;
    private double outRate, inRate;
    
    static {
        form = DecimalFormat.getInstance();
        labelFont = new Font("SansSerif", Font.PLAIN, 9);
        tickLabelInset = new Insets(20, 0, 20, 0);
        plotInset = new RectangleInsets(0, 0, 0, 2);
    }
    
    public Host(long ip, InetAddress hostAddr, FrontendProcessorModule mod) {
        module = mod;
        host = hostAddr;
        firstUpdate = true;
        detailsMenu = new JPopupMenu();
        itemToHandle = new HashMap();
        
        addControls();
        
        Dimension dim = getPreferredSize();
        dim.setSize(-1, 90);
        setPreferredSize(dim);
        
        gSet = ((Main)mod).getReceptor().getGlobalSettings();
        gSet.addConfigChangeListener(this);
        configurationChanged();
    }
    
    public void hostRemoved(){
    }
    
    public void readHost(DataInputStream din) throws IOException {
        startTime = din.readLong();
        totalBytes = din.readLong();
        inBytes = din.readLong();
        outBytes = din.readLong();
        outRate = din.readDouble();
        inRate = din.readDouble();
        
        if(firstUpdate){
            borderThread();
            firstUpdate = false;
        }
        
        historyData.readHistory(din);
    }
    public void updateData() {
        if(isVisible()){
            rangeTopVal.setText(Util.convertBytes(valueAxis.getUpperBound(), true));
            rangeBotVal.setText(Util.convertBytes(valueAxis.getLowerBound(), true));
     
            totalData.setText(Util.convertBytes(totalBytes, true));
            inData.setText(Util.convertBytes(inBytes, true));
            outData.setText(Util.convertBytes(outBytes, true));
            upRate.setText(Util.convertBytesRate(outRate, true));
            downRate.setText(Util.convertBytesRate(inRate, true));
            historyData.updateData();
        }
    }
    
    public void actionPerformed(ActionEvent e) {
        Object o = e.getSource();
        
        if(o == details){
            configurePopupMenu();
            detailsMenu.show(details, 0, 0);
        } else if(itemToHandle.containsKey(o)){
            addToDetails((ModuleHandle)itemToHandle.get(o));
        }
    }
    
    private void addToDetails(ModuleHandle handle){
        ((Main)module).addHostToDetails(Util.getLongIp(host), handle);
    }
    
    private void configurePopupMenu(){
        detailsMenu.removeAll();
        ConsoleModuleManager mmon = ((Main)module).getReceptor().getManager();
        java.util.List hvList = mmon.getModuleHandles("hostDetails", ModuleHandle.TYPE_PROC);
        if(hvList.size() > 0){
            Iterator it = hvList.iterator();
            while (it.hasNext()) {
                ModuleHandle hand = (ModuleHandle) it.next();
                JMenuItem item = detailsMenu.add(hand.toString());
                itemToHandle.put(item, hand);
                item.addActionListener(this);
            }
        } else {
            detailsMenu.add("No hostDetails instances found");
        }
    }
    
    private void addControls(){
        JMenuBar menuBar = new JMenuBar();
        setLayout(new BorderLayout());
        
        JPanel topPanel = new JPanel(new BorderLayout());
        JPanel totalsPanel = new JPanel(new GridLayout(1, 6));
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
        
        Util.getTimeStamp(sb, startTime, true, true);
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
        historyData = new HistoryDisplay();
        chart = ChartFactory.createStackedAreaChart(null, null, null, historyData, PlotOrientation.VERTICAL, false, false, false);
        
        cPlot = chart.getCategoryPlot();
        chart.setAntiAlias(false);
        cPlot.setInsets(plotInset);
        cPlot.setAxisOffset(new RectangleInsets(0, 0, 0, 0));
        cPlot.setRangeGridlinesVisible(false);
        cPlot.setBackgroundAlpha(0.5f);
        //cPlot.setDataAreaRatio(1.0);

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
    
    public InetAddress getHost(){
        return host;
    }
    
    public void configurationChanged() {
        CategoryItemRenderer renderer = cPlot.getRenderer();
        for(int i = 0; i < NABFlow.NUM_TYPES; i++){
            renderer.setSeriesPaint(i, gSet.getTypeColor(i));
        }
    }
    
    private JLabel makeLabel(String str){
        JLabel label = new JLabel(str);
        
        label.setFont(labelFont);
        
        return label;
    }
}