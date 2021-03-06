/*
 * StreamServerManager.java
 *
 * Created on June 15, 2005, 5:10 PM
 */

package eunomia.gui.settings;

import eunomia.*;
import eunomia.gui.*;
import eunomia.core.managers.*;
import eunomia.core.managers.listeners.*;
import eunomia.core.receptor.Receptor;
import eunomia.gui.desktop.NabInternalFrame;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import org.apache.log4j.Logger;

/**
 *
 * @author  Mikhail Sosonkin
 */
public class ReceptorServerManager extends JPanel implements Exiter, 
                        ActionListener, ListSelectionListener, ReceptorManagerListener {
    private JSplitPane split;
    private JList list;
    private ReceptorConfigurationPanel confPan;
    private JButton add, remove;
    
    private static Logger logger;
    
    static {
        logger = Logger.getLogger(ReceptorServerManager.class);
    }
    
    public ReceptorServerManager() {
        addControls();
        
        ReceptorManager.ins.addReceptorManagerListener(this);
        
        updateList();
        if(list.getModel().getSize() != 0){
            list.setSelectedIndex(0);
        }
    }
    
    private void updateList(){
        list.setListData(ReceptorManager.ins.getReceptors().toArray());
    }
    
    private void addReceptor(){
        String name = JOptionPane.showInputDialog(this, "Enter the name for the receptor");
        if(name != null){
            try {
                ReceptorManager.ins.addDefaultReceptor(name);
                saveReceptors();
            } catch(Exception e){
                e.printStackTrace();
            }
        }
    }
    
    private void removeReceptor(){
        Receptor rec = (Receptor)list.getSelectedValue();
        if(rec != null && JOptionPane.showConfirmDialog(this, "The receptor \""+ rec + "\" will be forever remove?") == JOptionPane.YES_OPTION){
            try {
                ReceptorManager.ins.removeReceptor(rec);
                saveReceptors();
            } catch(Exception e){
                e.printStackTrace();
                logger.error("Error while removing: " + rec);
            }
        }
    }

    public void valueChanged(ListSelectionEvent e) {
        if(!e.getValueIsAdjusting()){
            Object sel = list.getSelectedValue();
            if(sel != null){
                confPan.setReceptor((Receptor)sel);
            }
        }
    }
    
    public void setShowReceptor(Receptor rec){
        confPan.setReceptor(rec);
        list.setSelectedValue(rec, true);
    }
    
    public void receptorAdded(Receptor rec) {
        updateList();
        list.setSelectedValue(rec, true);
        //seems redundant but the event doesn't work for some reason.
        confPan.setReceptor(rec);
    }
    
    public void receptorRemoved(Receptor sds) {
        updateList();
    }
    
    public void actionPerformed(ActionEvent e) {
        Object o = e.getSource();
        if(o == add){
            addReceptor();
        } else if(o == remove){
            removeReceptor();
        }
    }

    private void saveReceptors(){
        try {
            ReceptorManager.ins.save();
        } catch(Exception e){
            e.printStackTrace();
        }
    }
    
    public void startExitSequence(){
        saveReceptors();
    }
    
    private boolean firstPaint = false;
    public void paint(Graphics g){
        super.paint(g);
        if(!firstPaint){
            split.setDividerLocation(0.4);
            firstPaint = true;
        }
    }

    private void addControls(){
        JPanel mainPanel = new JPanel(new BorderLayout());
        JPanel buttonsPanel = new JPanel(new GridLayout(1, 2));
        split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        
        setLayout(new BorderLayout());
        
        buttonsPanel.add(add = new JButton("Add"));
        buttonsPanel.add(remove = new JButton("Remove"));
        mainPanel.add(new JLabel("Receptors"), BorderLayout.NORTH);
        mainPanel.add(new JScrollPane(list = new JList()));
        mainPanel.add(buttonsPanel, BorderLayout.SOUTH);
        split.setLeftComponent(mainPanel);
        split.setRightComponent(confPan = new ReceptorConfigurationPanel());
        add(split);
        
        split.setDividerSize(4);
        split.setContinuousLayout(true);
        
        add.addActionListener(this);
        remove.addActionListener(this);
        
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.addListSelectionListener(this);
    }
}