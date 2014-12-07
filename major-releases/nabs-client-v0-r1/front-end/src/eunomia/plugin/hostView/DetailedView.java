/*
 * DetailedView.java
 *
 * Created on August 21, 2005, 4:38 PM
 *
 */

package eunomia.plugin.hostView;

import eunomia.core.data.flow.*;
import eunomia.util.*;
        
import java.awt.*;
import java.net.*;
import java.util.*;
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
    private HostData data;
    private long[][] tableData;
    private StringBuilder sb;
    
    private TableModelEvent modelEvent;
    private JTable table;
    private JLabel identLabel;
    private ConnectionsTable cGui;
    
    static {
        colNames = new String[]{"Content Type", "Ingress", "Egress", "TOTAL"};
    }
    
    public DetailedView(InetAddress host, HostData hData) {
        data = hData;
        lclHost = host;
        
        HostResolver.addRequest(this);
        
        tableData = data.getDataTable();
        sb = new StringBuilder();
        modelEvent = new TableModelEvent(this);
        
        addControls();
        this.setComponentPopupMenu(null);
    }
    
    public InetAddress getHost(){
        return lclHost;
    }
    
    public HostData getHostData(){
        return data;
    }
    
    public void refresh(){
        if(isVisible()){
            if(sb.length() > 0){
                sb.delete(0, sb.length());
            }

            sb.append("<html><body>");
            sb.append("Host Name: ");
            if(hostName != null){
                sb.append(hostName);
            } else {
                sb.append("(Resolving)");
            }
            sb.append("<br>Host Addr: ");
            sb.append(lclHost.getHostAddress());
            sb.append("<br>Data Since: ");
            sb.append(new Date(data.getStartTime()).toString());
            sb.append("<br>Active Flows: ");
            sb.append(Integer.toString(data.conversationCount()));
            sb.append("</body></html>");
            identLabel.setText(sb.toString());
        
            table.tableChanged(modelEvent);
            cGui.updateConversations(data.getConversationArray());
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
            return Flow.typeNames[rowIndex];
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
        
        setLayout(new BorderLayout());
        
        JScrollPane pane;
        topPanel.add(identLabel = new JLabel(), BorderLayout.NORTH);
        topPanel.add(pane = new JScrollPane(table = new JTable(this)));
        add(topPanel, BorderLayout.NORTH);
        add(cGui = new ConnectionsTable());
        
        pane.setPreferredSize(new Dimension(0, 170));
    }

    public InetAddress getAddress() {
        return lclHost;
    }

    public void setResolved(String hName) {
        hostName = hName;
    }
}