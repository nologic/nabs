/*
 * StreamsPanel.java
 *
 * Created on January 1, 2006, 11:41 PM
 *
 */

package eunomia.gui.realtime.receptorAdmin;

import eunomia.core.managers.event.state.AddDatabaseEvent;
import eunomia.core.managers.event.state.AddDatabaseTypeEvent;
import eunomia.core.managers.event.state.AddModuleEvent;
import eunomia.core.managers.event.state.AddStreamServerEvent;
import eunomia.core.managers.event.state.ReceptorUserAddedEvent;
import eunomia.core.managers.event.state.ReceptorUserRemovedEvent;
import eunomia.core.managers.event.state.RemoveDatabaseEvent;
import eunomia.core.managers.event.state.RemoveStreamServerEvent;
import eunomia.core.managers.event.state.StreamStatusChangedEvent;
import eunomia.core.managers.listeners.ReceptorStateListener;
import eunomia.core.receptor.Receptor;
import eunomia.core.receptor.StreamServerDesc;
import eunomia.gui.realtime.ReceptorAdmin;
import eunomia.gui.realtime.receptorAdmin.editors.ProtocolEditor;
import eunomia.gui.realtime.receptorAdmin.editors.TCPEditor;
import eunomia.gui.realtime.receptorAdmin.editors.UDPEditor;
import eunomia.messages.receptor.ModuleHandle;
import eunomia.util.SpringUtilities;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpringLayout;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

/**
 *
 * @author Mikhail Sosonkin
 */
public class StreamsPanel extends JPanel implements ActionListener, 
        ListSelectionListener, ReceptorStateListener {
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

    private void addControls(){
        JPanel controlsPanel = new JPanel(new GridLayout(1, 2));
        JPanel fieldsPanel = new JPanel(new SpringLayout());
        JPanel protocolSpecific = new JPanel(new BorderLayout());
        JPanel buttonsPanel = new JPanel(new GridLayout(1, 2, 6, 6));
        JPanel streamEdit = new JPanel(new BorderLayout());
        
        setLayout(new BorderLayout());
        
        buttonsPanel.add(addStream = new JButton("Apply"));
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

    public void databaseAdded(AddDatabaseEvent e) {
    }

    public void databaseRemoved(RemoveDatabaseEvent e) {
    }

    public void databaseTypeAdded(AddDatabaseTypeEvent e) {
    }

    public void moduleAdded(AddModuleEvent e) {
        if(e.getType() == ModuleHandle.TYPE_FLOW) {
            fModSelection.setModel(new DefaultComboBoxModel(receptor.getState().getFlowModules().toArray()));
        }
    }

    public void streamServerAdded(AddStreamServerEvent e) {
        streamModel.add(e.getServer());
        streamTable.tableChanged(streamModel.getTableModelEvent());
    }

    public void streamServerRemoved(RemoveStreamServerEvent e) {
        streamModel.remove(e.getServer());
        streamTable.tableChanged(streamModel.getTableModelEvent());
    }

    public void streamStatusChanged(StreamStatusChangedEvent e) {
        streamTable.tableChanged(streamModel.getTableModelEvent());
    }

    public void receptorUserAdded(ReceptorUserAddedEvent e) {
    }

    public void receptorUserRemoved(ReceptorUserRemovedEvent e) {
    }
    
    private class StreamsTableModel implements TableModel {
        private String[] colNames = new String[]{"Active", "Name", "Interpreter", "Protocol"};
        private List servers;
        private TableModelEvent event;
        
        public StreamsTableModel(){
            servers = new ArrayList();
            event = new TableModelEvent(this);
        }
        
        private StreamServerDesc checkName(String name) {
            Iterator it = servers.iterator();
            while (it.hasNext()) {
                StreamServerDesc serv = (StreamServerDesc) it.next();
                if(serv.getName().equals(name)) {
                    return serv;
                }
            }
            
            return null;
        }
        
        public void add(StreamServerDesc stream) {
            StreamServerDesc desc = checkName(stream.getName());
            if(stream == desc) {
                return;
            } else if(desc != null) {
                servers.remove(desc);
            }
            
            servers.add(stream);
        }
        
        public void remove(StreamServerDesc stream) {
            servers.remove(stream);
        }
        
        public StreamServerDesc getServer(int i){
            return (StreamServerDesc)servers.get(i);
        }
        
        public TableModelEvent getTableModelEvent(){
            return event;
        }
        
        public int getRowCount(){
            return servers.size();
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
            StreamServerDesc stream = (StreamServerDesc)servers.get(rowIndex);
            switch(columnIndex){
                case 0: return Boolean.valueOf(stream.isConnected());
                case 1: return stream.getName();
                case 2: return stream.getModName();
                case 3: {
                    Object o = stream.getProtocol();
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
                connectStream(((StreamServerDesc)servers.get(rowIndex)).getName(), ((Boolean)aValue).booleanValue());
            }
        }
        
        public void addTableModelListener(TableModelListener l){
        }
        
        public void removeTableModelListener(TableModelListener l){
        }
    }
}