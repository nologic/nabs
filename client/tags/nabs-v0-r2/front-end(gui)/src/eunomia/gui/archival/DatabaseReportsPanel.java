/*
 * DatabaseReportsPanel.java
 *
 * Created on December 3, 2006, 5:10 PM
 *
 */

package eunomia.gui.archival;

import eunomia.core.data.staticData.DatabaseReportListener;
import eunomia.core.receptor.Receptor;
import eunomia.gui.MainGui;
import eunomia.messages.DatabaseDescriptor;
import eunomia.messages.receptor.ncm.AnalysisSummaryMessage;
import eunomia.plugin.interfaces.GUIStaticAnalysisModule;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.EventObject;
import java.util.Iterator;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.CellEditorListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableModel;

/**
 *
 * @author Mikhail Sosonkin
 */
public class DatabaseReportsPanel extends JPanel implements ActionListener, DatabaseReportListener, TableModel, TableCellEditor {
    private static final String[] columns = new String[]{"Name", "ID", "Progress", "Report"};
    private static final Class[] columnClass = new Class[]{String.class, Integer.class, Double.class, String.class};
    
    private DatabaseDescriptor db;
    private Receptor receptor;
    private JButton report;
    private AnalysisSummaryMessage.SUM[] sums;
    private TableModelEvent cEvent;
    private JTable table;
    private JLabel editLabel;
    
    public DatabaseReportsPanel(Receptor rec) {
        receptor = rec;
        sums = new AnalysisSummaryMessage.SUM[0];
        cEvent = new TableModelEvent(this);
        receptor.getManager().addDatabaseReportListener(this);
        
        addControls();
    }

    public void actionPerformed(ActionEvent e) {
        Object o = e.getSource();
        
        if(o == report){
            getNewReport();
        }
    }
    
    private void getNewReport() {
        receptor.getOutComm().getAnalysisSummaryReport(db.getName());
    }
    
    public void setDatabase(DatabaseDescriptor db){
        this.db = db;
        
        setAnalysisSummaryReport(receptor.getManager().getDatabaseReport(db.getName()));
        getNewReport();
    }

    public void setAnalysisSummaryReport(AnalysisSummaryMessage sum) {
        if(sum == null) {
            sums = new AnalysisSummaryMessage.SUM[0];
        } else if(sum.getDatabase().equals(db.getName())) {
            List list = sum.getSummaries();
            AnalysisSummaryMessage.SUM[] tmpSums = new AnalysisSummaryMessage.SUM[list.size()];

            Iterator it = list.iterator();
            for (int i = 0; i < tmpSums.length && it.hasNext(); i++) {
                tmpSums[i] = (AnalysisSummaryMessage.SUM)it.next();
            }

            sums = tmpSums;
        }
        table.tableChanged(cEvent);
    }

    public void showAnalysisReport(GUIStaticAnalysisModule mod) {
        JDialog dialog = new JDialog(MainGui.v(), mod.toString(), true);
        
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setSize(300, 300);
        Container c = dialog.getContentPane();
        c.setLayout(new BorderLayout());
        
        c.add(mod.getResultsComponent());
        
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }
    
    public int getRowCount() {
        return sums.length;
    }

    public int getColumnCount() {
        return columns.length;
    }

    public String getColumnName(int i) {
        return columns[i];
    }

    public Class getColumnClass(int c) {
        return columnClass[c];
    }

    public boolean isCellEditable(int r, int c) {
        return c > 2;
    }

    public Object getValueAt(int r, int c) {
        switch(c){
            case 0: return sums[r].getModule();
            case 1: return sums[r].getHandle().getInstanceID();
            case 2: return sums[r].getProgress();
            case 3: return "Get Report";
        }
        
        return null;
    }

    public void setValueAt(Object object, int i, int i0) {
    }

    public void addTableModelListener(TableModelListener l) {
    }

    public void removeTableModelListener(TableModelListener l) {
    }
    
    private void addControls() {
        setLayout(new BorderLayout());
        
        JPanel buttonsPanel = new JPanel();
        add(new JScrollPane(table = new JTable(this)));
        
        
        table.getColumnModel().getColumn(3).setCellEditor(this);
        editLabel = new JLabel("Get Report");
    }

    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        receptor.getOutComm().getAnalysisReport(sums[row].getHandle());
        
        return editLabel;
    }

    public Object getCellEditorValue() {
        return "Get Report";
    }

    public boolean isCellEditable(EventObject anEvent) {
        return true;
    }

    public boolean shouldSelectCell(EventObject anEvent) {
        return false;
    }

    public boolean stopCellEditing() {
        return true;
    }

    public void cancelCellEditing() {
    }

    public void addCellEditorListener(CellEditorListener l) {
    }

    public void removeCellEditorListener(CellEditorListener l) {
    }
}