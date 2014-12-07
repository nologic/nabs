/*
 * ReceptorAdmin.java
 *
 * Created on December 12, 2005, 10:47 PM
 *
 */

package eunomia.gui.realtime;

import eunomia.core.receptor.Receptor;
import eunomia.gui.NABStrings;
import eunomia.gui.IconResource;
import eunomia.gui.archival.TerminalManager;
import eunomia.gui.desktop.NabInternalFrame;
import eunomia.gui.realtime.receptorAdmin.DatabasesPanel;
import eunomia.gui.realtime.receptorAdmin.ModulesPanel;
import eunomia.gui.realtime.receptorAdmin.SievePanel;
import eunomia.gui.realtime.receptorAdmin.StreamsPanel;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Font;
import javax.swing.JButton;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JTabbedPane;


/**
 *
 * @author Mikhail Sosonkin
 */
public class ReceptorAdmin extends NabInternalFrame {
    private static Font labelFont;

    private JTabbedPane tabs;
    private Receptor receptor;
    private JButton update;
    private StreamsPanel sPanel;
    private ModulesPanel mPanel;
    private DatabasesPanel dPanel;
    private SievePanel sievePanel;
    
    static {
        labelFont = new Font("SansSerif", Font.PLAIN, 9);
    }
    
    public ReceptorAdmin(Receptor rec) {
        super("Control Panel");
        
        receptor = rec;

        setSize(600, 500);
        setMaximizable(true);
        setClosable(false);
        setResizable(true);
        setIconifiable(false);
        setDefaultCloseOperation(JInternalFrame.HIDE_ON_CLOSE);
        setFrameIcon(IconResource.getReceptorAdminister());
        
        addControls();
        receptor.getOutComm().updateReceptor();
    }
    
    private void addControls(){
        Container c = getContentPane();
        
        c.setLayout(new BorderLayout());
        c.add(tabs = new JTabbedPane());
        
        tabs.addTab("Sensors", sPanel = new StreamsPanel(receptor));
        tabs.addTab("Modules", mPanel = new ModulesPanel(receptor));
        tabs.addTab("Databases", dPanel = new DatabasesPanel(receptor));
        tabs.addTab(NABStrings.CURRENT_RECEPTOR_NAME, sievePanel = new SievePanel(receptor));

        receptor.getState().addReceptorStateListener(sPanel);
        receptor.getState().addReceptorStateListener(dPanel);
        receptor.getState().addReceptorStateListener(sievePanel);
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