/*
 * LossyHistogramModule.java
 *
 * Created on June 24, 2005, 4:35 PM
 *
 */
package eunomia.plugin.gui.lossyHistogram;

import com.vivic.eunomia.sys.frontend.ConsoleContext;
import eunomia.config.ConfigChangeListener;
import org.apache.log4j.Logger;
import org.jfree.chart.title.TextTitle;
import eunomia.receptor.module.NABFlow.NABFilterEntry;
import com.vivic.eunomia.filter.Filter;
import eunomia.receptor.module.NABFlow.NABFlow;
import eunomia.messages.receptor.ModuleHandle;
import eunomia.plugin.alg.LossyFlow;
import com.vivic.eunomia.module.frontend.FrontendProcessorModule;
import com.vivic.eunomia.sys.frontend.ConsoleReceptor;
import com.vivic.eunomia.sys.frontend.GlobalSettings;
import eunomia.Descriptor;
import eunomia.plugin.msg.ModifyGraphMessage;
import eunomia.plugin.msg.RestoreGraphMessage;
import com.vivic.eunomia.sys.util.NumberFormater;
import java.awt.Font;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.PointerInfo;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.ToolTipManager;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.entity.CategoryItemEntity;
import org.jfree.chart.labels.ItemLabelAnchor;
import org.jfree.chart.labels.ItemLabelPosition;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.chart.renderer.category.StackedBarRenderer3D;
import org.jfree.ui.TextAnchor;

/**
 *
 * @author Mikhail Sosonkin
 */
public class Main implements FrontendProcessorModule, ConfigChangeListener,
                             ActionListener, PopupMenuListener, MessageSender {

    public static String CMD_SINGLE_MOVE = "CMD_SINGLE_MOVE";
    public static String CMD_SET_MSG_SENDER = "CMD_SET_MSG_SENDER";
    private StreamCategoryData scd;
    private JFreeChart hostChart;
    private ChartPanel chartComp;
    private CategoryPlot cPlot;
    private HistogramController cont;
    private String title;
    private List subTitles;
    private JMenu moveHost;
    private JMenuItem removeHost;
    private JMenuItem restoreGraph;
    private CategoryItemEntity selectedEntity;
    private LCTableEntry selectedEntry;
    private ConsoleReceptor receptor;
    private LCTableEntry[] lossyTable;
    private HashMap itemToHandle;
    private int topCount;
    private int scdType;
    private String singleMove;
    private JMenuItem singleItem;
    private MessageSender sender;
    private GlobalSettings gSet;
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
        sender = this;

        hostChart = ChartFactory.createStackedBarChart3D(null, null, null, scd, PlotOrientation.VERTICAL, true, true, false);

        subTitles = hostChart.getSubtitles();
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

        chartComp = new ChartPanel(hostChart, true);

        addToPopupMenu(chartComp.getPopupMenu());

        ToolTipManager.sharedInstance().setInitialDelay(150);
        
        receptor = ConsoleContext.getReceptor();
        
        gSet = receptor.getGlobalSettings();
        gSet.addConfigChangeListener(this);
        configurationChanged();
    }

    private void addToPopupMenu(JPopupMenu menu) {
        menu.setDefaultLightWeightPopupEnabled(true);

        moveHost = new JMenu("Move to HostView");
        removeHost = new JMenuItem("Block and Delete");
        restoreGraph = new JMenuItem("Restore Hosts");
        menu.insert(new JPopupMenu.Separator(), 0);
        menu.insert(restoreGraph, 0);
        menu.insert(removeHost, 0);
        menu.insert(moveHost, 0);

        removeHost.addActionListener(this);
        restoreGraph.addActionListener(this);
        menu.addPopupMenuListener(this);
    }

    public void actionPerformed(ActionEvent e) {
        Object o = e.getSource();

        if(selectedEntity != null) {
            if(itemToHandle.containsKey(o)) {
                blockAndDelete(scd.getType(), (ModuleHandle)itemToHandle.get(o));
            } else if(o == removeHost) {
                blockAndDelete(scd.getType(), null);
            } else if(o == singleItem) {
                viewInDetails();
            }
        }

        if(o == restoreGraph) {
            RestoreGraphMessage msg = new RestoreGraphMessage();
            try {
                sender.sendObject(msg);
            } catch(IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void viewInDetails() {
        // This is to specifically to interact within the network status module. Maybe later, we'll have a better way.
        List mods = receptor.getManager().getModuleHandles("networkStatus", Descriptor.TYPE_PROC);
        if(mods.size() > 0) {
            ModuleHandle handle = (ModuleHandle)mods.get(0);
            FrontendProcessorModule module = (FrontendProcessorModule)receptor.getManager().getEunomiaModule(handle);
            LossyFlow flow = selectedEntry.getFlow();

            long ip = 0;
            long ip1 = 0;

            switch(scd.getType()) {
                case StreamCategoryData.SINGLE_HOST:
                case StreamCategoryData.SRC_IP:
                    ip = flow.getSourceIP();
                    break;
                case StreamCategoryData.DST_IP:
                    ip = flow.getDestinationIP();
                    break;
                case StreamCategoryData.HOST_TO_HOST:
                case StreamCategoryData.PER_FLOW:
                    ip = flow.getSourceIP();
                    ip1 = flow.getDestinationIP();
                    break;
            }

            module.setProperty("AHD", Long.toString(ip));
            if(ip1 != 0) {
                module.setProperty("AHD", Long.toString(ip1));
            }
        }
    }

    private void blockAndDelete(int type, ModuleHandle handle) {
        NABFilterEntry entry = new NABFilterEntry();
        NABFilterEntry entry2 = null;
        LossyFlow flow = selectedEntry.getFlow();
        long source;
        long destination;

        switch(type) {
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

        try {
            sender.sendObject(mgm);
        } catch(IOException ex) {
            ex.printStackTrace();
        }
    }

    public void sendObject(Object o) throws IOException {
        ObjectOutput oo = new ObjectOutputStream(receptor.getManager().openInterModuleStream(this));
        oo.writeObject(o);
        oo.close();
    }

    public void popupMenuCanceled(PopupMenuEvent e) {
    }

    public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
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

        for(int i = 0; i < moveHost.getItemCount(); i++) {
            itemToHandle.remove(moveHost.getItem(i));
        }
        moveHost.removeAll();

        if(selectedEntity != null) {
            String str = selectedEntity.getCategory().toString();
            selectedEntry = scd.getTableEntry(selectedEntity.getCategoryIndex());

            removeHost.setText("Remove " + str + " From This Chart");
            moveHost.setText("Move " + str + " to HostView");

            java.util.List hvList;
            if(singleMove == null) {
                hvList = receptor.getManager().getModuleHandles("hostView", ModuleHandle.TYPE_PROC);
                if(hvList.size() > 0) {
                    moveHost.setEnabled(true);
                    Iterator it = hvList.iterator();
                    while(it.hasNext()) {
                        ModuleHandle hand = (ModuleHandle)it.next();
                        JMenuItem item = moveHost.add(hand.toString());
                        itemToHandle.put(item, hand);
                        item.addActionListener(this);
                    }
                } else {
                    moveHost.setEnabled(false);
                }
            } else {
                moveHost.setEnabled(true);
                moveHost.setText("View Related Flows For: " + str);
                if(singleItem == null) {
                    singleItem = moveHost.add("Add to View");
                    singleItem.addActionListener(this);
                } else {
                    moveHost.add(singleItem);
                }
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

    public void updateData() {
        if(chartComp.isShowing()) {
            scd.updateData();
        }
    }

    public void configurationChanged() {
        CategoryItemRenderer renderer = cPlot.getRenderer();
        for(int i = 0; i < NABFlow.NUM_TYPES; i++) {
            renderer.setSeriesPaint(i, gSet.getTypeColor(i));
        }
    }

    public JComponent getControlComponent() {
        return cont;
    }

    public void showLegend(boolean b) {
        if(b) {
            hostChart.setSubtitles(subTitles);
        } else {
            hostChart.setSubtitles(new LinkedList());
        }
    }

    public String getTitle() {
        if(title == null) {
            return "Heavy-Hitter View";
        }
        return title.replace('\n', ' ');
    }

    public void setProperty(String name, Object value) {
        if(name.equals(CMD_SINGLE_MOVE)) {
            singleMove = value.toString();
        } else if(name.equals(CMD_SET_MSG_SENDER)) {
            sender = (MessageSender)value;
        }
    }

    public Object getProperty(String name) {
        if(name.equals("web")) {
            BufferedImage img = new BufferedImage(800, 600, BufferedImage.TYPE_INT_RGB);
            NumberAxis axis = (NumberAxis)cPlot.getRangeAxis();

            axis.setRange(0, (16L * 1024L * (long)lossyTable[0].getFrequency()) * 1.1);
            hostChart.draw(img.createGraphics(), new Rectangle2D.Double(0, 0, 800, 600));
            return img;
        }

        return null;
    }

    public void setGenTitle() {
        if(scd.getType() != scdType || scd.getColumnCount() != topCount) {
            scdType = scd.getType();
            topCount = scd.getColumnCount();
            StringBuilder builder = new StringBuilder();

            builder.append("\nTop ");
            builder.append(Integer.toString(scd.getColumnCount()));
            switch(scd.getType()) {
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

    public void updateStatus(InputStream in) throws IOException {
        DataInputStream din = new DataInputStream(in);
        int tableSize = din.readInt();

        if(tableSize == 0) {
            return;
        }

        if(lossyTable == null || lossyTable.length != tableSize) {
            lossyTable = new LCTableEntry[tableSize];
        }

        for(int i = 0; i < tableSize; i++) {
            LCTableEntry entry = lossyTable[i];
            boolean isNNull = din.readBoolean();
            if(isNNull) {
                if(entry == null) {
                    entry = new LCTableEntry();
                    lossyTable[i] = entry;
                }

                entry.getFlow().assign(din.readLong(), din.readLong(), din.readInt(), din.readInt());

                entry.setFlowId(din.readInt());
                entry.setIdleTime(din.readInt());
                entry.setFrequency(din.readInt());
                entry.setStartTime(din.readLong());

                for(int k = 0; k < NABFlow.NUM_TYPES; ++k) {
                    int f = din.readInt();
                    entry.setFrequencyType(k, f);
                }
            }
        }

        scd.updateLossyTable(lossyTable);
        updateData();
        setGenTitle();
    }

    public void getControlData(OutputStream out) throws IOException {
        cont.getControlData(out);
    }

    public void setControlData(InputStream in) throws IOException {
        cont.setControlData(in);
    }

    public void processMessage(DataInputStream din) throws IOException {
    }
}