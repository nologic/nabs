/*
 * AlertCellRenderer.java
 *
 * Created on July 11, 2007, 6:58 PM
 *
 */

package eunomia.plugin.gui.networkPolicy.table;

import eunomia.plugin.com.networkPolicy.AlertItem;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Comparator;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;

/**
 *
 * @author Mikhail Sosonkin
 */
public class AlertCellRendererSorter extends DefaultTableCellRenderer implements MouseListener {
    private static Color[] stateColor;
    
    private static Comparator[] colComparators;
    private static Comparator[] reverseComparators;
    
    private Comparator comparator;
    private AlertsTableModel model;
    private JTable table;
    private Font tableFont;
    private Font selTableFont;
    
    public AlertCellRendererSorter(JTable table, AlertsTableModel model) {
        this.model = model;
        this.table = table;
        
        setBorder(BorderFactory.createEmptyBorder());
        setHorizontalAlignment(JLabel.CENTER);
    }
    
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        AlertItem alert = model.getAlert(row);
        
        if(tableFont == null) {
            tableFont = table.getFont();
            selTableFont = new Font(tableFont.getName(), Font.BOLD, tableFont.getSize());
        }
        
        if (isSelected) {
            setBackground(table.getSelectionBackground());
        } else {
            setBackground(stateColor[alert.getStatus()]);
        }

        if(model.isSelectedIP(alert.getIpString())) {
            setFont(selTableFont);
        } else {
            setFont(tableFont);
        }
        
        setValue(value);
        
        return this;
    }
    
    public void mouseClicked(MouseEvent e) {
        JTableHeader h = table.getTableHeader();
        TableColumnModel columnModel = h.getColumnModel();
        int viewColumn = columnModel.getColumnIndexAtX(e.getX());
        int column = columnModel.getColumn(viewColumn).getModelIndex();
        
        if (column != -1 && colComparators[column] != null) {
            if(comparator == colComparators[column]){
                comparator = reverseComparators[column];
            } else {
                comparator = colComparators[column];
            }
        }
        
        model.sort(comparator);
    }
    
    public void mousePressed(MouseEvent e) {
    }
    
    public void mouseReleased(MouseEvent e) {
    }
    
    public void mouseEntered(MouseEvent e) {
    }
    
    public void mouseExited(MouseEvent e) {
    }
    
    static {
        stateColor = new Color[] {
            new Color(0x3FFF0000, true), //NEW
            new Color(0x3F0000FF, true), //OPEN
            new Color(0x3F00FFFF, true), //PENDING
            new Color(0x3F00FF00, true), //CLOSED
            new Color(0x3FFFFFFF, true)  //UNKNOWN
        };
        
        
        //private static String[] columns = new String [] {"Policy Violated", "Violations", "Violator", "Time", "Status", "Description"};
        reverseComparators = new Comparator[AlertsTableModel.columns.length];
        colComparators = new Comparator[AlertsTableModel.columns.length];
        
        colComparators[0] = new Comparator(){
            //Policy
            public int compare(Object o1, Object o2){
                AlertItem c1 = (AlertItem)o1;
                AlertItem c2 = (AlertItem)o2;
                
                if(c1.getPolicyID() < c2.getPolicyID()) {
                    return -1;
                } else if(c1.getPolicyID() > c2.getPolicyID()){
                    return 1;
                }
                
                return 0;
            }
        };
        
        colComparators[1] = new Comparator(){
            //Violations
            public int compare(Object o1, Object o2){
                AlertItem c1 = (AlertItem)o1;
                AlertItem c2 = (AlertItem)o2;
                
                if(c1.getViolations() < c2.getViolations()) {
                    return -1;
                } else if(c1.getViolations() > c2.getViolations()){
                    return 1;
                }
                
                return 0;
            }
        };
        colComparators[2] = new Comparator(){
            //Violator
            public int compare(Object o1, Object o2){
                AlertItem c1 = (AlertItem)o1;
                AlertItem c2 = (AlertItem)o2;
                
                if(c1.getFlowId().getSourceIP() < c2.getFlowId().getSourceIP()) {
                    return -1;
                } else if(c1.getFlowId().getSourceIP() > c2.getFlowId().getSourceIP()){
                    return 1;
                }
                
                return 0;
            }
        };
        colComparators[3] = new Comparator(){
            //Host Name
            public int compare(Object o1, Object o2){
                AlertItem c1 = (AlertItem)o1;
                AlertItem c2 = (AlertItem)o2;
                
                String h1 = c1.getHostName();
                String h2 = c2.getHostName();
                
                if(h1 == null || h2 == null) {
                    return 0;
                }
                
                return h1.compareTo(h2);
            }
        };
        colComparators[4] = new Comparator(){
            //Time
            public int compare(Object o1, Object o2){
                AlertItem c1 = (AlertItem)o1;
                AlertItem c2 = (AlertItem)o2;
                
                if(c1.getFirstSeen() < c2.getFirstSeen()) {
                    return -1;
                } else if(c1.getFirstSeen() > c2.getFirstSeen()){
                    return 1;
                }
                
                return 0;
            }
        };
        colComparators[5] = new Comparator(){
            //Status
            public int compare(Object o1, Object o2){
                AlertItem c1 = (AlertItem)o1;
                AlertItem c2 = (AlertItem)o2;
                
                if(c1.getStatus() < c2.getStatus()) {
                    return -1;
                } else if(c1.getStatus() > c2.getStatus()){
                    return 1;
                }
                
                return 0;
            }
        };
        
        reverseComparators[0] = new Comparator(){
            //Policy
            public int compare(Object o1, Object o2){
                AlertItem c1 = (AlertItem)o1;
                AlertItem c2 = (AlertItem)o2;
                
                if(c1.getPolicyID() > c2.getPolicyID()) {
                    return -1;
                } else if(c1.getPolicyID() < c2.getPolicyID()){
                    return 1;
                }
                
                return 0;
            }
        };
        
        reverseComparators[1] = new Comparator(){
            //Violations
            public int compare(Object o1, Object o2){
                AlertItem c1 = (AlertItem)o1;
                AlertItem c2 = (AlertItem)o2;
                
                if(c1.getViolations() > c2.getViolations()) {
                    return -1;
                } else if(c1.getViolations() < c2.getViolations()){
                    return 1;
                }
                
                return 0;
            }
        };
        reverseComparators[2] = new Comparator(){
            //Violator
            public int compare(Object o1, Object o2){
                AlertItem c1 = (AlertItem)o1;
                AlertItem c2 = (AlertItem)o2;
                
                if(c1.getFlowId().getSourceIP() > c2.getFlowId().getSourceIP()) {
                    return -1;
                } else if(c1.getFlowId().getSourceIP() < c2.getFlowId().getSourceIP()){
                    return 1;
                }
                
                return 0;
            }
        };
        reverseComparators[3] = new Comparator(){
            //Host Name
            public int compare(Object o1, Object o2){
                AlertItem c1 = (AlertItem)o1;
                AlertItem c2 = (AlertItem)o2;
                
                String h1 = c1.getHostName();
                String h2 = c2.getHostName();
                
                if(h1 == null || h2 == null) {
                    return 0;
                }
                
                return -(h1.compareTo(h2));
            }
        };
        reverseComparators[4] = new Comparator(){
            //Time
            public int compare(Object o1, Object o2){
                AlertItem c1 = (AlertItem)o1;
                AlertItem c2 = (AlertItem)o2;
                
                if(c1.getFirstSeen() > c2.getFirstSeen()) {
                    return -1;
                } else if(c1.getFirstSeen() < c2.getFirstSeen()){
                    return 1;
                }
                
                return 0;
            }
        };
        reverseComparators[5] = new Comparator(){
            //Status
            public int compare(Object o1, Object o2){
                AlertItem c1 = (AlertItem)o1;
                AlertItem c2 = (AlertItem)o2;
                
                if(c1.getStatus() > c2.getStatus()) {
                    return -1;
                } else if(c1.getStatus() < c2.getStatus()){
                    return 1;
                }
                
                return 0;
            }
        };
    }
}
