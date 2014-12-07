/*
 * ModulePanelStreamTable.java
 *
 * Created on December 15, 2006, 4:35 PM
 *
 */

package eunomia.gui.realtime.receptorAdmin.module;

import eunomia.core.receptor.Receptor;
import eunomia.core.receptor.StreamServerDesc;
import eunomia.plugin.GUIPlugin;
import java.awt.BorderLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

/**
 *
 * @author Mikhail Sosonkin
 */
public class ModuleInstanceDetails extends JPanel implements TableModel {
    private Class[] colClasses;
    private String[] colNames;
    private JTable table;
    private GUIPlugin module;
    private Receptor receptor;
    private StreamServerDesc[] servers;
    private TableModelEvent event;
    
    public ModuleInstanceDetails(Receptor rec) {
        colNames = new String[]{"Connected", "Stream Server"};
        colClasses = new Class[]{Boolean.class, String.class};
        servers = new StreamServerDesc[]{};
        receptor = rec;
        event = new TableModelEvent(this);
        
        addControls();
    }
    
    public void setGUIPlugin(GUIPlugin mod){
        module = mod;
        table.tableChanged(event);
        table.setEnabled(mod != null);
    }

    public void update(){
        servers = (StreamServerDesc[])receptor.getState().getStreamServers().toArray(new StreamServerDesc[]{});
        table.tableChanged(event);
    }

    public int getRowCount() {
        return servers.length;
    }

    public int getColumnCount() {
        return colNames.length;
    }

    public String getColumnName(int c) {
        return colNames[c];
    }

    public Class getColumnClass(int c) {
        return colClasses[c];
    }

    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return columnIndex == 0;
    }

    public Object getValueAt(int r, int c) {
        StreamServerDesc desc = servers[r];
        switch(c) {
            case 0: 
                if(module == null) {
                    return false;
                }
                return Boolean.valueOf(module.getStreamsSet().contains(desc));
            case 1: return desc.getName();
        }
        
        return null;
    }

    public void setValueAt(Object aValue, int r, int c) {
        if(module != null) {
            receptor.getOutComm().connectModuleToServer(module.getModuleHandle(), servers[r].getName(), ((Boolean)aValue).booleanValue());
        }
    }

    public void addTableModelListener(TableModelListener l) {
    }

    public void removeTableModelListener(TableModelListener l) {
    }

    private void addControls() {
        setLayout(new BorderLayout());
        add(new JScrollPane(table = new JTable(this)));
    }
}