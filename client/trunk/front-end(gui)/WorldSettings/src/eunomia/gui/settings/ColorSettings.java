/*
 * WorldSettings.java
 *
 * Created on July 6, 2005, 2:38 PM
 *
 */

package eunomia.gui.settings;

import eunomia.config.Settings;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;
import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.Border;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

/**
 *
 * @author Mikhail Sosonkin
 */

public class ColorSettings extends JPanel implements ActionListener {
    private JButton apply;
    private JButton defaults;
    private JTable flowsTable;
    private Vector colors;
    private TableModelEvent tEvent;
    private JDialog closeDialog;
    
    public ColorSettings() {
        addControls();
    }
    
    private void applySettings(){
        for(int i = 0; i < colors.size(); i++){
            Settings.v().setTypeColor(i, (Color)colors.get(i));
        }
        
        try {
            Settings.v().save();
        } catch (Exception ex){
            ex.printStackTrace();
        }
    }
    
    private void resetToDefault(){
        if(JOptionPane.showConfirmDialog(this, "Reset settings?", "Global Settings", 
                JOptionPane.YES_NO_CANCEL_OPTION) == JOptionPane.YES_OPTION){
            Settings.v().reset();
            colors.clear();
            for(int i = 0; i < 8; i++){
                colors.add(Settings.v().getTypeColor(i));
            }
            flowsTable.tableChanged(tEvent);
            try {
                Settings.v().save();
            } catch (Exception ex){
                ex.printStackTrace();
            }
        }
    }
    
    public void actionPerformed(ActionEvent e){
        Object o = e.getSource();
        
        if(o == apply){
            applySettings();
            if(closeDialog != null) {
                closeDialog.setVisible(false);
            }
        } else if(o == defaults){
            resetToDefault();
        }
    }
    
    private void addControls(){
        setLayout(new BorderLayout());
        
        JPanel buttonsPanel = new JPanel(new GridLayout(1, 2));
        
        ColorTableModel cModel = new ColorTableModel(colors = new Vector());
        buttonsPanel.add(defaults = new JButton("Reset To Defaults"));
        buttonsPanel.add(apply = new JButton("Apply"));
        add(new JScrollPane(flowsTable = new JTable(cModel)));
        add(buttonsPanel, BorderLayout.SOUTH);
        
        flowsTable.setDefaultRenderer(Color.class, new ColorRenderer(true));
        flowsTable.setDefaultEditor(Color.class, new ColorEditor());
        
        apply.addActionListener(this);
        defaults.addActionListener(this);
        
        tEvent = new TableModelEvent(cModel);
    }
    
    private class ColorTableModel implements TableModel {
        private String[] colNames;
        private Vector colors;
        
        public ColorTableModel(Vector colorStore){
            colNames = new String[]{"Name", "Color"};
            colors = colorStore;
            for(int i = 0; i < 8; i++){
                colors.add(Settings.v().getTypeColor(i));
            }
        }
        
        public int getRowCount(){
            return 8;
        }
        
        public int getColumnCount(){
            return colNames.length;
        }
        
        public String getColumnName(int columnIndex){
            return colNames[columnIndex];
        }
        
        public Class getColumnClass(int columnIndex){
            if(columnIndex == 1){
                return Color.class;
            }
            return String.class;
        }
        
        public boolean isCellEditable(int rowIndex, int columnIndex){
            return columnIndex == 1;
        }
        
        public Object getValueAt(int rowIndex, int columnIndex){
            switch(columnIndex){
                case 0: return Settings.typeNames[rowIndex];
                case 1: return colors.get(rowIndex);
            }
            throw new RuntimeException("Column too big: " + columnIndex);
        }
        
        public void setValueAt(Object aValue, int rowIndex, int columnIndex){
            if(columnIndex == 1){
                colors.set(rowIndex, aValue);
            }
        }
        
        public void addTableModelListener(TableModelListener l){
        }
        
        public void removeTableModelListener(TableModelListener l){
        }
    }
    
    private class ColorEditor extends AbstractCellEditor implements TableCellEditor, ActionListener {
        private Color currentColor;
        private JButton button;
        private JColorChooser colorChooser;
        private JDialog dialog;
        protected static final String EDIT = "edit";
        
        public ColorEditor() {
            button = new JButton();
            button.setActionCommand(EDIT);
            button.addActionListener(this);
            button.setBorderPainted(false);
            
            colorChooser = new JColorChooser();
            dialog = JColorChooser.createDialog(button, "Pick a Color", true, colorChooser, this, null);
        }

        public void actionPerformed(ActionEvent e) {
            if (EDIT.equals(e.getActionCommand())) {
                button.setBackground(currentColor);
                colorChooser.setColor(currentColor);
                dialog.setVisible(true);
                
                fireEditingStopped();
                
            } else {
                currentColor = colorChooser.getColor();
            }
        }

        public Object getCellEditorValue() {
            return currentColor;
        }
        
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            currentColor = (Color)value;
            return button;
        }
    }
    
    public class ColorRenderer extends JLabel implements TableCellRenderer {
        private Border unselectedBorder = null;
        private Border selectedBorder = null;
        private boolean isBordered = true;
        
        public ColorRenderer(boolean isBordered) {
            this.isBordered = isBordered;
            setOpaque(true);
        }
        
        public Component getTableCellRendererComponent(JTable table, Object color, boolean isSelected, boolean hasFocus, int row, int column) {
            Color newColor = (Color)color;
            setBackground(newColor);
            if (isBordered) {
                if (isSelected) {
                    if (selectedBorder == null) {
                        selectedBorder = BorderFactory.createMatteBorder(2,5,2,5, table.getSelectionBackground());
                    }
                    setBorder(selectedBorder);
                } else {
                    if (unselectedBorder == null) {
                        unselectedBorder = BorderFactory.createMatteBorder(2,5,2,5, table.getBackground());
                    }
                    setBorder(unselectedBorder);
                }
            }
            
            setToolTipText("RGB value: " + newColor.getRed() + ", "
                    + newColor.getGreen() + ", "
                    + newColor.getBlue());
            return this;
        }
    }

    public void setCloseDialog(JDialog closeDialog) {
        this.closeDialog = closeDialog;
    }
}