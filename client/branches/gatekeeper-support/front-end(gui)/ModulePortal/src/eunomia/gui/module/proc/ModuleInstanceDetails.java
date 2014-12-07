/*
 * ModulePanelStreamTable.java
 *
 * Created on December 15, 2006, 4:35 PM
 *
 */

package eunomia.gui.module.proc;

import eunomia.core.receptor.Receptor;
import eunomia.core.receptor.StreamServerDesc;
import eunomia.module.ProcFrontendModule;
import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
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
    private ProcFrontendModule module;
    private Receptor receptor;
    private List servers;
    private TableModelEvent event;
    
    public ModuleInstanceDetails(Receptor rec) {
        colNames = new String[]{"Connected", "Stream Server"};
        colClasses = new Class[]{Boolean.class, String.class};
        
        servers = new ArrayList();
        receptor = rec;
        event = new TableModelEvent(this);
        
        addControls();
    }
    
    public void setModule(ProcFrontendModule mod){
        module = mod;
        table.tableChanged(event);
        table.setEnabled(mod != null);
    }

    public void update(){
        boolean changed = false;
        
        List servs = receptor.getState().getStreamServers();
        Iterator it = servs.iterator();
        while (it.hasNext()) {
            Object d = it.next();
            if(!servers.contains(d)) {
                servers.add(d);
                changed = true;
            }
        }
        
        it = servers.iterator();
        while (it.hasNext()) {
            Object d = it.next();
            if(!servs.contains(d)) {
                it.remove();
                changed = true;
            }
        }
        
        if(changed) {
            table.tableChanged(event);
        }
    }

    public int getRowCount() {
        return servers.size();
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
        StreamServerDesc desc = (StreamServerDesc)servers.get(r);
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
            StreamServerDesc desc = (StreamServerDesc)servers.get(r);
            receptor.getOutComm().connectModuleToServer(module.getHandle(), desc.getName(), ((Boolean)aValue).booleanValue());
        }
    }
    
    public void selectAll(boolean v) {
        for (int i = 0; i < servers.size(); i++) {
            if(!module.getStreamsSet().contains(servers.get(i))){
                setValueAt(Boolean.valueOf(v), i, 0);
            }
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