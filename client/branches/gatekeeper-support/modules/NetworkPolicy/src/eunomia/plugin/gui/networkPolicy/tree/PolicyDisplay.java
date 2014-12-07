/*
 * PolicyTree.java
 *
 * Created on June 19, 2007, 10:51 PM
 *
 */

package eunomia.plugin.gui.networkPolicy.tree;

import eunomia.plugin.com.networkPolicy.PolicyItem;
import eunomia.plugin.utils.networkPolicy.PolicyLanguage;
import eunomia.receptor.module.NABFlow.NABFilterEntry;
import eunomia.receptor.module.NABFlow.NABFlow;
import eunomia.util.Util;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.border.Border;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 *
 * @author Mikhail Sosonkin
 */
public class PolicyDisplay extends JList implements ListSelectionListener, ListCellRenderer, MouseListener {
    public static final Color[] TYPE_COLOR = new Color[] {
        new Color(0x1FDC143C, true), new Color(0x1FB22222, true), new Color(0x1FFF8C00, true), new Color(0x1F008000, true), new Color(0x1F778899, true)
    };
    
    private ImageIcon plus_sign;
    private ImageIcon minus_sign;
    private ImageIcon blank_sign;
    
    private List listeners;
    private JLabel renderingLabel;
    private PolicyListModel listModel;
    
    private Font policyFont;
    private Font headerFont;
    private Border policyBorder;
    private Border headerBorder;
    
    public PolicyDisplay() {
        super();

        ClassLoader cloader = getClass().getClassLoader();
        blank_sign = new ImageIcon(cloader.getResource("eunomia/plugin/icons/list-blank.png"));
        plus_sign = new ImageIcon(cloader.getResource("eunomia/plugin/icons/list-add.png"));
        minus_sign = new ImageIcon(cloader.getResource("eunomia/plugin/icons/list-remove.png"));
        
        listeners = new LinkedList();
        renderingLabel = new JLabel();
        
        setCellRenderer(this);
        addListSelectionListener(this);
        setModel(listModel = new PolicyListModel());
        addMouseListener(this);
        
        policyFont = new Font("SansSerif", Font.PLAIN, 12);
        policyBorder = BorderFactory.createLineBorder(Color.LIGHT_GRAY);
        headerFont = new Font("SansSerif", Font.PLAIN, 12);
        headerBorder = BorderFactory.createEmptyBorder();
        
        renderingLabel.setOpaque(true);
        renderingLabel.setIconTextGap(20);
        
        for (int i = -1; i < PolicyItem.NUM_TYPES; i++) {
            insertPolicyItem(new SpecialPolicyItem(i));
        }
    }
    
    public void addPolicySelectionListener(PolicySelectionListener l) {
        listeners.add(l);
    }
    
    public void removePolicySelectionListener(PolicySelectionListener l) {
        listeners.remove(l);
    }
    
    private void firePolicySelection(int type, PolicyItem pi) {
        Iterator it = listeners.iterator();
        while (it.hasNext()) {
            PolicySelectionListener l = (PolicySelectionListener) it.next();
            if(pi != null) {
                l.policyItemSelection(pi);
            } else {
                l.policyTypeSelection(type);
            }
        }
    }
    
    public PolicyItem getSelectedPolicy() {
        return (PolicyItem)getSelectedValue();
    }
    
    public void insertPolicyItem(PolicyItem p){
        listModel.addPolicy(p);
    }
    
    public void removePolicyItem(PolicyItem p){
        listModel.removePolicy(p);
    }

    public void valueChanged(ListSelectionEvent e) {
        if(!e.getValueIsAdjusting()) {
            Object o = getSelectedValue();

            if(o instanceof SpecialPolicyItem) {
                SpecialPolicyItem spi = (SpecialPolicyItem)o;
                int type = spi.getPolicyType();
                
                if(type == -1) {
                    firePolicySelection(PolicyItem.NUM_TYPES, null);
                } else {
                    firePolicySelection(type, null);
                }
            } else if(o instanceof PolicyItem){
                PolicyItem pi = (PolicyItem)o;
                firePolicySelection(0, pi);
            }
        }
    }

    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        PolicyItem item = (PolicyItem)value;
        int type = item.getPolicyType();
        if(type == -1) {
            type = 0;
        }
        
        renderingLabel.setHorizontalAlignment(JLabel.LEFT);
        renderingLabel.setComponentOrientation(list.getComponentOrientation());
        renderingLabel.setBackground( isSelected?list.getSelectionBackground():TYPE_COLOR[type] );
        renderingLabel.setForeground( isSelected?list.getSelectionForeground():list.getForeground() );
        renderingLabel.setEnabled(list.isEnabled());
        renderingLabel.setIcon(null);
        
        if(item instanceof SpecialPolicyItem) {
            renderingLabel.setFont(headerFont);
            renderingLabel.setBorder(headerBorder);
            
            if(item.getPolicyType() == -1) {
                renderingLabel.setIcon(blank_sign);
                renderingLabel.setText("<html><body><center><u><b>All policies</b></u><br>New Alerts: " + 
                        listModel.getAlertCount(-1, true) + "/" + listModel.getAlertCount(-1, false) + "</center></body></html>");
            } else {
                SpecialPolicyItem spi = (SpecialPolicyItem)item;
                
                renderingLabel.setIcon( (spi.isAllShowing()?minus_sign:plus_sign) );
                renderingLabel.setText("<html><body><center><u><b>" + PolicyLanguage.TYPE_NAME[type] + " policies (" + listModel.getTypeCount(item.getPolicyType()) + ")</b></u><br>New Alerts: " + 
                        listModel.getAlertCount(type, true) + "/" + listModel.getAlertCount(type, false) + "</center></body></html>");
            }
        } else {
            renderingLabel.setText(PolicyLanguage.makePolicySummaryHTML(item));
            renderingLabel.setFont(policyFont);
            renderingLabel.setBorder(policyBorder);
        }
        
        renderingLabel.setToolTipText(item.getDescription());
        
        return renderingLabel;
    }
    


    public void mouseClicked(MouseEvent e) {
        if(e.getClickCount() == 2) {
            PolicyItem item = getSelectedPolicy();
            
            if(item instanceof SpecialPolicyItem) {
                SpecialPolicyItem spi = (SpecialPolicyItem)item;
                
                if(spi.isAllShowing()) {
                    spi.setAllShowing(false);
                    listModel.unshow(item.getPolicyType());
                } else {
                    spi.setAllShowing(true);
                    listModel.reshow(item.getPolicyType());
                }
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
    
    private class PolicyListModel implements ListModel {
        private List listeners;
        private List source;
        private List display;
        private int[] typeCounts;
        
        public PolicyListModel() {
            typeCounts = new int[PolicyLanguage.TYPE_NAME.length];
            source = new ArrayList();
            display = new ArrayList();
            listeners = new LinkedList();
        }
        
        public int getTypeCount(int type) {
            return typeCounts[type];
        }
        
        public void addPolicy(PolicyItem item) {
            source.add(item);

            if( !(item instanceof SpecialPolicyItem) ){
                ++typeCounts[item.getPolicyType()];
            }
            
            int insert = findInsertIndex(item.getPolicyType());
            
            if(insert > 0) {
                PolicyItem prev = (PolicyItem)display.get(insert - 1);
                if(prev instanceof SpecialPolicyItem) {
                    SpecialPolicyItem sItem = (SpecialPolicyItem)prev;
                    if(!sItem.isAllShowing()) {
                        return;
                    }
                }
            }
            
            display.add(insert, item);
            
            fireUpdateEvent();
        }
        
        public void removePolicy(PolicyItem item) {
            display.remove(item);
            source.remove(item);
            
            if( !(item instanceof SpecialPolicyItem) ){
                --typeCounts[item.getPolicyType()];
            }
        }
        
        private int findInsertIndex(int type) {
            int size = display.size();
            for (int i = 0; i < size; i++) {
                PolicyItem check = (PolicyItem)display.get(i);
                if(check.getPolicyType() > type) {
                    return i;
                }
            }

            return size;
        }
        
        public void unshow(int type) {
            Iterator it = display.iterator();
            while (it.hasNext()) {
                PolicyItem item = (PolicyItem)it.next();
                
                if(!(item instanceof SpecialPolicyItem) && item.getPolicyType() == type) {
                    it.remove();
                }
            }
            
            fireUpdateEvent();
        }
        
        public void reshow(int type) {
            Iterator it = source.iterator();
            while (it.hasNext()) {
                PolicyItem item = (PolicyItem)it.next();
                
                if(!(item instanceof SpecialPolicyItem) && item.getPolicyType() == type) {
                    int insert = findInsertIndex(item.getPolicyType());
                    display.add(insert, item);
                }
            }
            
            fireUpdateEvent();
        }
        
        public void fireUpdateEvent() {
            ListDataEvent event = new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, 0, 0);
            Iterator it = listeners.iterator();
            while (it.hasNext()) {
                ListDataListener l = (ListDataListener) it.next();
                l.contentsChanged(event);
            }
        }
        
        public Object getElementAt(int index) {
            return display.get(index);
        }

        public void addListDataListener(ListDataListener l) {
            listeners.add(l);
        }

        public void removeListDataListener(ListDataListener l) {
            listeners.remove(l);
        }

        public int getSize() {
            return display.size();
        }
            
        public int getAlertCount(int type, boolean isNew) {
            int total = 0;
            Iterator it = source.iterator();
            while (it.hasNext()) {
                PolicyItem pi = (PolicyItem) it.next();
                if(type == -1 || pi.getPolicyType() == type) {
                    total += (isNew?pi.getNewAlertCount():pi.getAlertCount());
                }
            }

            return total;
        }
    }
    
    private class SpecialPolicyItem extends PolicyItem {
        private boolean isAllShowing;
        
        public SpecialPolicyItem(int type) {
            isAllShowing = true;
            if(type == -1) {
                setDescription("All Policies");
            } else {
                setDescription(PolicyLanguage.TYPE_NAME[type]);
            }
            setPolicyType(type);
        }

        public boolean isAllShowing() {
            return isAllShowing;
        }

        public void setAllShowing(boolean isAllShowing) {
            this.isAllShowing = isAllShowing;
        }
    }
}
