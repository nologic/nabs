/*
 * DatabaseReportsPanel.java
 *
 * Created on December 3, 2006, 5:10 PM
 *
 */

package eunomia.gui.archival;

import eunomia.core.data.staticData.DatabaseReportListener;
import eunomia.core.receptor.Receptor;
import eunomia.messages.DatabaseDescriptor;
import eunomia.messages.receptor.ncm.AnalysisSummaryMessage;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import com.vivic.eunomia.sys.util.Util;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
/**
 *
 * @author Mikhail Sosonkin
 */
public class DatabaseReportsPanel extends JPanel implements ActionListener, DatabaseReportListener, TableModel, TableCellRenderer {
    private static final String[] columns = new String[]{"Name", "ID", "Progress"};
    private static final Class[] columnClass = new Class[]{String.class, Integer.class, Double.class};
    
    private DatabaseDescriptor db;
    private Receptor receptor;
    private AnalysisSummaryMessage.SUM[] sums;
    private TableModelEvent cEvent;
    private JTable table;
    private JLabel editLabel;
    private JButton report;
    
    private PercentPanel renderComp;
    
    public DatabaseReportsPanel(Receptor rec) {
        receptor = rec;
        sums = new AnalysisSummaryMessage.SUM[0];
        cEvent = new TableModelEvent(this);
        
        renderComp = new PercentPanel();
        
        addControls();
        
        receptor.getManager().addDatabaseReportListener(this);
    }

    public void actionPerformed(ActionEvent e) {
        Object o = e.getSource();
        
        if(o == report){
            getNewReport();
        }
    }
    
    public void getNewReport() {
        //receptor.getOutComm().getAnalysisSummaryReport();
    }

    public void setAnalysisSummaryReport(AnalysisSummaryMessage sum) {
        editLabel.setText("Received: " + Util.getTimeStamp(sum.getTimestamp(), true, true));
        
        List list = sum.getSummaries();
        AnalysisSummaryMessage.SUM[] tmpSums = new AnalysisSummaryMessage.SUM[list.size()];

        Iterator it = list.iterator();
        for (int i = 0; i < tmpSums.length && it.hasNext(); i++) {
            tmpSums[i] = (AnalysisSummaryMessage.SUM)it.next();
        }

        sums = tmpSums;
        table.tableChanged(cEvent);
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
        return false;
    }

    public Object getValueAt(int r, int c) {
        switch(c){
            case 0: return sums[r].getHandle().getModuleName();
            case 1: return sums[r].getHandle().getInstanceID();
            case 2: return Double.valueOf(sums[r].getProgress());
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
        JPanel buttonsPanel = new JPanel(new GridLayout(1, 2));
        setLayout(new BorderLayout());
        
        buttonsPanel.add(editLabel = new JLabel("Received: (Not yet)"));
        buttonsPanel.add(report = new JButton("Refresh"));
        
        add(new JScrollPane(table = new JTable(this)));
        add(buttonsPanel, BorderLayout.SOUTH);
        
        report.addActionListener(this);
        table.getColumnModel().getColumn(2).setCellRenderer(this);
    }

    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Double d = (Double)value;
        renderComp.setBackground((isSelected?table.getSelectionBackground():table.getBackground()));
        renderComp.setValue(d.doubleValue());
        
        return renderComp;
    }
    
    private class PercentPanel extends JPanel {
        private JLabel renderLabel;
        private double val;
        
        public PercentPanel() {
            setOpaque(false);
            setLayout(new BorderLayout());
            add(renderLabel = new JLabel());
            renderLabel.setOpaque(false);
            renderLabel.setHorizontalAlignment(JLabel.CENTER);
            renderLabel.setFont(new Font(renderLabel.getFont().getName(), Font.PLAIN, renderLabel.getFont().getSize()));
        }
        
        public void setValue(double d) {
            val = d;
            renderLabel.setText(Double.valueOf(d * 100.0).toString() + "%");
        }
        
        public void paint(Graphics g){
            Dimension dim = getSize();
            
            g.setColor(getBackground());
            g.fillRect(0, 0, dim.width, dim.height);

            g.setColor(Color.ORANGE);
            g.fillRect(2, 2, (int)((double)dim.width * val) - 4, dim.height - 4);
            
            super.paint(g);
        }
    }
}