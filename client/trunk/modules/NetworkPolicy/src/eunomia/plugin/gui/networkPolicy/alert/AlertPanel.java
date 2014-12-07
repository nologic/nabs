/*
 * AlertPanel.java
 *
 * Created on January 3, 2007, 3:23 PM
 */

package eunomia.plugin.gui.networkPolicy.alert;

import com.vivic.eunomia.module.frontend.FrontendProcessorModule;
import eunomia.Descriptor;
import eunomia.messages.receptor.ModuleHandle;
import eunomia.plugin.com.networkPolicy.AlertItem;
import eunomia.plugin.com.networkPolicy.PolicyItem;
import eunomia.plugin.gui.networkPolicy.Main;
import eunomia.plugin.gui.networkPolicy.table.AlertCellRendererSorter;
import eunomia.plugin.gui.networkPolicy.table.AlertsTableModel;
import eunomia.util.HostResolver;
import com.vivic.eunomia.sys.util.Util;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableColumnModel;

/**
 *
 * @author  kulesh, Mikhail Sosonkin.
 */
public class AlertPanel extends JPanel implements ListSelectionListener, ActionListener, MouseListener {
    private static Comparator[] comprators;
    
    private AlertsTableModel model;
    private JTable table;
    private JList list;
    
    private AlertEditor editor;
    private List alerts;
    
    private Main main;
    
    private JMenuItem[] markItems;
    private JMenuItem deleteAlerts;
    private JMenuItem deleteAlertsForHost;
    private JMenuItem deleteAlertTypeForHost;
    private JMenuItem moveToDetails;
    
    public AlertPanel(Main main) {
        this.main = main;
        alerts = new ArrayList();
        
        addControls();
        addPopupMenu();
    }
    
    public JList getHostsList() {
        return list;
    }
    
    public AlertEditor getAlertEditor() {
        return editor;
    }
    
    private void addControls() {
        setLayout(new BorderLayout());
        
        model = new AlertsTableModel(alerts);
        add(new JScrollPane(table = new JTable(model)), BorderLayout.CENTER);
        add(editor = new AlertEditor(main), BorderLayout.SOUTH);
        
        editor.setBorder(BorderFactory.createTitledBorder("Selected Alert:"));
        
        list = new JList(model);
        list.setLayoutOrientation(JList.VERTICAL_WRAP);
        list.setPrototypeCellValue("  255.255.255.255  ");
        list.setVisibleRowCount(4);
    
        AlertCellRendererSorter renderer = new AlertCellRendererSorter(table, model);
        TableColumnModel cModel = table.getColumnModel();
        for (int i = 0; i < cModel.getColumnCount(); i++) {
            cModel.getColumn(i).setCellRenderer(renderer);
        }
        table.getTableHeader().addMouseListener(renderer);
        table.getSelectionModel().addListSelectionListener(this);
        
        list.addListSelectionListener(this);
        list.addMouseListener(this);
    }
    
    private void addPopupMenu() {
        JPopupMenu menu = new JPopupMenu();
        
        markItems = new JMenuItem[AlertsTableModel.status.length];
        for (int i = 0; i < markItems.length; i++) {
            markItems[i] = menu.add("Mark as " + AlertsTableModel.status[i]);
            markItems[i].addActionListener(this);
        }
        
        menu.addSeparator();
        
        moveToDetails = menu.add("View Related Flows for this Host");
        
        menu.addSeparator();
        
        deleteAlerts = menu.add("Delete");
        deleteAlertsForHost = menu.add("Remove all Alerts for this Host");
        deleteAlertTypeForHost = menu.add("Remove This Alert Type for this Host");
        
        deleteAlerts.addActionListener(this);
        deleteAlertsForHost.addActionListener(this);
        deleteAlertTypeForHost.addActionListener(this);
        moveToDetails.addActionListener(this);
        
        table.setComponentPopupMenu(menu);
    }
    
    private void markAlerts(int[] items, int mark) {
        List list = new ArrayList();
        for (int i = 0; i < items.length; i++) {
            AlertItem item = model.getAlert(items[i]);
            if(item.getStatus() != mark) {
                if(item.getStatus() == AlertItem.NEW) {
                    item.getPolicyItem().addNewAlerts(-1);
                } else {
                    item.getPolicyItem().addNewAlerts( mark == AlertItem.NEW?1:0 );
                }

                list.add(item);
                item.setStatus(mark);
            }
        }
        
        try {
            main.saveAlertList(list);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    
    private void deleteAlerts(int[] rows) {
        long[] alertsIds = new long[rows.length];
        AlertItem[] alerts = new AlertItem[rows.length];
        
        for (int i = 0; i < rows.length; i++) {
            AlertItem item = model.getAlert(rows[i]);
            alerts[i] = item;
            alertsIds[i] = item.getAlertID();
        }
        
        for (int i = 0; i < alerts.length; i++) {
            model.removeAlert(alerts[i]);
            deleteFromSource(alerts[i]);
        }
        
        if(alertsIds != null) {
            try {
                main.deleteAlertList(alertsIds);
            } catch (IOException ex) {
            }
        }
    }
    
    public void actionPerformed(ActionEvent e) {
        Object o = e.getSource();
        
        if(o instanceof JMenuItem) {
            for (int i = 0; i < markItems.length; i++) {
                if(markItems[i] == o) {
                    synchronized(alerts) {
                        markAlerts(table.getSelectedRows(), i);
                    }
                    table.repaint();
                    return;
                }
            }
            
            if(o == deleteAlerts) {
                deleteSelectedAlerts();
            } else if(o == deleteAlertsForHost) {
                deleteAlertsForHost();
            } else if(o == deleteAlertTypeForHost) {
                deleteAlertTypeForHost();
            } else if(o == moveToDetails) {
                moveToDetails();
            }
        }
    }
    
    public void locateAndShowHost(long ip) {
        synchronized(alerts) {
            int size = model.getRowCount();
            for (int i = 0; i < size; i++) {
                AlertItem item = model.getAlert(i);
                if(item.getFlowId().getSourceIP() == ip) {
                    table.setRowSelectionInterval(i, i);
                    table.scrollRectToVisible(table.getCellRect(i, 0, true));
                    break;
                }
            }
        }
        
        showInDetails(ip);
    }
    
    private void showInDetails(long ip) {
        List mods = main.getReceptor().getManager().getModuleHandles("networkStatus", Descriptor.TYPE_PROC);
        if(mods.size() > 0) {
            ModuleHandle handle = (ModuleHandle)mods.get(0);
            FrontendProcessorModule module = (FrontendProcessorModule)main.getReceptor().getManager().getEunomiaModule(handle);
            module.setProperty("AHD", Long.toString(ip));
        }
    }
    private void moveToDetails() {
        AlertItem item;
        synchronized(alerts) {
            int row = table.getSelectedRow();
            item = (AlertItem)model.getAlert(row);
        }
        
        if(item != null) {
            showInDetails(item.getFlowId().getSourceIP());
        }
    }
    
    private void deleteFromSource(AlertItem alert) {
        synchronized(alerts) {
            alerts.remove(alert);
        }
    }
    
    private void deleteAlertTypeForHost() {
        List ids = new ArrayList();
        long[] lids = null;
        
        synchronized(alerts) {
            int row = table.getSelectedRow();
            AlertItem item = (AlertItem)model.getAlert(row);
            
            if(item != null) {
                long ip = item.getFlowId().getSourceIP();
                long pId = item.getPolicyID();
                
                Iterator it = alerts.iterator();
                while(it.hasNext()) {
                    AlertItem alert = (AlertItem)it.next();
                    if(alert.getPolicyID() == pId && alert.getFlowId().getSourceIP() == ip) {
                        ids.add(alert);
                    }
                }
            }
        }
        
        if(JOptionPane.showConfirmDialog(this, "Delete " + ids.size() + " Alerts?") != JOptionPane.YES_OPTION) {
            return;
        }
        
        lids = new long[ids.size()];
        for (int i = 0; i < lids.length; i++) {
            AlertItem alert = (AlertItem)ids.get(i);
            lids[i] = alert.getAlertID();
            model.removeAlert(alert);
            deleteFromSource(alert);
        }
        
        try {
            main.deleteAlertList(lids);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    
    public void deleteAlertsForPolicy(PolicyItem pi) {
        List ids = new ArrayList();
        synchronized(alerts) {
            Iterator it = alerts.iterator();
            while(it.hasNext()) {
                AlertItem alert = (AlertItem)it.next();
                if(alert.getPolicyItem() == pi) {
                    ids.add(alert);
                }
            }
        }
        
        Iterator it = ids.iterator();
        while (it.hasNext()) {
            AlertItem alert = (AlertItem) it.next();
            model.removeAlert(alert);
            deleteFromSource(alert);
        }
    }
    
    private void deleteAlertsForHost() {
        List ids = new ArrayList();
        long[] lids = null;
        
        synchronized(alerts) {
            int row = table.getSelectedRow();
            AlertItem item = (AlertItem)model.getAlert(row);
            
            if(item != null) {
                long ip = item.getFlowId().getSourceIP();
                Iterator it = alerts.iterator();
                while(it.hasNext()) {
                    AlertItem alert = (AlertItem)it.next();
                    if(alert.getFlowId().getSourceIP() == ip) {
                        ids.add(alert);
                    }
                }
            }
        }
        
        if(JOptionPane.showConfirmDialog(this, "Delete " + ids.size() + " Alerts?") != JOptionPane.YES_OPTION) {
            return;
        }
        
        lids = new long[ids.size()];
        for (int i = 0; i < lids.length; i++) {
            AlertItem alert = (AlertItem)ids.get(i);
            lids[i] = alert.getAlertID();
            model.removeAlert(alert);
            deleteFromSource(alert);
        }
        
        try {
            main.deleteAlertList(lids);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    
    private void deleteSelectedAlerts() {
        long[] alertsIds = null;
        
        if(JOptionPane.showConfirmDialog(this, "Delete " + table.getSelectedRowCount() + " Alerts?") != JOptionPane.YES_OPTION) {
            return;
        }
        
        synchronized(alerts) {
            deleteAlerts(table.getSelectedRows());
        }
    }
    
    public void insertAlertItem(AlertItem ai, int type){
        HostResolver.addRequest(ai);
        ai.getPolicyItem().addAlerts(1);
        if(ai.getStatus() == AlertItem.NEW) {
            ai.getPolicyItem().addNewAlerts(1);
        }

        synchronized(alerts) {
            alerts.add(ai);
            model.addAlert(ai);
        }
    }
    
    public void removeAlertItem(AlertItem ai){
    }
    
    public void showType(int type) {
        model.setTypeVisible(type);
        model.fireListChanged();
    }
    
    public void showPolicy(PolicyItem item) {
        model.setPolicyVisible(item);
        model.fireListChanged();
    }
    
    public void valueChanged(ListSelectionEvent e) {
        Object o = e.getSource();
        if(o == table.getSelectionModel()) {
            int row = table.getSelectedRow();
            if(row >= 0) {
                AlertItem item = model.getAlert(row);
                editor.showAlert(item);
            }
        } else if(o == list) {
            if(!e.getValueIsAdjusting()) {
                model.setSelectedIPs(list.getSelectedValues());
                table.repaint();
            }
        }
    }

    public void mouseClicked(MouseEvent e) {
        if(e.getClickCount() == 2) {
            Object o = list.getSelectedValue();
            if(o != null) {
                String ip = o.toString();
                locateAndShowHost(Util.getLongIp(ip));
            }
        }
    }

    public void mousePressed(MouseEvent e) {
    }

    public void mouseReleased(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }
}
