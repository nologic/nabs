/*
 * WorldSettings.java
 *
 * Created on March 27, 2006, 9:25 PM
 *
 */

package eunomia.gui.settings;

import eunomia.gui.NABStrings;
import eunomia.gui.desktop.NabInternalFrame;
import java.awt.BorderLayout;
import java.awt.Container;
import javax.swing.JDialog;
import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

/**
 *
 * @author Mikhail Sosonkin
 */
public class WorldSettings extends NabInternalFrame {
    private ReceptorServerManager rsm;
    private ColorSettings color;
    private JPanel colorSettings;
    private JDialog cDialog;
    
    public WorldSettings() {
        super(NABStrings.SETTINGS_WINDOW_TITLE);
        
        setSize(500, 400);
        setDefaultCloseOperation(JInternalFrame.HIDE_ON_CLOSE);
        setMaximizable(true);
        setResizable(true);
        setClosable(true);
        
        addControls();
    }
    
    public void showColorDialog() {
        if(cDialog == null) {
            cDialog = new JDialog(JOptionPane.getFrameForComponent(this), "Color Settings", true);
            cDialog.setSize(400, 400);
            color.setCloseDialog(cDialog);
        }
        
        cDialog.setContentPane(color);
        cDialog.setLocationRelativeTo(this);
        cDialog.setVisible(true);
        //after it's closed
        colorSettings.add(color);
    }

    public ReceptorServerManager getReceptorServerManager(){
        return rsm;
    }

    private void addControls() {
        JTabbedPane tabs = new JTabbedPane();
        Container c = getContentPane();
        
        c.setLayout(new BorderLayout());
        
        colorSettings = new JPanel(new BorderLayout());
        colorSettings.add(color = new ColorSettings());
        
        tabs.addTab(NABStrings.SETTINGS_RECEPTORS_TAB, rsm = new ReceptorServerManager());
        tabs.addTab(NABStrings.SETTINGS_COLOR_TAB, colorSettings);
        
        c.add(tabs);
    }
}