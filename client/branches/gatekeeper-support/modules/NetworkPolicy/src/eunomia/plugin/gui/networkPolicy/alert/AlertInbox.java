/*
 * PolicyPane.java
 *
 * Created on January 3, 2007, 3:10 PM
 */

package eunomia.plugin.gui.networkPolicy.alert;

import eunomia.plugin.com.networkPolicy.AlertItem;
import eunomia.plugin.com.networkPolicy.PolicyItem;
import eunomia.plugin.gui.networkPolicy.*;
import eunomia.plugin.gui.networkPolicy.tree.PolicySelectionListener;
import eunomia.plugin.gui.networkPolicy.tree.PolicyDisplay;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;

/**
 *
 * @author  kulesh, Mikhail Sosonkin
 */
public class AlertInbox extends JPanel implements PolicySelectionListener, ActionListener {
    private JSplitPane policySplitpane;
    private PolicyDisplay policyTree;
    private AlertPanel alertPanel;
    private JButton addPolicy, removePolicy, editPolicy;
    private PolicyEditorDialog pEdit;
    private PolicyViewer viewer;
    private List listeners;
    private Main main;
    
    public AlertInbox(Main main) {
        this.main = main;
        listeners = new LinkedList();
        
        addControls();
    }
    
    public void addPolicyListChangeListener(PolicyListChangeListener l) {
        listeners.add(l);
        if(pEdit != null){
            pEdit.addPolicyListChangeListener(l);
        }
    }
    
    public void removePolicyListChangeListener(PolicyListChangeListener l) {
        listeners.remove(l);
        if(pEdit != null) {
            pEdit.removePolicyListChangeListener(l);
        }
    }
    
    private void firePolicyRemoved(PolicyItem pi) {
        Iterator it = listeners.iterator();
        while (it.hasNext()) {
            PolicyListChangeListener l = (PolicyListChangeListener) it.next();
            l.policyRemoved(pi);
        }
    }
    
    private boolean paintOne = true;
    public void paint(Graphics g) {
        super.paint(g);
        if(paintOne) {
            paintOne = false;
            policySplitpane.setDividerLocation(0.2);
        }
    }
    
    public void actionPerformed(ActionEvent e) {
        Object o = e.getSource();
        
        if(pEdit == null) {
            pEdit = new PolicyEditorDialog(JOptionPane.getFrameForComponent(this));
            pEdit.getEditor().setModuleManager(main.getReceptor().getManager());
            Iterator it = listeners.iterator();
            while (it.hasNext()) {
                PolicyListChangeListener l = (PolicyListChangeListener) it.next();
                pEdit.addPolicyListChangeListener(l);
            }

        }

        if(o == addPolicy) {
            pEdit.setLocationRelativeTo(this);
            pEdit.editPolicy(null, true);
        } else if(o == removePolicy) {
             PolicyItem sel = policyTree.getSelectedPolicy();
             String msg = "Remove Selected Policy (" + sel.getAlertCount() + " associates alerts will be deleted)?";
             if(sel != null && JOptionPane.showConfirmDialog(removePolicy, msg, "Confirm Remove", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                 policyTree.removePolicyItem(sel);
                 firePolicyRemoved(sel);
                 alertPanel.deleteAlertsForPolicy(sel);
             }
        } else if(o == editPolicy) {
            PolicyItem sel = policyTree.getSelectedPolicy();
            if(sel != null) {
                editPolicy(sel);
            }
        }
    }
    
    private void editPolicy(PolicyItem item){
        boolean doAllEdit = false;
        if(item.getAlertCount() > 0) {
            String question = "Do you want to modify policy rate and/or filter? (Pressing 'Yes' will delete " + item.getAlertCount() + " associated alerts)";
            int resp = JOptionPane.showConfirmDialog(editPolicy, question, "Editing Policy", JOptionPane.YES_NO_CANCEL_OPTION);
            doAllEdit = (resp == JOptionPane.YES_OPTION);
            
            if(resp == JOptionPane.CANCEL_OPTION) {
                return;
            }
        } else {
            doAllEdit = true;
        }
        
        item.setRemoveAlerts(doAllEdit);
        pEdit.setLocationRelativeTo(this);
        pEdit.editPolicy(item, doAllEdit);
        
        if(!pEdit.isCanceled() && doAllEdit && item.getAlertCount() > 0) {
            alertPanel.deleteAlertsForPolicy(item);
            item.resetAlertCount();
        }
    }
    
    public void policyItemSelection(PolicyItem item) {
        alertPanel.showPolicy(item);
        viewer.showPolicy(item);
    }
    
    public void policyTypeSelection(int type) {
        alertPanel.showType(type);
        viewer.showPolicy(null);
    }
    
    public AlertPanel getAlertPanel() {
        return alertPanel;
    }
    
    private void addControls() {
        policySplitpane = new JSplitPane();
        alertPanel = new AlertPanel(main);
        JPanel leftComponent = new JPanel(new BorderLayout());
        JPanel buttonsPanel = new JPanel(new GridLayout(1, 3, 2, 2));
        JPanel alertsPanel = new JPanel(new BorderLayout(10, 10));
        JPanel viewerPanel = new JPanel(new BorderLayout());
        JPanel treePanel = new JPanel(new BorderLayout());
        JPanel listView = new JPanel(new BorderLayout());
        setLayout(new BorderLayout());
        
        buttonsPanel.add(addPolicy = new JButton("New"));
        buttonsPanel.add(removePolicy = new JButton("Remove"));
        buttonsPanel.add(editPolicy = new JButton("Edit"));
        
        JScrollPane pane;
        treePanel.add(pane = new JScrollPane(policyTree = new PolicyDisplay()), BorderLayout.CENTER);

        listView.add(pane = new JScrollPane(alertPanel.getHostsList()));
        
        viewerPanel.add(viewer = new PolicyViewer(), BorderLayout.CENTER);

        leftComponent.add(treePanel);
        leftComponent.add(viewerPanel, BorderLayout.SOUTH);
        leftComponent.add(buttonsPanel, BorderLayout.NORTH);
        
        alertsPanel.add(listView, BorderLayout.NORTH);
        alertsPanel.add(alertPanel);
        
        policySplitpane.setLeftComponent(leftComponent);
        policySplitpane.setRightComponent(alertsPanel);
        
        add(policySplitpane, BorderLayout.CENTER);
        
        policyTree.addPolicySelectionListener(this);
        policySplitpane.setContinuousLayout(true);
        policySplitpane.setOneTouchExpandable(true);
        
        addPolicy.addActionListener(this);
        removePolicy.addActionListener(this);
        editPolicy.addActionListener(this);
        
        treePanel.setBorder(BorderFactory.createTitledBorder("Defined Policies"));
        listView.setBorder(BorderFactory.createTitledBorder("Violators"));
        
        treePanel.setPreferredSize(new Dimension(100, 0));
        listView.setPreferredSize(new Dimension(0, 130));
    }
    
    public void insertAlertItem(AlertItem ai, int type){
        alertPanel.insertAlertItem(ai, type);
    }
    
    public void removeAlertItem(AlertItem ai){
        alertPanel.removeAlertItem(ai);
    }
    
    public void insertPolicyItem(PolicyItem p){
        policyTree.insertPolicyItem(p);
    }
    
    public void removePolicyItem(PolicyItem p){
        policyTree.removePolicyItem(p);
    }
}