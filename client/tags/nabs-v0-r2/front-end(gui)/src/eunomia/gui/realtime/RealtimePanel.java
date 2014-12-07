/*
 * RealtimePanel.java
 *
 * Created on June 17, 2005, 5:15 PM
 */

package eunomia.gui.realtime;

import eunomia.core.managers.ModuleManager;
import eunomia.core.managers.listeners.ModuleManagerListener;
import eunomia.gui.desktop.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import eunomia.core.receptor.*;
import eunomia.gui.*;
import eunomia.gui.archival.TerminalManager;
import eunomia.gui.desktop.Desktop;
import eunomia.gui.module.ModulePortal;
import eunomia.messages.receptor.ModuleHandle;
import eunomia.plugin.*;
import eunomia.plugin.interfaces.*;
import java.util.*;

/**
 *
 * @author  Mikhail Sosonkin
 */
public class RealtimePanel extends JPanel implements ActionListener, ModuleManagerListener, FrameCreator {
    private Desktop modDesk;
    private JButton openAdmin;
    private Receptor receptor;
    private ReceptorAdmin admin;
    private GUIModule statMod;
    private HashMap modToPort;
    private ModuleManager manager;
    private int xLoc, yLoc;
    private boolean doCon;
    
    public RealtimePanel(Receptor rec) {
        receptor = rec;
        manager = receptor.getManager();
        
        modToPort = new HashMap();
        manager.addModuleManagerListener(this);
        
        xLoc = yLoc = 0;
        
        addControls();
    }
    
    public void setConnect(boolean doConnect){
        doCon = doConnect;
    }
    
    public void moduleListChanged() {
        updateModuleList();
    }

    public void moduleAdded(ModuleHandle handle) {
        moduleAdded((GUIPlugin)manager.getModuleByHandle(handle));
    }

    public void moduleRemoved(ModuleHandle handle, GUIModule mod) {
        ModulePortal port = (ModulePortal)modToPort.get(mod);
        
        if(port != null){
            port.terminate();
            port.dispose();
        }
    }
    
    public void reset(){
        Iterator it = modToPort.values().iterator();
        while (it.hasNext()) {
            ModulePortal mp = (ModulePortal) it.next();
            mp.dispose();
        }
        
        modToPort.clear();
        modDesk.setSideComponent(null);
    }
    
    private void moduleAdded(GUIPlugin mod){
        if(mod.getModule().getClass().getName().equals("eunomia.plugin.gui.streamStatus.Main")){
            // Stream Status is a special "hard coded" module.
            statMod = mod;
            modDesk.setSideComponent(statMod.getJComponent());
        } else {
            if(modToPort.get(mod) == null){
                ModulePortal modPortal = new ModulePortal(mod);
                addWindow(modPortal);
                modPortal.setVisible(true);
                modToPort.put(mod, modPortal);
            }
        }
    }
    
    private void addWindow(ModulePortal portal){
        modDesk.add(portal);
        portal.setLocation(xLoc += 15, yLoc += 15);
        if(xLoc > 100 || yLoc > 100){
            xLoc = yLoc = 0;
        }
    }
    
    public void updateModuleList(){
        Iterator it = manager.getModules();
        
        while(it.hasNext()){
            GUIPlugin mod = (GUIPlugin)it.next();
            moduleAdded(mod);
        }
    }
    
    public void actionPerformed(ActionEvent e){
        Object o = e.getSource();
        
        if(o == openAdmin){
            if(admin == null){
                modDesk.add(admin = new ReceptorAdmin(receptor));
                admin.setTerminalManager(new TerminalManager(this));
            }
            
            admin.setVisible(true);
        }
    }
    
    public Receptor getReceptor(){
        return receptor;
    }
    
    private void addControls(){
        JToolBar toolBar;
        
        modDesk = new Desktop();

        setLayout(new BorderLayout());
        
        add(toolBar = new JToolBar(), BorderLayout.NORTH);
        add(modDesk);
        
        toolBar.add(openAdmin = makeButton("Administer", "Open administrative window", IconResource.getReceptorAdminister()));
        
        toolBar.setFloatable(false);
        toolBar.setRollover(true);
        openAdmin.addActionListener(this);
    }
    
    public JInternalFrame createInterfaceFrame() {
        JInternalFrame frame = new NabInternalFrame();
        modDesk.add(frame);

        return frame;
    }
    
    public JButton makeButton(String text, String tp, Icon icon){
        JButton button = new JButton(text);
        
        Font font = button.getFont();
        button.setToolTipText(tp);
        button.setFont(new Font(font.getName(), Font.BOLD, 9));
        button.setIcon(icon);
        
        return button;
    }
}