/*
 * ReceptorAdmin.java
 *
 * Created on December 12, 2005, 10:47 PM
 *
 */

package eunomia.gui.realtime;

import eunomia.core.managers.listeners.ReceptorStateListener;
import eunomia.core.receptor.*;
import eunomia.gui.archival.TerminalManager;
import eunomia.gui.desktop.NabInternalFrame;
import eunomia.gui.realtime.receptorAdmin.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;


/**
 *
 * @author Mikhail Sosonkin
 */
public class ReceptorAdmin extends NabInternalFrame implements ActionListener, ReceptorStateListener {
    private static Font labelFont;

    private JTabbedPane tabs;
    private Receptor receptor;
    private JButton update;
    private StreamsPanel sPanel;
    private ModulesPanel mPanel;
    private DatabasesPanel dPanel;
    
    static {
        labelFont = new Font("SansSerif", Font.PLAIN, 9);
    }
    
    public ReceptorAdmin(Receptor rec) {
        super("Receptor: " + rec);
        
        receptor = rec;
        receptor.getState().addReceptorStateListener(this);
        setSize(600, 500);
        setMaximizable(true);
        setClosable(true);
        setResizable(true);
        setDefaultCloseOperation(JInternalFrame.HIDE_ON_CLOSE);
        
        addControls();
        receptor.getOutComm().updateReceptor();
    }
    
    public void actionPerformed(ActionEvent e){
        Object o = e.getSource();
        
        if(o == update){
            receptor.getOutComm().updateReceptor();
        }
    }
    
    public void receptorStateChanged() {
        sPanel.update();
        mPanel.update();
        dPanel.update();
    }
    
    private void addControls(){
        Container c = getContentPane();
        
        c.setLayout(new BorderLayout());
        c.add(tabs = new JTabbedPane());
        
        tabs.addTab("Streams", sPanel = new StreamsPanel(receptor));
        tabs.addTab("Modules", mPanel = new ModulesPanel(receptor));
        tabs.addTab("Databases", dPanel = new DatabasesPanel(receptor));
    }
    
    public static JLabel makeLabel(String str){
        JLabel label = new JLabel(str);
        
        label.setFont(labelFont);
        label.setHorizontalAlignment(JLabel.CENTER);
        
        return label;
    }

    public void setTerminalManager(TerminalManager termMan) {
        dPanel.setTerminalManager(termMan);
    }
}