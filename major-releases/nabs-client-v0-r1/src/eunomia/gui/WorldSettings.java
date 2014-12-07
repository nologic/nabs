/*
 * WorldSettings.java
 *
 * Created on July 6, 2005, 2:38 PM
 *
 */

package eunomia.gui;

import eunomia.Exiter;
import eunomia.config.Settings;
import eunomia.core.data.flow.Flow;

import javax.swing.table.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Vector;
import javax.swing.border.Border;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

/**
 *
 * @author Mikhail Sosonkin
 */

public class WorldSettings extends JInternalFrame implements ActionListener, Exiter {
    private JButton apply;
    private JButton defaults;
    private JTable flowsTable;
    private Vector colors;
    private TableModelEvent tEvent;
    
    public WorldSettings() {
        super("Eunomia Global Settings");
        
        setSize(500, 400);
        setDefaultCloseOperation(JInternalFrame.HIDE_ON_CLOSE);
        setMaximizable(true);
        setResizable(true);
        setClosable(true);
        
        addControls();
    }
    
    private void applySettings(){
        for(int i = 0; i < colors.size(); i++){
            Settings.setTypeColor(i, (Color)colors.get(i));
        }
        try {
            Settings.save();
        } catch (Exception ex){
            ex.printStackTrace();
        }
    }
    
    private void resetToDefault(){
        if(JOptionPane.showConfirmDialog(this, "Reset settings?", "Global Settings", 
                JOptionPane.YES_NO_CANCEL_OPTION) == JOptionPane.YES_OPTION){
            Settings.reset();
            colors.clear();
            for(int i = 0; i < Flow.NUM_TYPES; i++){
                colors.add(Settings.getTypeColor(i));
            }
            flowsTable.tableChanged(tEvent);

            try {
                Settings.save();
            } catch (Exception ex){
                ex.printStackTrace();
            }
        }
    }
    
    public void actionPerformed(ActionEvent e){
        Object o = e.getSource();
        
        if(o == apply){
            applySettings();
        } else if(o == defaults){
            resetToDefault();
        }
    }
    
    public void startExitSequence(){
    }
    
    private void addControls(){
        JPanel buttonsPanel = new JPanel(new GridLayout(1, 2));
        Container c = getContentPane();
        
        c.setLayout(new BorderLayout());
        
        ColorTableModel cModel = new ColorTableModel(colors = new Vector());
        buttonsPanel.add(defaults = new JButton("Reset To Defaults"));
        buttonsPanel.add(apply = new JButton("Apply"));
        add(new JScrollPane(flowsTable = new JTable(cModel)));
        add(buttonsPanel, BorderLayout.SOUTH);
        
        flowsTable.setDefaultRenderer(Color.class, new ColorRenderer(true));
        flowsTable.setDefaultEditor(Color.class, new ColorEditor());
        
        apply.addActionListener(this);
        defaults.addActionListener(this);
        defaults.addMouseListener(MyMouseListener.ins);
        apply.addMouseListener(MyMouseListener.ins);
        
        tEvent = new TableModelEvent(cModel);
    }
    
    private class ColorTableModel implements TableModel {
        private String[] colNames;
        private Vector colors;
        
        public ColorTableModel(Vector colorStore){
            colNames = new String[]{"Number", "Name", "Color"};
            colors = colorStore;
            for(int i = 0; i < Flow.NUM_TYPES; i++){
                colors.add(Settings.getTypeColor(i));
            }
        }
        
        public int getRowCount(){
            return Flow.NUM_TYPES;
        }
        
        public int getColumnCount(){
            return colNames.length;
        }
        
        public String getColumnName(int columnIndex){
            return colNames[columnIndex];
        }
        
        public Class getColumnClass(int columnIndex){
            if(columnIndex == 2){
                return Color.class;
            }
            return String.class;
        }
        
        public boolean isCellEditable(int rowIndex, int columnIndex){
            return columnIndex == 2;
        }
        
        public Object getValueAt(int rowIndex, int columnIndex){
            switch(columnIndex){
                case 0: return Integer.valueOf(rowIndex);
                case 1: return Flow.typeNames[rowIndex];
                case 2: return colors.get(rowIndex);
            }
            throw new RuntimeException("Column too big: " + columnIndex);
        }
        
        public void setValueAt(Object aValue, int rowIndex, int columnIndex){
            if(columnIndex == 2){
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
}