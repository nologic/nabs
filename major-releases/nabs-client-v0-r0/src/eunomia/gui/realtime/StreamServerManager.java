/*
 * StreamServerManager.java
 *
 * Created on June 15, 2005, 5:10 PM
 */

package eunomia.gui.realtime;

import eunomia.*;
import eunomia.gui.*;
import eunomia.core.data.streamData.*;
import eunomia.core.managers.*;
import eunomia.core.managers.listeners.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

/**
 *
 * @author  Mikhail Sosonkin
 */
public class StreamServerManager extends JInternalFrame implements Exiter, InternalFrameListener, 
                        ActionListener, ListSelectionListener, StreamManagerListener {
    private JSplitPane split;
    private JList list;
    private StreamConfigurationPanel confPan;
    private JButton add, remove;
    
    public StreamServerManager() {
        super("Receptor Manager");
        
        setSize(500, 400);
        setDefaultCloseOperation(JInternalFrame.HIDE_ON_CLOSE);
        setMaximizable(true);
        setResizable(true);
        setClosable(true);
        addInternalFrameListener(this);
        addControls();
        
        StreamManager.ins.addStreamManagerListener(this);
        
        updateList();
        if(list.getModel().getSize() != 0){
            list.setSelectedIndex(0);
        }
    }
    
    private void updateList(){
        list.setListData(StreamManager.ins.getStreamList().toArray());
    }
    
    private void addStream(){
        String name = JOptionPane.showInputDialog(this, "Enter the name for the receptor");
        if(name != null){
            try {
                StreamManager.ins.createDefaultStream(name);
                saveStreams();
            } catch(Exception e){
                e.printStackTrace();
            }
        }
    }
    
    private void removeStream(){
        StreamDataSource sds = (StreamDataSource)list.getSelectedValue();
        if(sds != null && JOptionPane.showConfirmDialog(this, "The receptor \""+ sds + "\" will be forever remove?") == JOptionPane.YES_OPTION){
            StreamManager.ins.removeStream(sds);
        }
    }

    public void valueChanged(ListSelectionEvent e) {
        if(!e.getValueIsAdjusting()){
            Object sel = list.getSelectedValue();
            if(sel != null){
                confPan.setStreamSource((StreamDataSource)sel);
            }
        }
    }
    
    public void streamAdded(StreamDataSource sds) {
        updateList();
        list.setSelectedValue(sds, true);
        //seems redundant but the event doesn't work for some reason.
        confPan.setStreamSource(sds);
    }
    
    public void streamRemoved(StreamDataSource sds) {
        updateList();
    }
    
    public void actionPerformed(ActionEvent e) {
        Object o = e.getSource();
        if(o == add){
            addStream();
        } else if(o == remove){
            removeStream();
        }
    }

    private void saveStreams(){
        try {
            StreamManager.ins.saveStreams();
        } catch(Exception e){
            e.printStackTrace();
        }
    }
    
    public void startExitSequence(){
        saveStreams();
    }

    private void addControls(){
        JPanel mainPanel = new JPanel(new BorderLayout());
        JPanel buttonsPanel = new JPanel(new GridLayout(1, 2));
        Container c = getContentPane();
        split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        
        c.setLayout(new BorderLayout());
        
        buttonsPanel.add(add = new JButton("Add"));
        buttonsPanel.add(remove = new JButton("Remove"));
        mainPanel.add(new JLabel("Receptors"), BorderLayout.NORTH);
        mainPanel.add(new JScrollPane(list = new JList()));
        mainPanel.add(buttonsPanel, BorderLayout.SOUTH);
        split.setLeftComponent(mainPanel);
        split.setRightComponent(confPan = new StreamConfigurationPanel());
        c.add(split);
        
        split.setDividerSize(4);
        split.setContinuousLayout(true);
        
        add.addActionListener(this);
        remove.addActionListener(this);
        remove.addMouseListener(MyMouseListener.ins);
        add.addMouseListener(MyMouseListener.ins);
        
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.addListSelectionListener(this);
    }
    
    public void internalFrameActivated(InternalFrameEvent e) {
    }
    
    public void internalFrameClosed(InternalFrameEvent e) {
    }
    
    public void internalFrameClosing(InternalFrameEvent e) {
    }
    
    public void internalFrameDeactivated(InternalFrameEvent e) {
    }
    
    public void internalFrameDeiconified(InternalFrameEvent e) {
    }
    
    public void internalFrameIconified(InternalFrameEvent e) {
    }
    
    public void internalFrameOpened(InternalFrameEvent e) {
        split.setDividerLocation(0.4);
    }
}