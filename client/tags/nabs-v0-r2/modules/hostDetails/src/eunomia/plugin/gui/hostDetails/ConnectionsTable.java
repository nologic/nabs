/*
 * -ConversationGUI.java
 *  ConnectionsTable.java
 *
 * Created on August 23, 2005, 3:17 PM
 *
 */

package eunomia.plugin.gui.hostDetails;

import eunomia.plugin.oth.hostDetails.conv.Conversation;
import eunomia.util.*;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;

/**
 *
 * @author Mikhail Sosonkin
 */

public class ConnectionsTable extends JPanel implements TableModel, MouseListener {
    private static final String[] colNames = new String[]{"Last Seen", "Local Port",
    "Remote Port", "Remote IP", "Remote Host Name", "Distribution", "Total", "Ingress", "Egress"};
    private static Comparator[] colComparators;
    private static Comparator[] reverseComparators;
    
    private JTable table;
    private TableModelEvent event;
    private Conversation[] convs;
    private Calendar cal;
    private Comparator comparator;
    private DistributionCell dcell;
    
    public ConnectionsTable(){
        dcell = new DistributionCell();
        event = new TableModelEvent(this);
        cal = Calendar.getInstance();
        
        addControls();
        
        table.getColumnModel().getColumn(5).setCellRenderer(dcell);
    }
    
    public void addTableModelListener(TableModelListener l) {
    }
    
    public void updateConversations(Conversation[] c){
        if(comparator != null){
            Arrays.sort(c, comparator); // this uses mergesort.
        }
        convs = c;
        table.tableChanged(event);
    }
    
    public Class getColumnClass(int columnIndex) {
        return String.class;
    }
    
    public int getColumnCount() {
        return colNames.length;
    }
    
    public String getColumnName(int columnIndex) {
        return colNames[columnIndex];
    }
    
    public int getRowCount() {
        if(convs == null){
            return 0;
        }
        
        return convs.length;
    }
    
    public Object getValueAt(int r, int columnIndex) {
        Conversation[] c = convs;
        if(r < c.length){
            switch(columnIndex){
                case 0:
                    int h, m, s;
                    h = cal.get(Calendar.HOUR_OF_DAY);
                    m = cal.get(Calendar.MINUTE);
                    s = cal.get(Calendar.SECOND);
                    cal.setTimeInMillis(c[r].getLastActive());
                    return  (h < 10?"0":"") + h + ":" + (m < 10?"0":"") + m + ":" + (s < 10?"0":"") + s;
                case 1: return c[r].getLclPort();
                case 2: return c[r].getRmtPort();
                case 3: return c[r].getRmtAddress().getHostAddress();
                case 4: return c[r].getRmtHost();
                case 5: return c[r].getPercentTypes();
                case 6: return Util.convertBytes(c[r].getTotalBytes());
                case 7: return Util.convertBytes(c[r].getInBytes());
                case 8: return Util.convertBytes(c[r].getOutBytes());
            }
        }
        
        return "";
    }
    
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }
    
    public void removeTableModelListener(TableModelListener l) {
    }
    
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
    }
    
    public void mouseClicked(MouseEvent e){
        JTableHeader h = table.getTableHeader();
        TableColumnModel columnModel = h.getColumnModel();
        int viewColumn = columnModel.getColumnIndexAtX(e.getX());
        int column = columnModel.getColumn(viewColumn).getModelIndex();
        if(column == 5){
            column--; //no sort for distribution
        }
        
        if (column != -1 && colComparators[column] != null) {
            if(comparator == colComparators[column]){
                comparator = reverseComparators[column];
            } else {
                comparator = colComparators[column];
            }
        }
    }
    
    public void mousePressed(MouseEvent e){
    }
    
    public void mouseReleased(MouseEvent e){
    }
    
    public void mouseEntered(MouseEvent e){
    }
    
    public void mouseExited(MouseEvent e){
    }
    
    private void addControls(){
        setLayout(new BorderLayout());
        
        add(new JScrollPane(table = new JTable(this)));
        
        table.getTableHeader().addMouseListener(this);
        
        TableColumnModel model = table.getColumnModel();
        model.getColumn(0).setPreferredWidth(20);
        model.getColumn(1).setPreferredWidth(20);
        model.getColumn(2).setPreferredWidth(20);
        model.getColumn(3).setPreferredWidth(40);
        model.getColumn(6).setPreferredWidth(15);
        model.getColumn(7).setPreferredWidth(15);
        model.getColumn(8).setPreferredWidth(15);
    }
    
    static {
        // made this in the arrays, so that there are less decisions
        // while sorting.
        
        // initialize colComparators
        reverseComparators = new Comparator[colNames.length];
        colComparators = new Comparator[colNames.length];
        
        colComparators[0] = new Comparator(){
            //Last Seen
            public int compare(Object o1, Object o2){
                Conversation c1 = (Conversation)o1;
                Conversation c2 = (Conversation)o2;
                
                if(c1.getLastActive() < c2.getLastActive()){
                    return -1;
                } else if(c1.getLastActive() > c2.getLastActive()){
                    return 1;
                }
                
                return 0;
            }
            
            public boolean equals(Object obj){
                return this == obj;
            }
        };
        
        colComparators[1] = new Comparator(){
            //Local port
            public int compare(Object o1, Object o2){
                Conversation c1 = (Conversation)o1;
                Conversation c2 = (Conversation)o2;
                
                if(c1.getLcl_port() < c2.getLcl_port()){
                    return -1;
                } else if(c1.getLcl_port() > c2.getLcl_port()){
                    return 1;
                }
                
                return 0;
            }
            
            public boolean equals(Object obj){
                return this == obj;
            }
        };
        
        colComparators[2] = new Comparator(){
            //remote port
            public int compare(Object o1, Object o2){
                Conversation c1 = (Conversation)o1;
                Conversation c2 = (Conversation)o2;
                
                if(c1.getRmt_port() < c2.getRmt_port()){
                    return -1;
                } else if(c1.getRmt_port() > c2.getRmt_port()){
                    return 1;
                }
                
                return 0;
            }
            
            public boolean equals(Object obj){
                return this == obj;
            }
        };
        
        colComparators[3] = new Comparator(){
            //remote ip
            public int compare(Object o1, Object o2){
                Conversation c1 = (Conversation)o1;
                Conversation c2 = (Conversation)o2;
                
                if(c1.getRmt_ip() < c2.getRmt_ip()){
                    return -1;
                } else if(c1.getRmt_ip() > c2.getRmt_ip()){
                    return 1;
                }
                
                return 0;
            }
            
            public boolean equals(Object obj){
                return this == obj;
            }
        };
        
        colComparators[4] = new Comparator(){
            //remote host
            public int compare(Object o1, Object o2){
                Conversation c1 = (Conversation)o1;
                Conversation c2 = (Conversation)o2;
                
                return c1.getRmtHost().compareTo(c2.getRmtHost());
            }
            
            public boolean equals(Object obj){
                return this == obj;
            }
        };
        
        colComparators[5] = new Comparator(){
            //total
            public int compare(Object o1, Object o2){
                Conversation c1 = (Conversation)o1;
                Conversation c2 = (Conversation)o2;
                
                if(c1.getTotalBytes() < c2.getTotalBytes()){
                    return -1;
                } else if(c1.getTotalBytes() > c2.getTotalBytes()){
                    return 1;
                }
                
                return 0;
            }
            
            public boolean equals(Object obj){
                return this == obj;
            }
        };
        
        colComparators[6] = new Comparator(){
            //in
            public int compare(Object o1, Object o2){
                Conversation c1 = (Conversation)o1;
                Conversation c2 = (Conversation)o2;
                
                if(c1.getInBytes() < c2.getInBytes()){
                    return -1;
                } else if(c1.getInBytes() > c2.getInBytes()){
                    return 1;
                }
                
                return 0;
            }
            
            public boolean equals(Object obj){
                return this == obj;
            }
        };
        
        colComparators[7] = new Comparator(){
            //out
            public int compare(Object o1, Object o2){
                Conversation c1 = (Conversation)o1;
                Conversation c2 = (Conversation)o2;
                
                if(c1.getOutBytes() < c2.getOutBytes()){
                    return -1;
                } else if(c1.getOutBytes() > c2.getOutBytes()){
                    return 1;
                }
                
                return 0;
            }
            
            public boolean equals(Object obj){
                return this == obj;
            }
        };
        
        reverseComparators[0] = new Comparator(){
            //Last Seen
            public int compare(Object o1, Object o2){
                Conversation c1 = (Conversation)o1;
                Conversation c2 = (Conversation)o2;
                
                if(c1.getLastActive() > c2.getLastActive()){
                    return -1;
                } else if(c1.getLastActive() < c2.getLastActive()){
                    return 1;
                }
                
                return 0;
            }
            
            public boolean equals(Object obj){
                return this == obj;
            }
        };
        
        reverseComparators[1] = new Comparator(){
            //Local port
            public int compare(Object o1, Object o2){
                Conversation c1 = (Conversation)o1;
                Conversation c2 = (Conversation)o2;
                
                if(c1.getLcl_port() > c2.getLcl_port()){
                    return -1;
                } else if(c1.getLcl_port() < c2.getLcl_port()){
                    return 1;
                }
                
                return 0;
            }
            
            public boolean equals(Object obj){
                return this == obj;
            }
        };
        
        reverseComparators[2] = new Comparator(){
            //remote port
            public int compare(Object o1, Object o2){
                Conversation c1 = (Conversation)o1;
                Conversation c2 = (Conversation)o2;
                
                if(c1.getRmt_port() > c2.getRmt_port()){
                    return -1;
                } else if(c1.getRmt_port() < c2.getRmt_port()){
                    return 1;
                }
                
                return 0;
            }
            
            public boolean equals(Object obj){
                return this == obj;
            }
        };
        
        reverseComparators[3] = new Comparator(){
            //remote ip
            public int compare(Object o1, Object o2){
                Conversation c1 = (Conversation)o1;
                Conversation c2 = (Conversation)o2;
                
                if(c1.getRmt_ip() > c2.getRmt_ip()){
                    return -1;
                } else if(c1.getRmt_ip() < c2.getRmt_ip()){
                    return 1;
                }
                
                return 0;
            }
            
            public boolean equals(Object obj){
                return this == obj;
            }
        };
        
        reverseComparators[4] = new Comparator(){
            //remote host
            public int compare(Object o1, Object o2){
                Conversation c1 = (Conversation)o1;
                Conversation c2 = (Conversation)o2;
                
                return c2.getRmtHost().compareTo(c1.getRmtHost());
            }
            
            public boolean equals(Object obj){
                return this == obj;
            }
        };
        
        reverseComparators[5] = new Comparator(){
            //total
            public int compare(Object o1, Object o2){
                Conversation c1 = (Conversation)o1;
                Conversation c2 = (Conversation)o2;
                
                if(c1.getTotalBytes() > c2.getTotalBytes()){
                    return -1;
                } else if(c1.getTotalBytes() < c2.getTotalBytes()){
                    return 1;
                }
                
                return 0;
            }
            
            public boolean equals(Object obj){
                return this == obj;
            }
        };
        
        reverseComparators[6] = new Comparator(){
            //in
            public int compare(Object o1, Object o2){
                Conversation c1 = (Conversation)o1;
                Conversation c2 = (Conversation)o2;
                
                if(c1.getInBytes() > c2.getInBytes()){
                    return -1;
                } else if(c1.getInBytes() < c2.getInBytes()){
                    return 1;
                }
                
                return 0;
            }
            
            public boolean equals(Object obj){
                return this == obj;
            }
        };
        
        reverseComparators[7] = new Comparator(){
            //out
            public int compare(Object o1, Object o2){
                Conversation c1 = (Conversation)o1;
                Conversation c2 = (Conversation)o2;
                
                if(c1.getOutBytes() > c2.getOutBytes()){
                    return -1;
                } else if(c1.getOutBytes() < c2.getOutBytes()){
                    return 1;
                }
                
                return 0;
            }
            
            public boolean equals(Object obj){
                return this == obj;
            }
        };
        
    }
}