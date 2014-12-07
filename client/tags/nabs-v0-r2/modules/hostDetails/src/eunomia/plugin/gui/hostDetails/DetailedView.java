/*
 * DetailedView.java
 *
 * Created on August 21, 2005, 4:38 PM
 *
 */

package eunomia.plugin.gui.hostDetails;

import eunomia.plugin.oth.hostDetails.conv.Conversation;
import eunomia.receptor.module.NABFlow.NABFlow;
import eunomia.util.*;

import java.awt.*;
import java.net.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;


/**
 *
 * @author Mikhail Sosonkin
 */

public class DetailedView extends JPanel implements TableModel, ResolveRequest {
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
    private JLabel hostAddress;
    private JLabel dateSince;
    private JLabel activeFlows;
    
    private JLabel totalBytes;
    private JLabel inBytes;
    private JLabel outBytes;
    private JLabel inRate;
    private JLabel outRate;
    
    static {
        colNames = new String[]{"Content Type", "Ingress", "Egress", "TOTAL"};
    }
    
    public DetailedView(InetAddress host, HostDetail detail, long[][] dTable) {
        hDetail = detail;
        lclHost = host;
        tableData = dTable;
        
        HostResolver.addRequest(this);
        
        sb = new StringBuilder();
        modelEvent = new TableModelEvent(this);
        
        addControls();
        this.setComponentPopupMenu(null);
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
                nameHost.setText(hostName);
            } else {
                nameHost.setText("(Resolving)");
            }
            hostAddress.setText(lclHost.getHostAddress());
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
        JPanel topPanel = new JPanel(new BorderLayout());
        JPanel identPanel = new JPanel(new GridLayout(1, 2));
        JPanel identRightPanel = new JPanel(new SpringLayout());
        JPanel identLeftPanel = new JPanel(new SpringLayout());
        
        setLayout(new BorderLayout());
        
        JScrollPane pane;
        identLeftPanel.add(new JLabel("Host Name:"));
        identLeftPanel.add(nameHost = new JLabel());
        identLeftPanel.add(new JLabel("Host Addr:"));
        identLeftPanel.add(hostAddress = new JLabel());
        identLeftPanel.add(new JLabel("Date Since:"));
        identLeftPanel.add(dateSince = new JLabel());
        identLeftPanel.add(new JLabel("Active Flows:"));
        identLeftPanel.add(activeFlows = new JLabel());
        
        identRightPanel.add(new JLabel("Total:"));
        identRightPanel.add(totalBytes = new JLabel());
        identRightPanel.add(new JLabel("IN Bytes:"));
        identRightPanel.add(inBytes = new JLabel());
        identRightPanel.add(new JLabel("OUT Bytes:"));
        identRightPanel.add(outBytes = new JLabel());
        identRightPanel.add(new JLabel("IN Rate:"));
        identRightPanel.add(inRate = new JLabel());
        identRightPanel.add(new JLabel("OUT Rate:"));
        identRightPanel.add(outRate = new JLabel());
        
        identPanel.add(identLeftPanel);
        identPanel.add(identRightPanel);
        topPanel.add(identPanel, BorderLayout.NORTH);
        topPanel.add(pane = new JScrollPane(table = new JTable(this)));
        add(topPanel, BorderLayout.NORTH);
        add(cGui = new ConnectionsTable());
        
        pane.setPreferredSize(new Dimension(0, 170));
        SpringUtilities.makeCompactGrid(identRightPanel, identRightPanel.getComponentCount()/2, 2, 20, 6, 3, 3);
        SpringUtilities.makeCompactGrid(identLeftPanel, identLeftPanel.getComponentCount()/2, 2, 20, 6, 3, 3);
    }
    
    public InetAddress getAddress() {
        return lclHost;
    }
    
    public void setResolved(String hName) {
        hostName = hName;
    }
}