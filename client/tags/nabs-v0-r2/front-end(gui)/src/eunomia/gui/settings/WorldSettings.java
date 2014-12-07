/*
 * WorldSettings.java
 *
 * Created on March 27, 2006, 9:25 PM
 *
 */

package eunomia.gui.settings;

import eunomia.*;
import eunomia.gui.desktop.NabInternalFrame;
import eunomia.gui.settings.ReceptorServerManager;
import java.awt.*;
import javax.swing.*;

/**
 *
 * @author Mikhail Sosonkin
 */
public class WorldSettings extends NabInternalFrame implements Exiter {
    private ReceptorServerManager rsm;
    
    public WorldSettings() {
        super(NABStrings.SETTINGS_WINDOW_TITLE);
        
        setSize(500, 400);
        setDefaultCloseOperation(JInternalFrame.HIDE_ON_CLOSE);
        setMaximizable(true);
        setResizable(true);
        setClosable(true);
        
        addControls();
    }

    public void startExitSequence() {
    }
    
    public ReceptorServerManager getReceptorServerManager(){
        return rsm;
    }

    private void addControls() {
        JTabbedPane tabs = new JTabbedPane();
        Container c = getContentPane();
        
        c.setLayout(new BorderLayout());
        
        tabs.addTab(NABStrings.SETTINGS_RECEPTORS_TAB, rsm = new ReceptorServerManager());
        tabs.addTab(NABStrings.SETTINGS_MODULES_TAB, new ModuleMapper());
        tabs.addTab(NABStrings.SETTINGS_COLOR_TAB, new ColorSettings());
        
        c.add(tabs);
    }
}