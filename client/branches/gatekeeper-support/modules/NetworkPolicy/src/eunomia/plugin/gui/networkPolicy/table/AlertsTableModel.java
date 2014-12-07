/*
 * AlertsTableModel.java
 *
 * Created on March 26, 2007, 10:16 PM
 *
 */

package eunomia.plugin.gui.networkPolicy.table;

import eunomia.plugin.com.networkPolicy.AlertItem;
import eunomia.plugin.com.networkPolicy.PolicyItem;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.swing.ListModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author Mikhail Sosonkin
 */
public class AlertsTableModel extends DefaultTableModel implements ListModel {
    public static final String[] status = new String[] {"New", "Open", "Pending", "Closed", "Unknown"};
    private static Class[] types = new Class [] {String.class, Integer.class, String.class, String.class, Object.class, String.class};
    private static int ALL_TYPES = PolicyItem.NUM_TYPES;
    
    static String[] columns = new String [] {"Policy Violated", "Violations", "Violator", "Host Name", "Time", "Status", "Notes"};
    
    private List source;
    
    private int visibleType;
    private PolicyItem visiblePolicy;
    private Comparator comparator;
    
    // Address List
    private Set addresses;
    private List addrArray;
    private List listListeners;
    private ListDataEvent lde;
    private Object[] selected;
    
    public AlertsTableModel(List source) {
        super(columns, 0);
        
        selected = new String[0];
        lde = new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, 0, 0);
        addrArray = new ArrayList();
        listListeners = new LinkedList();
        addresses = new HashSet();
        visibleType = ALL_TYPES;
        this.source = source;
    }
    
    public void setSelectedIPs(Object[] ips) {
        selected = ips;
    }
    
    public boolean isSelectedIP(String ip) {
        for (int i = 0; i < selected.length; i++) {
            if(selected[i].equals(ip)) {
                return true;
            }
        }
        
        return false;
    }
    
    // These funtions are not really efficient... but I wonder if it's enough.
    public void setTypeVisible(int type) {
        int oldVType;
        
        if(visibleType == type) {
            return;
        }
        
        oldVType = visibleType;
        visiblePolicy = null;
        visibleType = type;
        
        clear();
        addAlerts();
        
        fireTableRowsInserted(0, dataVector.size() - 1);
    }
    
    private void clear() {
        dataVector.clear();
        addresses.clear();
    }
    
    private boolean showAccept(AlertItem item) {
        PolicyItem aPol = item.getPolicyItem();
        
        if(visiblePolicy == null) {
            return visibleType == ALL_TYPES || aPol.getPolicyType() == visibleType;
        } else {
            return aPol == visiblePolicy;
        }
    }
    
    private void addAlerts() {
        synchronized(source) {
            Iterator it = source.iterator();
            while (it.hasNext()) {
                AlertItem ai = (AlertItem) it.next();
                if(showAccept(ai)) {
                    addresses.add(ai.getIpString());
                    dataVector.add(ai);
                }
            }
            addrArray.clear();
            addrArray.addAll(addresses);
            Collections.sort(addrArray);
        }
        
        sort(comparator);
    }
    
    public void setPolicyVisible(PolicyItem pi) {
        visibleType = -1;
        visiblePolicy = pi;
        
        clear();
        addAlerts();
        
        fireTableRowsInserted(0, dataVector.size() - 1);
    }

    public Class getColumnClass(int columnIndex) {
        return types [columnIndex];
    }

    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }
    
    public void addAlert(AlertItem item) {
        int type = item.getPolicyItem().getPolicyType();
        if(showAccept(item)) {
            String ip = item.getIpString();
            if(!addresses.contains(ip)) {
                addresses.add(ip);
                addrArray.add(ip);
                Collections.sort(addrArray);
            }
            insertRow(0, item);
        }
    }
    
    public void removeAlert(AlertItem item) {
        synchronized(dataVector) {
            int row = dataVector.indexOf(item);
            dataVector.remove(item);
            fireTableRowsDeleted(row, row);
            fireListChanged();
        }
    }

    public void insertRow(int row, Object rowData) {
        dataVector.insertElementAt(rowData, row); 
        fireTableRowsInserted(row, row);
        fireListChanged();
    }
    
    public Object getValueAt(int row, int column) {
        AlertItem a = (AlertItem)dataVector.elementAt(row);
        switch(column) {
            case 0: return a.getPolicyItem().toString();
            case 1: return Long.toString(a.getViolations());
            case 2: return a.getIpString();
            case 3: 
                String hostname = a.getHostName();
                return (hostname == null?"(Unknown)":hostname);
            case 4: return a.getFirstSeenString();
            case 5: return status[a.getStatus()];
            case 6: return a.getNotes();
        }
        
        return null;
    }

    public AlertItem getAlert(int row) {
        if(row > dataVector.size() || row < 0) {
            return null;
        }
        
        return (AlertItem)dataVector.get(row);
    }

    void sort(Comparator comparator) {
        if(comparator != null) {
            synchronized(dataVector) {
                this.comparator = comparator;
                Collections.sort(dataVector, comparator);
            }
        }
    }

    //List Model
    public void fireListChanged() {
        Iterator it = listListeners.iterator();
        while (it.hasNext()) {
            ListDataListener l = (ListDataListener) it.next();
            l.contentsChanged(lde);
        }
    }
    
    public int getSize() {
        return addrArray.size();
    }

    public Object getElementAt(int index) {
        return addrArray.get(index);
    }

    public void addListDataListener(ListDataListener l) {
        listListeners.add(l);
    }

    public void removeListDataListener(ListDataListener l) {
        listListeners.remove(l);
    }
}