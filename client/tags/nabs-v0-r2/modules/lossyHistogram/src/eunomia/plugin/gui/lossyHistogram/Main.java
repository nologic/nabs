/*
 * LossyHistogramModule.java
 *
 * Created on June 24, 2005, 4:35 PM
 *
 */

package eunomia.plugin.gui.lossyHistogram;

import eunomia.plugin.interfaces.*;
import eunomia.config.*;
import eunomia.core.receptor.listeners.MessageReceiver;
import eunomia.util.number.*;
import eunomia.core.receptor.Receptor;
import eunomia.messages.Message;

import org.jfree.chart.renderer.category.*;
import org.jfree.chart.*;
import org.jfree.chart.plot.*;
import org.jfree.chart.axis.*;
import org.jfree.chart.labels.*;
import org.jfree.ui.*;

import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import javax.swing.event.*;
import org.apache.log4j.Logger;
import org.jfree.chart.entity.*;
import org.jfree.chart.title.TextTitle;
import java.io.*;
import eunomia.receptor.module.NABFlow.NABFilterEntry;
import eunomia.flow.Filter;
import eunomia.receptor.module.NABFlow.NABFlow;
import eunomia.messages.module.msg.GenericModuleMessage;
import eunomia.messages.receptor.ModuleHandle;
import eunomia.plugin.msg.ModifyGraphMessage;
import java.util.*;

/**
 *
 * @author Mikhail Sosonkin
 */

public class Main implements GUIModule, ConfigChangeListener,
        ActionListener, PopupMenuListener, MessageReceiver {
    
    private StreamCategoryData scd;
    private JFreeChart hostChart;
    private ChartPanel chartComp;
    private CategoryPlot cPlot;
    private HistogramController cont;
    private String title;
    
    private JMenu moveHost;
    private JMenuItem removeHost;
    private CategoryItemEntity selectedEntity;
    private LCTableEntry selectedEntry;
    private Receptor receptor;
    private LCTableEntry[] lossyTable;
    private HashMap itemToHandle;
    
    private int topCount;
    private int scdType;
    
    private static Logger logger;
    
    static {
        logger = Logger.getLogger(Main.class);
    }
    
    public Main() {
        scd = new StreamCategoryData();
        cont = new HistogramController(scd);
        itemToHandle = new HashMap();
        topCount = -1;
        scdType = -1;

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
        
        moveHost = new JMenu("Move to HostView");
        removeHost = new JMenuItem("Block and Delete");
        menu.insert(new JPopupMenu.Separator(), 0);
        menu.insert(removeHost, 0);
        menu.insert(moveHost, 0);
        
        removeHost.addActionListener(this);
        menu.addPopupMenuListener(this);
    }
    
    public void actionPerformed(ActionEvent e) {
        Object o = e.getSource();
 
        if(itemToHandle.containsKey(o)){
            if(selectedEntity != null){
                blockAndDelete(scd.getType(), (ModuleHandle)itemToHandle.get(o));
            }
        } else if(o == removeHost){
            if(selectedEntity != null){
                blockAndDelete(scd.getType(), null);
            }
        }
    }
        
    private void blockAndDelete(int type, ModuleHandle handle){
        NABFilterEntry entry = new NABFilterEntry();
        NABFilterEntry entry2 = null;
        NABFlow flow = selectedEntry.getFlow();
        long source;
        long destination;
 
        switch(type){
            case StreamCategoryData.SINGLE_HOST:
            case StreamCategoryData.SRC_IP:
                entry.setSourceIpRange(flow.getSourceIP(), flow.getSourceIP());
                break;
            case StreamCategoryData.DST_IP:
                entry.setDestinationIpRange(flow.getDestinationIP(), flow.getDestinationIP());
                break;
            case StreamCategoryData.HOST_TO_HOST:
                source = flow.getSourceIP();
                destination = flow.getDestinationIP();
                entry2 = new NABFilterEntry();
 
                //block is bidirectional for both hosts.
                entry.setSourceIpRange(source, source);
                entry.setDestinationIpRange(destination, destination);
                entry2.setSourceIpRange(destination, destination);
                entry2.setDestinationIpRange(source, source);
                break;
            case StreamCategoryData.PER_FLOW:
                source = flow.getSourceIP();
                destination = flow.getDestinationIP();
                int port1 = flow.getSourcePort();
                int port2 = flow.getDestinationPort();
 
                entry2 = new NABFilterEntry();
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
        
        ModifyGraphMessage mgm = new ModifyGraphMessage();
        mgm.setEntry1(entry);
        mgm.setEntry2(entry2);
        mgm.setFlowID(selectedEntry.getFlowId());
        mgm.setHandle(handle);
        
        GenericModuleMessage gmm = receptor.getManager().prepareGenericMessage(this);
        try {
            ObjectOutput oo = new ObjectOutputStream(gmm.getOutputStream());
            oo.writeObject(mgm);
            receptor.getManager().sendGenericMessage(this, gmm);
        } catch (Exception e){
            e.printStackTrace();
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
        
        for(int i = 0; i < moveHost.getItemCount(); i++){
            itemToHandle.remove(moveHost.getItem(i));
        }
        moveHost.removeAll();
        
        if(selectedEntity != null){
            String str = selectedEntity.getCategory().toString();
            selectedEntry = scd.getTableEntry(selectedEntity.getCategoryIndex());
            
            moveHost.setText("Move " + str + " to HostView");
            removeHost.setText("Remove " + str + " From This Chart");
            java.util.List hvList = receptor.getManager().getModuleList("hostView");
            if(hvList.size() > 0){
                moveHost.setEnabled(true);
                Iterator it = hvList.iterator();
                while (it.hasNext()) {
                    GUIModule mod = (GUIModule) it.next();
                    ModuleHandle hand = receptor.getManager().getModuleHandle(mod);
                    JMenuItem item = moveHost.add(hand.toString());
                    itemToHandle.put(item, hand);
                    item.addActionListener(this);
                }
            } else {
                moveHost.setEnabled(false);
            }
        } else {
            moveHost.setText("Move to HostView");
            removeHost.setText("Block and Delete");
        }
        moveHost.setEnabled(selectedEntity != null);
        removeHost.setEnabled(selectedEntity != null);
    }
    
    public JComponent getJComponent() {
        return chartComp;
    }
    
    public void updateData(){
        if(chartComp.isShowing()){
            scd.updateData();
        }
    }
    
    public void configurationChanged() {
        CategoryItemRenderer renderer = cPlot.getRenderer();
        for(int i = 0; i < NABFlow.NUM_TYPES; i++){
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
    }
    
    public Object getProperty(String name) {
        return null;
    }
    
    public Filter getFilter() {
        return null;
    }
    
    public void setGenTitle(){
        if(scd.getType() != scdType || scd.getColumnCount() != topCount){
            scdType = scd.getType();
            topCount = scd.getColumnCount();
            StringBuilder builder = new StringBuilder();

            builder.append("\nTop ");
            builder.append(Integer.toString(scd.getColumnCount()));
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
                case StreamCategoryData.SINGLE_HOST:
                    builder.append(" Hosts ");
                    break;
            }

            title = builder.toString();
            Font font = new Font("Verdana", Font.BOLD, 12);
            hostChart.setTitle(new TextTitle(title, font));
        }
    }
        
    public MessageReceiver getReceiver() {
        return this;
    }
    
    public void updateStatus(InputStream in) throws IOException {
        DataInputStream din = new DataInputStream(in);
        int tableSize = din.readInt();
        
        if(tableSize == 0){
            return;
        }
        
        if(lossyTable == null || lossyTable.length != tableSize){
            lossyTable = new LCTableEntry[tableSize];
        }
        
        for(int i = 0; i < tableSize; i++){
            LCTableEntry entry = lossyTable[i];
            boolean isNNull = din.readBoolean();
            if(isNNull){
                if(entry == null){
                    entry = new LCTableEntry();
                    lossyTable[i] = entry;
                }
                
                entry.getFlow().readFromDataStream(din);
                entry.setFlowId(din.readInt());
                entry.setIdleTime(din.readInt());
                entry.setFrequency(din.readInt());
                entry.setStartTime(din.readLong());
                for(int k = 0; k < NABFlow.NUM_TYPES; ++k){
                    int f = din.readInt();
                    entry.setFrequencyType(k, f);
                }
            }
        }

        scd.updateLossyTable(lossyTable);
        updateData();
        setGenTitle();
    }
    
    public void messageResponse(Message msg) {
        logger.info(msg);
    }
    
    public void getControlData(OutputStream out) throws IOException {
        cont.getControlData(out);
    }
    
    public void setControlData(InputStream in) throws IOException {
        cont.setControlData(in);
    }
    
    public void setReceptor(Receptor rec) {
        receptor = rec;
    }
    
    public Receptor getReceptor() {
        return receptor;
    }
}