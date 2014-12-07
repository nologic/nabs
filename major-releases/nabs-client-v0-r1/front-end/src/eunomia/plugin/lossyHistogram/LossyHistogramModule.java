/*
 * LossyHistogramModule.java
 *
 * Created on June 24, 2005, 4:35 PM
 *
 */

package eunomia.plugin.lossyHistogram;

import eunomia.plugin.interfaces.*;
import eunomia.plugin.alg.*;
import eunomia.config.*;
import eunomia.core.data.flow.*;
import eunomia.core.data.streamData.StreamDataSource;
import eunomia.plugin.hostView.*;
import eunomia.util.Util;
import eunomia.util.number.*;

import org.jfree.chart.renderer.category.*;
import org.jfree.chart.*;
import org.jfree.chart.plot.*;
import org.jfree.chart.axis.*;
import org.jfree.chart.labels.*;
import org.jfree.ui.*;

import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.net.InetAddress;
import javax.swing.event.*;
import org.apache.log4j.Logger;
import org.jfree.chart.entity.*;
import org.jfree.chart.title.TextTitle;


/**
 *
 * @author Mikhail Sosonkin
 */

public class LossyHistogramModule implements Module, ConfigChangeListener,
        RefreshNotifier, ActionListener, PopupMenuListener {
    
    private StreamCategoryData scd;
    private JFreeChart hostChart;
    private ChartPanel chartComp;
    private LossyCounter lossy;
    private CategoryPlot cPlot;
    private HistogramController cont;
    private FlowComparator comp;
    private FlowProc proc;
    private Filter filter;
    private StreamDataSource ds;
    private String title;
    
    private JMenuItem moveHost;
    private JMenuItem removeHost;
    private CategoryItemEntity selectedEntity;
    private TableEntry selectedEntry;
    private HostView hostView;
    
    private static Logger logger;
    
    static {
        logger = Logger.getLogger(LossyHistogramModule.class);
    }
    
    public LossyHistogramModule() {
        filter = new Filter();
        comp = new FlowComparator();
        lossy = new LossyCounter(comp);
        scd = new StreamCategoryData();
        cont = new HistogramController(comp, lossy, scd, this);
        scd.setLossyCounter(lossy);
        proc = new FlowProc(lossy);
        
        proc.setFilter(filter);
        
        hostChart = ChartFactory.createStackedBarChart3D(null, null, null, scd, PlotOrientation.VERTICAL, true, true, false);
        
        cPlot = hostChart.getCategoryPlot();
        CategoryAxis axis = cPlot.getDomainAxis();
        NumberAxis valueAxis = (NumberAxis)cPlot.getRangeAxis();
        axis.setCategoryLabelPositions(CategoryLabelPositions.createUpRotationLabelPositions(Math.PI / 8.0));
        valueAxis.setNumberFormatOverride(new NumberFormater());
        
        StackedBarRenderer3D renderer = new StackedBarRenderer3DMod();
        renderer.setBaseToolTipGenerator(scd);
        renderer.setItemLabelGenerator(scd);
        renderer.setItemLabelsVisible(true);
        renderer.setDrawBarOutline(false);
        renderer.setPositiveItemLabelPosition(new ItemLabelPosition(ItemLabelAnchor.OUTSIDE12, TextAnchor.CENTER));
        cPlot.setRenderer(renderer);
        
        configurationChanged();
        chartComp = new ChartPanel(hostChart, true);
        
        addToPopupMenu(chartComp.getPopupMenu());
        
        Settings.addConfigChangeListener(this);
    }

    private void addToPopupMenu(JPopupMenu menu){
        menu.setDefaultLightWeightPopupEnabled(true);
        
        moveHost = new JMenuItem("Move to HostView");
        removeHost = new JMenuItem("Block and Delete");
        menu.insert(new JPopupMenu.Separator(), 0);
        menu.insert(removeHost, 0);
        menu.insert(moveHost, 0);
        
        moveHost.addActionListener(this);
        removeHost.addActionListener(this);
        menu.addPopupMenuListener(this);
    }
    
    public void actionPerformed(ActionEvent e) {
        Object o = e.getSource();
        
        if(o == moveHost){
            if(selectedEntity != null && hostView != null){
                mostToHostView(selectedEntity.getCategory().toString(), scd.getType());
            }
        } else if(o == removeHost){
            if(selectedEntity != null && hostView != null){
                blockAndDelete(scd.getType(), true);
            }
        }
    }
    
    private void mostToHostView(String str, int type){
        String[] hosts = str.split(" - ");
        try {
            InetAddress address, address2;
                    
            switch(type){
                case StreamCategoryData.SRC_IP:
                    address = InetAddress.getByName(hosts[0]);
                    hostView.addHost(address);
                    break;
                case StreamCategoryData.DST_IP:
                    address = InetAddress.getByName(hosts[0]);
                    hostView.addHost(address);
                    break;
                case StreamCategoryData.HOST_TO_HOST:
                    address = InetAddress.getByName(hosts[0]);
                    address2 = InetAddress.getByName(hosts[1]);
                    hostView.addHost(address);
                    hostView.addHost(address2);
                    break;
                case StreamCategoryData.PER_FLOW:
                    String[] h1 = hosts[0].split(":");
                    String[] h2 = hosts[1].split(":");
                    address = InetAddress.getByName(h1[0]);
                    address2 = InetAddress.getByName(h2[0]);
                    hostView.addHost(address);
                    hostView.addHost(address2);
                    break;
            }
            blockAndDelete(type, true);
        } catch(Exception ex){
            logger.error(ex.getMessage());
            ex.printStackTrace();
        }
    }
    
    private void blockAndDelete(int type, boolean doDelete){
        FilterEntry entry = new FilterEntry();
        FilterEntry entry2 = null;
        Flow flow = selectedEntry.getFlow();
        long source;
        long destination;
        
        switch(type){
            case StreamCategoryData.SRC_IP:
                entry.setSourceIpRange(flow.getSourceIp(), flow.getSourceIp());
                break;
            case StreamCategoryData.DST_IP:
                entry.setDestinationIpRange(flow.getDestinationIp(), flow.getDestinationIp());
                break;
            case StreamCategoryData.HOST_TO_HOST:
                source = flow.getSourceIp();
                destination = flow.getDestinationIp();
                entry2 = new FilterEntry();
                
                //block is bidirectional for both hosts.
                entry.setSourceIpRange(source, source);
                entry.setDestinationIpRange(destination, destination);
                entry2.setSourceIpRange(destination, destination);
                entry2.setDestinationIpRange(source, source);
                break;
            case StreamCategoryData.PER_FLOW:
                source = flow.getSourceIp();
                destination = flow.getDestinationIp();
                int port1 = flow.getSourcePort();
                int port2 = flow.getDestinationPort();

                entry2 = new FilterEntry();
                //block is bidirectional for both hosts and ports.
                entry.setSourceIpRange(source, source);
                entry.setSourcePortRange(port1, port1);
                entry.setDestinationPortRange(port2, port2);
                entry.setDestinationIpRange(destination, destination);
                entry2.setSourceIpRange(destination, destination);
                entry2.setSourcePortRange(port2, port2);
                entry2.setDestinationPortRange(port1, port1);
                entry2.setDestinationIpRange(source, source);
                break;
        }
        filter.addFilterBlack(entry);
        if(entry2 != null){
            filter.addFilterBlack(entry2);
        }
        if(doDelete){
            lossy.deleteEntry(selectedEntry);
        }
    }
    
    public void popupMenuCanceled(PopupMenuEvent e){
    }
    
    public void popupMenuWillBecomeInvisible(PopupMenuEvent e){
    }
    
    public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
        JPopupMenu menu = chartComp.getPopupMenu();
        Point mPoint;
        Point cPoint;
        
        PointerInfo pInfo = MouseInfo.getPointerInfo();
        mPoint = pInfo.getLocation();
        cPoint = chartComp.getLocationOnScreen();
        selectedEntity = (CategoryItemEntity)chartComp.getEntityForPoint(mPoint.x - cPoint.x, mPoint.y - cPoint.y);
        selectedEntry = null;
        if(selectedEntity != null){
            String str = selectedEntity.getCategory().toString();
            selectedEntry = scd.getTableEntry(selectedEntity.getCategoryIndex());

            moveHost.setText("Move " + str + " to HostView");
            removeHost.setText("Remove " + str + " From This Chart");
        } else {
            moveHost.setText("Move to HostView");
            removeHost.setText("Block and Delete");
        }
        moveHost.setEnabled(selectedEntity != null);
        removeHost.setEnabled(selectedEntity != null);
    }
    
    public ModularFlowProcessor getFlowPocessor() {
        return proc;
    }
    
    public JComponent getJComponent() {
        return chartComp;
    }
    
    public RefreshNotifier getRefreshNotifier() {
        return this;
    }
    
    public void updateData(){
        if(chartComp.isShowing()){
            scd.updateData();
        }
    }
    
    public void configurationChanged() {
        CategoryItemRenderer renderer = cPlot.getRenderer();
        for(int i = 0; i < Flow.NUM_TYPES; i++){
            renderer.setSeriesPaint(i, Settings.getTypeColor(i));
        }
    }
    
    public JComponent getControlComponent() {
        return cont;
    }
    
    public boolean allowFullscreen() {
        return true;
    }
    
    public boolean allowFilters() {
        return true;
    }
    
    public boolean isConfigSeparate() {
        return true;
    }
    
    public boolean allowToolbar() {
        return true;
    }
    
    public boolean isControlSeparate() {
        return false;
    }
    
    public void showLegend(boolean b) {
    }
    
    public void showTitle(boolean b) {
    }
    
    public String getTitle() {
        if(title == null){
            return "Heavy-Hitter View";
        }
        return title.replace('\n', ' ');
    }
    
    public void setProperty(String name, Object value) {
        if(name.equals("HostView") && value instanceof HostView){
            hostView = (HostView)value;
        }
    }
    
    public Object getProperty(String name) {
        return hostView;
    }
    
    public void start() {
        proc.setDoProc(true);
    }
    
    public void stop() {
        proc.setDoProc(false);
    }
    
    public void reset() {
        lossy.reset();
        setGenTitle();
    }
    
    public Filter getFilter() {
        return proc.getFilter();
    }
    
    public void setGenTitle(){
        StringBuilder builder = new StringBuilder();
        
        if(ds != null){
            builder.append("View From ");
            builder.append(ds.toString());
        }
        builder.append("\nTop ");
        builder.append(Integer.toString(lossy.getTableSize()));
        switch(scd.getType()){
            case StreamCategoryData.SRC_IP: 
                builder.append(" Uploaders ");
                break;
            case StreamCategoryData.DST_IP: 
                builder.append(" Dowloaders ");
                break;
            case StreamCategoryData.HOST_TO_HOST:
                builder.append(" Host Pairs ");
                break;
            case StreamCategoryData.PER_FLOW:
                builder.append(" Flows ");
                break;
        }
        
        builder.append(" Since ");
        builder.append(Util.getTimeStamp(System.currentTimeMillis(), true, true));
        
        title = builder.toString();
        Font font = new Font("Verdana", Font.BOLD, 12);
        hostChart.setTitle(new TextTitle(title, font));
    }

    public void setStream(StreamDataSource sds) {
        ds = sds;
        setGenTitle();
    }
}