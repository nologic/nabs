/*
 * DetailedView.java
 *
 * Created on August 21, 2005, 4:38 PM
 *
 */

package eunomia.plugin.gui.hostDetails;

import com.vivic.eunomia.sys.frontend.GlobalSettings;
import eunomia.plugin.oth.hostDetails.conv.Conversation;
import eunomia.receptor.module.NABFlow.NABFlow;
import eunomia.util.HostResolver;
import eunomia.util.ResolveRequest;
import eunomia.util.SpringUtilities;
import com.vivic.eunomia.sys.util.Util;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.net.InetAddress;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SpringLayout;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;


/**
 *
 * @author Mikhail Sosonkin
 */

public class DetailedView extends JPanel implements TableModel, ResolveRequest {
    private static Font labelFont;
    private static String[] colNames;
    
    private InetAddress lclHost;
    private String hostName;
    private long[][] tableData;
    private StringBuilder sb;
    
    private TableModelEvent modelEvent;
    private JTable table;
    private ConnectionsTable cGui;
    private HostDetail hDetail;
    
    // Top panel details:
    private JLabel nameHost;
    private JLabel dateSince;
    private JLabel activeFlows;
    
    private JLabel totalBytes;
    private JLabel inBytes;
    private JLabel outBytes;
    private JLabel inRate;
    private JLabel outRate;
    
    static {
        colNames = new String[]{"Content Type", "Ingress", "Egress", "TOTAL"};
        labelFont = new Font("SansSerif", Font.PLAIN, 10);
    }
    
    public DetailedView(InetAddress host, HostDetail detail, long[][] dTable) {
        hDetail = detail;
        lclHost = host;
        tableData = dTable;
        
        HostResolver.addRequest(this);
        
        sb = new StringBuilder();
        modelEvent = new TableModelEvent(this);
        
        addControls();
        setComponentPopupMenu(null);
    }
    
    public void setGlobalSettings(GlobalSettings global) {
        cGui.setGlobalSettings(global);
    }
    
    public void setConversationList(Conversation[] conv){
        cGui.updateConversations(conv);
    }
    
    public HostDetail getHostDetail(){
        return hDetail;
    }
    
    public InetAddress getHost(){
        return lclHost;
    }
    
    public void refresh(){
        if(isVisible()){
            if(hostName != null){
                nameHost.setText(hostName + " (" + lclHost.getHostAddress() + ")");
            } else {
                nameHost.setText("(Resolving)" + " (" + lclHost.getHostAddress() + ")");
            }

            dateSince.setText(Util.getTimeStamp(hDetail.getStartTime(), true, true));
            activeFlows.setText(Integer.toString(hDetail.getConversationCount()));
            
            totalBytes.setText(Util.convertBytes(hDetail.getTotalBytes(), true));
            inBytes.setText(Util.convertBytes(hDetail.getInBytes(), true));
            outBytes.setText(Util.convertBytes(hDetail.getOutBytes(), true));
            inRate.setText(Util.convertBytesRate(hDetail.getInRate(), true));
            outRate.setText(Util.convertBytesRate(hDetail.getOutRate(), true));
            
            table.tableChanged(modelEvent);
        }
    }
    
    public int getRowCount(){
        return tableData[0].length;
    }
    
    public int getColumnCount(){
        return tableData.length + 1;
    }
    
    public String getColumnName(int columnIndex){
        return colNames[columnIndex];
    }
    
    public Class getColumnClass(int columnIndex){
        return String.class;
    }
    
    public boolean isCellEditable(int rowIndex, int columnIndex){
        return false;
    }
    
    public Object getValueAt(int rowIndex, int columnIndex){
        if(columnIndex == 0){
            if(rowIndex == tableData[0].length - 1){
                return "Total";
            }
            return NABFlow.typeNames[rowIndex];
        }
        
        if(sb.length() > 0){
            sb.delete(0, sb.length());
        }
        
        return Util.convertBytes(sb, tableData[columnIndex - 1][rowIndex], true).toString();
    }
    
    public void setValueAt(Object aValue, int rowIndex, int columnIndex){
    }
    
    public void addTableModelListener(TableModelListener l){
    }
    
    public void removeTableModelListener(TableModelListener l){
    }
    
    private void addControls(){
        JPanel mainPanel = new JPanel(new BorderLayout());
        JPanel topPanel = new JPanel(new BorderLayout());
        JPanel identPanel = new JPanel(new BorderLayout());
        JPanel identPanelBottom = new JPanel(new GridLayout(1, 3));
        JPanel identPanelTop = new JPanel(new SpringLayout());
        JPanel identOnePanel = new JPanel(new SpringLayout());
        JPanel identTwoPanel = new JPanel(new SpringLayout());
        JPanel identThreePanel = new JPanel(new SpringLayout());
        
        setLayout(new BorderLayout());
        
        JScrollPane pane;
        identPanelTop.add(makeLabel("Host Name:"));
        identPanelTop.add(nameHost = makeLabel(""));
        
        identTwoPanel.add(makeLabel("Date Since:"));
        identTwoPanel.add(dateSince = makeLabel(""));
        identTwoPanel.add(makeLabel("Active Flows:"));
        identTwoPanel.add(activeFlows = makeLabel(""));
        
        identOnePanel.add(makeLabel("Total:"));
        identOnePanel.add(totalBytes = makeLabel(""));
        identOnePanel.add(makeLabel("IN Bytes:"));
        identOnePanel.add(inBytes = makeLabel(""));
        identOnePanel.add(makeLabel("OUT Bytes:"));
        identOnePanel.add(outBytes = makeLabel(""));
        
        identThreePanel.add(makeLabel("IN Rate:"));
        identThreePanel.add(inRate = makeLabel(""));
        identThreePanel.add(makeLabel("OUT Rate:"));
        identThreePanel.add(outRate = makeLabel(""));
        
        identPanelBottom.add(identTwoPanel);
        identPanelBottom.add(identOnePanel);
        identPanelBottom.add(identThreePanel);
        
        identPanel.add(identPanelTop, BorderLayout.NORTH);
        identPanel.add(identPanelBottom);
        
        topPanel.add(identPanel, BorderLayout.NORTH);
        topPanel.add(pane = new JScrollPane(table = new JTable(this)));
        
        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(cGui = new ConnectionsTable());
        
        JScrollPane scrollPane;
        add(scrollPane = new JScrollPane(mainPanel));
        
        cGui.setPreferredSize(new Dimension(0, 200));
        pane.setPreferredSize(new Dimension(0, 170));
        
        SpringUtilities.makeCompactGrid(identPanelTop, identPanelTop.getComponentCount()/2, 2, 20, 6, 0, 2);
        SpringUtilities.makeCompactGrid(identOnePanel, identOnePanel.getComponentCount()/2, 2, 20, 6, 0, 2);
        SpringUtilities.makeCompactGrid(identTwoPanel, identTwoPanel.getComponentCount()/2, 2, 20, 6, 0, 2);
        SpringUtilities.makeCompactGrid(identThreePanel, identThreePanel.getComponentCount()/2, 2, 20, 6, 0, 2);
        
        table.setFont(labelFont);
    }
    
    public InetAddress getAddress() {
        return lclHost;
    }
    
    public void setResolved(String hName) {
        hostName = hName;
    }
    
    private JLabel makeLabel(String str){
        JLabel label = new JLabel(str);
        
        label.setFont(labelFont);
        
        return label;
    }
}