/*
 * PolicyEditorDialog.java
 *
 * Created on June 20, 2007, 10:32 PM
 *
 */

package eunomia.plugin.gui.networkPolicy;

import eunomia.flow.Filter;
import eunomia.plugin.com.networkPolicy.PolicyItem;
import eunomia.receptor.module.NABFlow.NABFilterEntry;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

/**
 *
 * @author Mikhail Sosonkin
 */
public class PolicyEditorDialog extends JDialog implements ActionListener {
    private JButton add, cancel;
    private PolicyEditor editor;
    private PolicyItem curItem;
    private List listeners;
    private boolean isCanceled;
    
    public PolicyEditorDialog(Frame owner) {
        super(owner);
        
        listeners = new LinkedList();
        
        setModal(true);
        setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
        setTitle("Policy Editor");
        setSize(500, 500);
        
        addControls();
    }
    
    public PolicyEditor getEditor() {
        return editor;
    }
    
    public void addPolicyListChangeListener(PolicyListChangeListener l) {
        listeners.add(l);
    }
    
    public void removePolicyListChangeListener(PolicyListChangeListener l) {
        listeners.remove(l);
    }
    
    private void firePolicyAdded(PolicyItem p) {
        Iterator it = listeners.iterator();
        while (it.hasNext()) {
            PolicyListChangeListener l = (PolicyListChangeListener) it.next();
            l.policyAdded(p);
        }
    }
    
    public void editPolicy(PolicyItem p, boolean allEdit) {
        if(p == null) {
            curItem = generateItem();
            allEdit = true;
        } else {
            curItem = p;
        }

        editor.setPolicyItem(curItem, allEdit, p == null);
        setVisible(true);
    }
    
    public void actionPerformed(ActionEvent e) {
        Object o = e.getSource();
        
        if(o == add) {
            if(editor.commitData()) {
                firePolicyAdded(curItem);
                curItem = null;
                isCanceled = false;
                setVisible(false);
            }
        } else if(o == cancel) {
            isCanceled = true;
            setVisible(false);
        }
    }
    
    public boolean isCanceled() {
        return isCanceled;
    }

    private void addControls() {
        JPanel buttomPanel = new JPanel(new BorderLayout());
        JPanel buttonsPanel = new JPanel(new GridLayout(1, 2, 5, 5));
        
        Container c = getContentPane();
        
        c.setLayout(new BorderLayout());
        
        buttonsPanel.add(add = new JButton("Save Policy"));
        buttonsPanel.add(cancel = new JButton("Cancel"));
        
        buttomPanel.add(buttonsPanel, BorderLayout.EAST);
        c.add(editor = new PolicyEditor());
        c.add(buttomPanel, BorderLayout.SOUTH);
        
        add.addActionListener(this);
        cancel.addActionListener(this);
    }
    
    private PolicyItem generateItem() {
        PolicyItem item = new PolicyItem();
        Filter filter = new Filter();
        filter.addFilterWhite(new NABFilterEntry(null));
        
        item.setPolicyID(-1);
        item.setPolicyType(PolicyItem.REAL_TIME);
        item.setRate(240*1024);
        item.setFilter(filter);
        
        return item;
    }
}
