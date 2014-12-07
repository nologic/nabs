/*
 * ModulesPanel.java
 *
 * Created on January 1, 2006, 11:41 PM
 *
 */

package eunomia.gui.realtime.receptorAdmin;

import eunomia.core.managers.listeners.ModuleManagerListener;
import eunomia.core.receptor.*;
import eunomia.gui.realtime.receptorAdmin.module.ModuleGeneralDetails;
import eunomia.gui.realtime.receptorAdmin.module.ModuleInstanceDetails;
import eunomia.messages.receptor.*;
import eunomia.plugin.GUIPlugin;
import eunomia.plugin.interfaces.GUIModule;
import eunomia.plugin.interfaces.GUIPluginListener;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

/**
 *
 * @author Mikhail Sosonkin
 */
public class ModulesPanel extends JPanel implements ActionListener, ModuleManagerListener, TreeSelectionListener, GUIPluginListener {
    private JButton instantiate;
    private JButton terminate;
    private JTree modList;
    private Receptor receptor;
    private HashMap nameToHandles;
    private ModuleRoot root;
    private GUIPlugin currentSelection;
    
    private JPanel modDetailPanel;
    private ModuleInstanceDetails modInstanceDetails;
    private ModuleGeneralDetails modGeneralDetails;
    
    public ModulesPanel(Receptor rec) {
        receptor = rec;
        root = new ModuleRoot();
        nameToHandles = new HashMap();
        addControls();
        
        modInstanceDetails = new ModuleInstanceDetails(receptor);
        modGeneralDetails = new ModuleGeneralDetails(receptor);
        
        receptor.getManager().addModuleManagerListener(this);
    }
    
    public void actionPerformed(ActionEvent e){
        Object o = e.getSource();
        
        if(o == instantiate){
            TreePath path = modList.getSelectionPath();
            if(path != null){
                receptor.getManager().createModInstance(path.getPathComponent(1).toString());
            }
        } else if(o == terminate){
            TreePath path = modList.getSelectionPath();
            if(path != null && path.getPathCount() == 3){
                DefaultMutableTreeNode node = (DefaultMutableTreeNode)path.getPathComponent(2);
                ModuleHandle handle = (ModuleHandle)node.getUserObject();
                receptor.getManager().terminateModInstance(handle);
            }
        }
    }
    
    public void moduleListChanged() {
        update();
    }
    
    public void moduleAdded(ModuleHandle handle) {
        update();
    }

    public void valueChanged(TreeSelectionEvent e) {
        Object o = e.getPath().getLastPathComponent();
        
        modInstanceDetails.setGUIPlugin(null);
                
        if(o instanceof DefaultMutableTreeNode){
            setCurrentView(modInstanceDetails);
            ModuleHandle handle = (ModuleHandle) ((DefaultMutableTreeNode)o).getUserObject();
            currentSelection = (GUIPlugin)receptor.getManager().getModuleByHandle(handle);
            if(currentSelection != null) {
                currentSelection.addGUIPluginListener(this);
                receptor.getOutComm().getModuleListeningList(handle);
            }
        } else {
            setCurrentView(modGeneralDetails);
            currentSelection = null;
        }
    }
    
    private void setCurrentView(JPanel comp){
        modDetailPanel.removeAll();
        modDetailPanel.add(comp);
        modDetailPanel.revalidate();
        modDetailPanel.repaint();
    }

    public void moduleRemoved(ModuleHandle handle, GUIModule module) {
        root.removeInstance(handle.getModuleName(), handle);
        ((DefaultTreeModel)modList.getModel()).reload();
    }
    
    public void streamListUpdated(GUIModule mod) {
        if(currentSelection == mod){
            modInstanceDetails.setGUIPlugin(currentSelection);
        }
    }

    public void update(){
        Iterator it = receptor.getState().getModules().iterator();
        while(it.hasNext()){
            String o = it.next().toString();
            if(!o.toString().equals("streamStatus")){
                root.addChild(new ModuleNode(o));
            }
        }
        
        it = receptor.getManager().getHandles();
        while(it.hasNext()){
            ModuleHandle handle = (ModuleHandle)it.next();
            root.addModInstance(handle.getModuleName(), handle);
        }
        
        root.updateDone();
        ((DefaultTreeModel)modList.getModel()).reload();
        
        modInstanceDetails.update();
    }
    
    private void addControls(){
        setLayout(new BorderLayout());
        JPanel modListingpanel = new JPanel(new BorderLayout());
        modDetailPanel = new JPanel(new BorderLayout());
        JPanel buttonsPanel = new JPanel(new GridLayout(2, 1));
        
        JSplitPane sPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        
        buttonsPanel.add(instantiate = new JButton("Create Instance"));
        buttonsPanel.add(terminate = new JButton("Close Instance"));

        modListingpanel.add(new JLabel("Module Listing"), BorderLayout.NORTH);
        modListingpanel.add(new JScrollPane(modList = new JTree(root)));
        modListingpanel.add(buttonsPanel, BorderLayout.SOUTH);
        
        sPane.setLeftComponent(modListingpanel);
        sPane.setRightComponent(modDetailPanel);
        
        add(sPane);
        
        sPane.setContinuousLayout(true);
        instantiate.addActionListener(this);
        terminate.addActionListener(this);
        modList.setRootVisible(true);
        modList.addTreeSelectionListener(this);
    }

    public void statusUpdated(GUIModule mod) {
    }

    public void controlUpdated(GUIModule mod) {
    }

    public void controlObtained(GUIModule mod) {
    }

    private class ModuleRoot implements TreeNode {
        private Vector nodes = new Vector();
        
        public TreeNode getChildAt(int i) {
            return (TreeNode)nodes.get(i);
        }

        public int getChildCount() {
            return nodes.size();
        }

        public TreeNode getParent() {
            return null;
        }

        public int getIndex(TreeNode node) {
            return nodes.indexOf(node);
        }

        public boolean getAllowsChildren() {
            return true;
        }

        public boolean isLeaf() {
            return false;
        }

        public Enumeration children() {
            return nodes.elements();
        }
        
        public void addChild(TreeNode child){
            String name = child.toString();
            
            for(int i = 0; i < nodes.size(); i++){
                if(nodes.get(i).toString().equals(name)){
                    return;
                }
            }
            
            nodes.add(child);
        }
        
        public void updateDone(){
            Collections.sort(nodes);
        }
        
        public void addModInstance(String name, ModuleHandle handle){
            for(int i = 0; i < nodes.size(); i++){
                ModuleNode node = ((ModuleNode)nodes.get(i));
                if(node.toString().equals(name)){
                    node.addModInstance(handle);
                }
            }
        }
        
        public void removeInstance(String name, ModuleHandle handle){
            for(int i = 0; i < nodes.size(); i++){
                ModuleNode node = ((ModuleNode)nodes.get(i));
                if(node.toString().equals(name)){
                    node.removeModInstance(handle);
                }
            }
        }
        
        public String toString(){
            return "Realtime Module";
        }
    }
    
    private class ModuleNode implements TreeNode, Comparable {
        private Vector list;
        private String name;
        
        public ModuleNode(String n){
            list = new Vector();
            name = n;
        }
        
        public void addModInstance(ModuleHandle handle){
            Iterator it = list.iterator();
            while (it.hasNext()) {
                ModuleHandle hand = (ModuleHandle) ((DefaultMutableTreeNode)it.next()).getUserObject();
                if(hand.getInstanceID() == handle.getInstanceID()){
                    return;
                }
            }
            
            list.add(new DefaultMutableTreeNode(handle));
        }
        
        public void removeModInstance(ModuleHandle handle){
            Iterator it = list.iterator();
            while (it.hasNext()) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode)it.next();
                ModuleHandle hand = (ModuleHandle) node.getUserObject();
                if(hand.getInstanceID() == handle.getInstanceID()){
                    list.remove(node);
                    return;
                }
            }
        }
        
        public TreeNode getChildAt(int childIndex) {
            return (TreeNode)list.get(childIndex);
        }
        
        public int getChildCount() {
            return list.size();
        }
        
        public TreeNode getParent() {
            return root;
        }
        
        public int getIndex(TreeNode node) {
            return list.indexOf(node);
        }
        
        public boolean getAllowsChildren() {
            return true;
        }
        
        public boolean isLeaf() {
            return false;
        }
        
        public Enumeration children() {
            Collections.sort(list);
            return list.elements();
        }
        
        public String toString(){
            return name;
        }

        public int compareTo(Object o) {
            return name.compareTo(o.toString());
        }
    }
}