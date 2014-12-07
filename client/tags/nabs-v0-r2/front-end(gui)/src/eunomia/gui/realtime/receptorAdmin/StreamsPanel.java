/*
 * StreamsPanel.java
 *
 * Created on January 1, 2006, 11:41 PM
 *
 */

package eunomia.gui.realtime.receptorAdmin;

import eunomia.core.receptor.*;
import eunomia.gui.*;
import eunomia.gui.realtime.ReceptorAdmin;
import eunomia.gui.realtime.receptorAdmin.editors.ProtocolEditor;
import eunomia.gui.realtime.receptorAdmin.editors.TCPEditor;
import eunomia.gui.realtime.receptorAdmin.editors.UDPEditor;
import eunomia.util.SpringUtilities;
import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;

/**
 *
 * @author Mikhail Sosonkin
 */
public class StreamsPanel extends JPanel implements ActionListener, 
        ListSelectionListener {
    private HashMap protocolToEditor;
    private JTabbedPane tabs;
    private JTable streamTable;
    private Receptor receptor;
    private StreamsTableModel streamModel;
    private JButton update, addStream, removeStream;
    private JTextField name;
    private JComboBox fModSelection, protocolSelection;
    private JPanel pSpecPanel;
    private ProtocolEditor editor;
    
    public StreamsPanel(Receptor rec) {
        streamModel = new StreamsTableModel();
        receptor = rec;
        
        protocolToEditor = new HashMap();
        protocolToEditor.put("TCP", new TCPEditor());
        protocolToEditor.put("UDP", new UDPEditor());
        
        addControls();
    }

    public void actionPerformed(ActionEvent e){
        Object o = e.getSource();
        
        if(o == addStream){
            if(editor != null){
                receptor.getOutComm().addStream(name.getText(), fModSelection.getSelectedItem().toString(), editor.getDescriptor());
            }
        } else if(o == removeStream){
            receptor.getOutComm().removeStream(name.getText());
        } else if(o == protocolSelection){
            editor = (ProtocolEditor)protocolSelection.getSelectedItem();
            pSpecPanel.removeAll();
            pSpecPanel.add((JComponent)editor, BorderLayout.NORTH);
            pSpecPanel.validate();
            pSpecPanel.repaint();
        }
    }
    
    private void connectStream(String name, boolean con){
        receptor.getOutComm().connectStream(name, con);
    }
    
    public void valueChanged(ListSelectionEvent listSelectionEvent) {
        int row = streamTable.getSelectedRow();
        if(row > -1 && row < streamModel.getRowCount()){
            name.setText(streamModel.getValueAt(row, 1).toString());
            fModSelection.setSelectedItem(streamModel.getValueAt(row, 2));
            protocolSelection.setSelectedItem(streamModel.getServer(row).getProtocol().protoString());
            
            editor = (ProtocolEditor)protocolSelection.getSelectedItem();
            editor.setDescriptor(streamModel.getServer(row).getProtocol());
            pSpecPanel.removeAll();
            pSpecPanel.add((JComponent)editor, BorderLayout.NORTH);
        }
    }
    
    public void update(){
        streamTable.setEnabled(false);
        streamModel.update();
        streamTable.tableChanged(streamModel.getTableModelEvent());
        streamTable.setEnabled(true);
        fModSelection.setModel(new DefaultComboBoxModel(receptor.getState().getFlowModules().toArray()));
    }
    
    private void addControls(){
        JPanel controlsPanel = new JPanel(new GridLayout(1, 2));
        JPanel fieldsPanel = new JPanel(new SpringLayout());
        JPanel protocolSpecific = new JPanel(new BorderLayout());
        JPanel buttonsPanel = new JPanel(new GridLayout(1, 2, 6, 6));
        JPanel streamEdit = new JPanel(new BorderLayout());
        
        setLayout(new BorderLayout());
        
        buttonsPanel.add(addStream = new JButton("Add/Edit"));
        buttonsPanel.add(removeStream = new JButton("Remove"));

        fieldsPanel.add(ReceptorAdmin.makeLabel("Name"));
        fieldsPanel.add(name = new JTextField());
        fieldsPanel.add(ReceptorAdmin.makeLabel("Interpreter"));
        fieldsPanel.add(fModSelection = new JComboBox());
        fieldsPanel.add(ReceptorAdmin.makeLabel("Protocol"));
        fieldsPanel.add(protocolSelection = new JComboBox(new ProtocolEditor[]{new TCPEditor(), new UDPEditor()}));
        SpringUtilities.makeCompactGrid(fieldsPanel, 3, 2, 2, 2, 4, 4);
        
        protocolSpecific.add(new JScrollPane(pSpecPanel = new JPanel(new BorderLayout())));
        
        streamEdit.add(fieldsPanel);
        streamEdit.add(buttonsPanel, BorderLayout.SOUTH);
        controlsPanel.add(streamEdit);
        controlsPanel.add(protocolSpecific);
        add(controlsPanel, BorderLayout.SOUTH);
        add(new JScrollPane(streamTable = new JTable(streamModel)));
        
        addStream.addActionListener(this);
        removeStream.addActionListener(this);
        streamTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        streamTable.getSelectionModel().addListSelectionListener(this);
        protocolSelection.addActionListener(this);
        protocolSelection.setSelectedIndex(0);
    }
    
    private class StreamsTableModel implements TableModel {
        private String[] colNames = new String[]{"Active", "Name", "Interpreter", "Protocol"};
        private StreamServerDesc[] servers;
        private TableModelEvent event;
        
        public StreamsTableModel(){
            servers = new StreamServerDesc[]{};
            event = new TableModelEvent(this);
        }
        
        public void update(){
            servers = (StreamServerDesc[])receptor.getState().getStreamServers().toArray(new StreamServerDesc[]{});
        }
        
        public StreamServerDesc getServer(int i){
            return servers[i];
        }
        
        public TableModelEvent getTableModelEvent(){
            return event;
        }
        
        public int getRowCount(){
            return servers.length;
        }

        public int getColumnCount(){
            return colNames.length;
        }
        
        public String getColumnName(int columnIndex){
            return colNames[columnIndex];
        }
        
        public Class getColumnClass(int columnIndex){
            switch(columnIndex){
                case 0: return Boolean.class;
                case 1:
                case 2: 
                case 3: return String.class;
            }
            return null;
        }
        
        public boolean isCellEditable(int rowIndex, int columnIndex){
            return columnIndex == 0;
        }
        
        public Object getValueAt(int rowIndex, int columnIndex){
            switch(columnIndex){
                case 0: return Boolean.valueOf(servers[rowIndex].isConnected());
                case 1: return servers[rowIndex].getName();
                case 2: return servers[rowIndex].getModName();
                case 3: {
                    Object o = servers[rowIndex].getProtocol();
                    if(o == null){
                        return "";
                    } else {
                        return o.toString();
                    }
                }
            }
            
            return "";
        }
        
        public void setValueAt(Object aValue, int rowIndex, int columnIndex){
            if(columnIndex == 0){
                connectStream(servers[rowIndex].getName(), ((Boolean)aValue).booleanValue());
            }
        }
        
        public void addTableModelListener(TableModelListener l){
        }
        
        public void removeTableModelListener(TableModelListener l){
        }
    }
}