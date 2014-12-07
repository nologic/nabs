/*
 * DiskResultSetTable.java
 *
 * Created on August 18, 2005, 6:34 PM
 */

package eunomia.gui.archival;

import eunomia.core.data.staticData.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;

/**
 *
 * @author Mikhail Sosonkin
 */
public class DiskResultSetTable extends JPanel implements TableModel, AdjustmentListener {
    private DiskResultSet set;
    private JTable table;
    private JScrollBar scrollBar;
    private int max;
    private int window;
    private int offset;
    private TableModel model;
    private TableModelEvent cEvent;
    
    public DiskResultSetTable(){
        addControls();
        
        model = new DefaultTableModel(0, 0);
        cEvent = new TableModelEvent(this);
        window = 30;
    }
    
    public void setResultSet(DiskResultSet rset) throws Exception {
        set = rset;
        
        max = rset.getRowCount();
        
        table.setModel(model);
        table.setModel(this);

        offset = 0;
        scrollBar.setValue(0);
        scrollBar.setMaximum( (max < window? 0 : max - window));
    }
    
    public void adjustmentValueChanged(AdjustmentEvent e){
        offset = scrollBar.getValue();
        table.tableChanged(cEvent);
    }
    
    private void addControls(){
        setLayout(new BorderLayout());
        
        scrollBar = new JScrollBar(JScrollBar.VERTICAL);
        
        table = new JTable(this);
        add(new JScrollPane(table, JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED));
        add(scrollBar, BorderLayout.EAST);
        
        scrollBar.addAdjustmentListener(this);
        scrollBar.setMinimum(0);
    }
    
    public void addTableModelListener(TableModelListener l){
    }
    
    public Class getColumnClass(int columnIndex){
        try {
            return set.getTypeClass(columnIndex);
        } catch (Exception e){
            e.printStackTrace();
        }
        return String.class;
    }
    
    public int getColumnCount(){
        if(set != null){
            return set.getColumnCount();
        }
        
        return 0;
    }
    
    public String getColumnName(int columnIndex){
        if(set != null){
            return set.getColumnName(columnIndex);
        }
        
        return null;
    }
    
    public int getRowCount(){
        return (max < window ? max:window);
    }
    
    public Object getValueAt(int rowIndex, int columnIndex){
        if(set != null){
            try {
                set.setRow(offset + rowIndex);
                Object o = set.getObject(columnIndex);
                if(o == null){
                    return "";
                }
                return o.toString();
            } catch(Exception e){
                e.printStackTrace();
            }
        }
        return null;
    }
    
    public boolean isCellEditable(int rowIndex, int columnIndex){
        return false;
    }
    
    public void removeTableModelListener(TableModelListener l){
    }
    
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
    }
}