/*
 * ModulePort.java
 *
 * Created on July 29, 2005, 4:13 PM
 *
 */

package eunomia.gui.module;

import eunomia.core.managers.*;
import eunomia.gui.*;
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
public class ModulePortal extends JInternalFrame implements ActionListener, WindowListener,
            InternalFrameListener {
    private static final Border toolBarBorder;
    
    private String name;
    private Module instance;
    private JPanel holder;
    private JPanel mainPanel;
    private JDialog controlDialog;
    private JFrame fsFrame;
    
    private JButton fullScreen;
    private JButton control;
    private JButton openFilter;
    private JButton stop;
    private JButton start;
    private JButton reset;
    
    static {
        toolBarBorder = BorderFactory.createEtchedBorder();
    }
    
    public ModulePortal(String modName) {
        name = modName;
        setSize(400, 300);
        setClosable(true);
        setMaximizable(true);
        setResizable(true);
        addInternalFrameListener(this);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        addControls();
    }
    
    public Module getModule(){
        return instance;
    }
    
    public void instantiate() throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        if(instance == null){
            Class klass = Class.forName(name);
            instance = (Module)klass.newInstance();

            holder.removeAll();
            holder.add(instance.getJComponent());
            DataManager.ins.registerWithUpdater(instance.getRefreshNotifier());
            setTitle(instance.getTitle());
            setVisible(true);
        }
    }
    
    public void terminate(){
        if(controlDialog != null){
            controlDialog.dispose();
        }
        
        if(fsFrame != null){
            fsFrame.dispose();
        }
        
        if(instance != null){
            DataManager.ins.deregisterWithUpdater(instance.getRefreshNotifier());
        }
    }
    
    public void showFullScreen(){
        if(instance != null){
            if(fsFrame == null){
                fsFrame = new JFrame();

                fsFrame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
                fsFrame.setSize(400, 400);
                fsFrame.addWindowListener(this);
                fsFrame.getContentPane().setLayout(new BorderLayout());
                fsFrame.setLocationRelativeTo(this);
            }
            
            fsFrame.setTitle(instance.getTitle());
            fsFrame.setContentPane(mainPanel);
            fsFrame.setVisible(true);
            setVisible(false);
            repaint();
        }
    }
    
    public void showControl(){
        if(instance != null){
            // Damn swing won't allow to change the owner.
            Frame owner = null;
            try {
                owner = JOptionPane.getFrameForComponent(mainPanel);
            } catch(Exception e){
            }

            controlDialog = new JDialog(owner, true);

            controlDialog.setTitle("Control Panel: " + instance.getTitle());
            controlDialog.setSize(500, 300);
            controlDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            controlDialog.setContentPane(instance.getControlComponent());
            controlDialog.setLocationRelativeTo(mainPanel);
            controlDialog.setVisible(true);
        }
    }
    
    public void openFilterEditor(){
        if(instance != null){
            Frame owner = null;
            try {
                owner = JOptionPane.getFrameForComponent(mainPanel);
                FilterEditor.editFilter(owner, instance.getFilter());
            } catch(Exception e){
                e.printStackTrace();
                FilterEditor.editFilter(instance.getFilter());
            }
        }
    }
    
    public void actionPerformed(ActionEvent e){
        Object o = e.getSource();
        
        if(instance != null){
            if(o == fullScreen){
                showFullScreen();
            } else if(o == control){
                showControl();
            } else if(o == stop){
                instance.stop();
            } else if(o == start){
                instance.start();
            } else if(o == reset){
                instance.reset();
            } else if(o == openFilter){
                openFilterEditor();
            }
        }
    }
    
    public void windowIconified(WindowEvent e) {
    }
    
    public void windowOpened(WindowEvent e) {
    }
    
    public void windowDeiconified(WindowEvent e) {
    }
    
    public void windowDeactivated(WindowEvent e) {
    }
    
    public void windowClosing(WindowEvent e) {
        add(mainPanel);
        setVisible(true);
        repaint();
    }
    
    public void windowClosed(WindowEvent e) {
    }
    
    public void windowActivated(WindowEvent e) {
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
        
        toolBar.add(control = makeButton("CTRL", "Control Panel Dialog"));
        toolBar.add(openFilter = makeButton("Filter", "Filter Setup"));
        toolBar.addSeparator();
        toolBar.add(start = makeButton("Start", "Continues the processing of flows"));
        toolBar.add(stop = makeButton("Stop", "Pauses processing"));
        toolBar.add(reset = makeButton("Reset", "Reset data"));
        toolBar.addSeparator();
        toolBar.add(fullScreen = makeButton("FS", "Full Screen Mode"));
        
        mainPanel.add(toolBar, BorderLayout.NORTH);
        mainPanel.add(holder);
        c.add(mainPanel);
        
        control.addActionListener(this);
        openFilter.addActionListener(this);
        fullScreen.addActionListener(this);
        start.addActionListener(this);
        stop.addActionListener(this);
        reset.addActionListener(this);
    }
    
    public JButton makeButton(String text, String tp){
        JButton button = new JButton(text);
        
        Font font = button.getFont();
        button.setToolTipText(tp);
        button.setFont(new Font(font.getName(), Font.BOLD, 9));
        
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
}