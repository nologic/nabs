/*
 * ModulePort.java
 *
 * Created on July 29, 2005, 4:13 PM
 *
 */

package eunomia.gui.module;
import eunomia.core.managers.ModuleManager;
import eunomia.gui.*;
import eunomia.gui.desktop.NabInternalFrame;
import eunomia.gui.filter.FilterEditor;
import eunomia.plugin.*;
import eunomia.plugin.interfaces.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

/**
 *
 * @author Mikhail Sosonkin
 */
public class ModulePortal extends NabInternalFrame implements ActionListener,
        InternalFrameListener, GUIPluginListener {
    private static final Border toolBarBorder;
    private static final Dimension seperatorDimension;
    
    private String name;
    private GUIPlugin instance;
    private JPanel holder;
    private JPanel mainPanel;
    private JDialog controlDialog;
    
    private JButton control;
    private JButton openFilter;
    private JButton stop;
    private JButton start;
    private JButton reset;
    private JButton commit;
    private JButton refresh;
    private JButton detach;
    
    private boolean isClosing;
    
    static {
        seperatorDimension = new Dimension(20, 20);
        toolBarBorder = BorderFactory.createEtchedBorder();
    }
    
    public ModulePortal(GUIPlugin inst) {
        instance = inst;
        
        isClosing = false;
        
        setSize(600, 500);
        setTitle("[" + inst.getReceptor().getManager().getModuleHandle(inst).getInstanceID() + "] - " + instance.getTitle());
        setMaximizable(true);
        setResizable(true);
        setIconifiable(true);
        addInternalFrameListener(this);
        setDefaultCloseOperation(JInternalFrame.DO_NOTHING_ON_CLOSE);
        addControls();
        setFrameIcon(IconResource.getFlowModuleWindow());
        
        holder.removeAll();
        holder.add(instance.getJComponent());
        
        instance.addGUIPluginListener(this);
    }
    
    public GUIModule getModule(){
        return instance;
    }
    
    public void terminate(){
        if(!isClosing){
            int ans = JOptionPane.showConfirmDialog(this, 
                    "Closing this module portal will terminate the instance of the server. Are you sure you want to proceed?",
                    "Should I proceed?", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

            if(ans == JOptionPane.YES_OPTION){
                isClosing = true;
                setTitle("CLOSING - " + getTitle());

                getContentPane().setEnabled(false);
                removeInternalFrameListener(this);

                if(controlDialog != null){
                    controlDialog.dispose();
                }

                instance.removeGUIPluginListener(this);

                ModuleManager manager = instance.getReceptor().getManager();
                manager.terminateModInstance(instance.getModule());
            } 
        }
    }
    
    public void statusUpdated(GUIModule mod) {
        setTitle("[" + instance.getModuleHandle().getInstanceID() + "] - " + instance.getTitle());
    }
    
    public void controlUpdated(GUIModule mod){
    }
    
    public void controlObtained(GUIModule mod){
    }
    
    public void showControl(){
        if(instance != null){
            // Damn swing won't allow to change the owner.
            // so we have to recreate eachtime.
            Frame owner = null;
            Container c = null;
            JPanel buttonPanel = new JPanel(new BorderLayout());
            JPanel subButtonPanel = new JPanel(new GridLayout(1, 2, 5, 5));
            try {
                owner = JOptionPane.getFrameForComponent(mainPanel);
            } catch(Exception e){
            }
            
            controlDialog = new JDialog(owner, true);
            c = controlDialog.getContentPane();
            c.setLayout(new BorderLayout());
            
            controlDialog.setTitle("Control Panel: " + instance.getTitle());
            controlDialog.setSize(500, 300);
            controlDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            controlDialog.setLocationRelativeTo(mainPanel);
            
            buttonPanel.add(subButtonPanel, BorderLayout.EAST);
            subButtonPanel.add(refresh = new JButton("Refresh"));
            subButtonPanel.add(commit = new JButton("Apply"));
            c.add(new JScrollPane(instance.getControlComponent()));
            c.add(buttonPanel, BorderLayout.SOUTH);
            
            commit.addActionListener(this);
            refresh.addActionListener(this);
            
            controlDialog.setVisible(true);
        }
    }
    
    public void openFilterEditor(){
        if(instance != null){
            Frame owner = null;
            try {
                owner = JOptionPane.getFrameForComponent(mainPanel);
                FilterEditor.editFilter(owner, instance);
            } catch(Exception e){
                e.printStackTrace();
                FilterEditor.editFilter(instance);
            }
        }
    }
    
    public void commitControlData(){
        if(instance != null){
            try {
                instance.getReceptor().getManager().sendControlData(instance);
                controlDialog.dispose();
            } catch(Exception e){
                e.printStackTrace();
            }
        }
    }
    
    public void refreshControlData(){
        if(instance != null){
            try {
                instance.getReceptor().getManager().getControlData(instance);
            } catch(Exception e){
                e.printStackTrace();
            }
        }
    }
    
    public void actionPerformed(ActionEvent e){
        Object o = e.getSource();
        
        if(instance != null){
            try {
                if(o == control){
                    showControl();
                } else if(o == stop){
                    instance.getReceptor().getManager().stopModule(instance);
                } else if(o == start){
                    instance.getReceptor().getManager().startModule(instance);
                } else if(o == reset){
                    instance.getReceptor().getManager().resetModule(instance);
                } else if(o == openFilter){
                    openFilterEditor();
                } else if(o == commit){
                    commitControlData();
                } else if(o == refresh){
                    refreshControlData();
                } else if(o == detach){
                    detach();
                }
            } catch(Exception ex){
                ex.printStackTrace();
            }
        }
    }
    
    private void addControls(){
        JToolBar toolBar = new JToolBar();
        holder = new JPanel(new BorderLayout());
        mainPanel = new JPanel(new BorderLayout());
        
        Container c = getContentPane();
        
        c.setLayout(new BorderLayout());
        
        toolBar.setBorder(toolBarBorder);
        toolBar.setBorderPainted(true);
        toolBar.setFloatable(false);
        toolBar.setRollover(true);
        
        toolBar.add(control = makeButton("", "Open Control Panel Dialog to edit module settings", IconResource.getFlowModuleControl()));
        toolBar.add(openFilter = makeButton("", "Filter Setup", IconResource.getFlowModuleFilter()));
        toolBar.addSeparator(seperatorDimension);
        toolBar.add(start = makeButton("", "Continues the processing of flows", IconResource.getFlowModuleStart()));
        toolBar.add(stop = makeButton("", "Pauses processing", IconResource.getFlowModuleStop()));
        toolBar.add(reset = makeButton("", "Reset data", IconResource.getFlowModuleReset()));
        toolBar.addSeparator(seperatorDimension);
        toolBar.add(detach = makeButton("", "Opens the module in a seperate window", IconResource.getFlowModuleDetach()));
        
        mainPanel.add(toolBar, BorderLayout.NORTH);
        mainPanel.add(holder);
        c.add(mainPanel);
        
        control.addActionListener(this);
        openFilter.addActionListener(this);
        start.addActionListener(this);
        stop.addActionListener(this);
        reset.addActionListener(this);
        detach.addActionListener(this);
    }
    
    public JButton makeButton(String text, String tp, Icon icon){
        JButton button = new JButton(text);
        
        Font font = button.getFont();
        button.setToolTipText(tp);
        button.setFont(new Font(font.getName(), Font.BOLD, 9));
        button.setIcon(icon);
        
        return button;
    }
    
    public void internalFrameActivated(InternalFrameEvent e) {
    }
    
    public void internalFrameClosed(InternalFrameEvent e) {
    }
    
    public void internalFrameClosing(InternalFrameEvent e) {
        terminate();
    }
    
    public void internalFrameDeactivated(InternalFrameEvent e) {
    }
    
    public void internalFrameDeiconified(InternalFrameEvent e) {
    }
    
    public void internalFrameIconified(InternalFrameEvent e) {
    }
    
    public void internalFrameOpened(InternalFrameEvent e) {
    }

    public void streamListUpdated(GUIModule mod) {
    }
}