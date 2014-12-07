/*
 * RealtimePanel.java
 *
 * Created on June 17, 2005, 5:15 PM
 */

package eunomia.gui.realtime;

import eunomia.core.managers.ModuleManager;
import eunomia.core.managers.event.state.module.ModuleAddedEvent;
import eunomia.core.managers.event.state.module.ModuleListChangedEvent;
import eunomia.core.managers.event.state.module.ModuleRemovedEvent;
import eunomia.core.managers.listeners.ModuleManagerListener;
import eunomia.core.receptor.Receptor;
import eunomia.gui.interfaces.FrameCreator;
import eunomia.gui.desktop.Desktop;
import eunomia.gui.desktop.NabInternalFrame;
import eunomia.gui.module.AnalysisPortal;
import eunomia.gui.module.ModulePortal;
import eunomia.gui.module.ProcessorPortal;
import eunomia.messages.receptor.ModuleHandle;
import eunomia.module.AnlzFrontendModule;
import eunomia.module.FrontendModule;
import eunomia.module.ProcFrontendModule;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.beans.PropertyVetoException;
import java.util.HashMap;
import java.util.Iterator;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;

/**
 *
 * @author  Mikhail Sosonkin
 */
public class RealtimePanel extends JPanel implements ModuleManagerListener, FrameCreator {
    private Desktop modDesk;
    
    private Receptor receptor;
    private ReceptorAdmin admin;
    private HashMap modToPort;
    private ModuleManager manager;
    private JInternalFrame reportFrame;
    
    private int xLoc, yLoc;
    private boolean doCon;
    
    public RealtimePanel(Receptor rec) {
        receptor = rec;
        manager = receptor.getManager();
        admin = new ReceptorAdmin(receptor);

        modToPort = new HashMap();
        manager.addModuleManagerListener(this);
        
        xLoc = yLoc = 0;
        
        addControls();
    }
    
    public void saveState() {
        Iterator it = modToPort.values().iterator();
        while (it.hasNext()) {
            Object o = it.next();
            ModulePortal mp = (ModulePortal) o;
            mp.saveWindowState();
        }
    }

    public void setConnect(boolean doConnect){
        doCon = doConnect;
        setEnabled(doCon);
    }
    
    public void moduleListChanged(ModuleListChangedEvent e) {
        updateModuleList();
    }

    public void moduleAdded(ModuleAddedEvent e) {
        moduleAdded(e.getHandle());
    }
    
    private void moduleAdded(ModuleHandle handle) {
        FrontendModule module = manager.getModule(handle);
        ModulePortal portal = null;
        
        ModulePortal modPortal = getPortal(module);
        if(modPortal != null){
            addWindow(modPortal, false);
    
            modPortal.setShowModuleID(false);
            modPortal.setVisible(true);
            modToPort.put(module, modPortal);
        }
    }
    
    private ModulePortal getPortal(FrontendModule module){
        if(module instanceof ProcFrontendModule) {
            ProcFrontendModule mod = (ProcFrontendModule)module;
            String modName = module.getHandle().getModuleName();
            if(modName.equals("streamStatus")){
                modDesk.setSideComponent(mod.getJComponent());
                modDesk.revalidate();
                modDesk.repaint();
                return null;
            }
            
            ProcessorPortal procPortal = new ProcessorPortal(mod);
            receptor.getOutComm().connectDefuaultModuleToServers(mod.getHandle(), true);
            
            if(modName.equals("networkPolicy")){
                procPortal.setButtonMask(ProcessorPortal.BUTTON_DETACH);
            } else if(modName.equals("feedBack")){
                procPortal.setButtonMask(0);
            } else if(modName.equals("networkStatus")){
                procPortal.setButtonMask(ProcessorPortal.BUTTON_DETACH | ProcessorPortal.BUTTON_CONTROL | ProcessorPortal.BUTTON_RESET);
            }
            
            return procPortal;
        } else if(module instanceof AnlzFrontendModule){
            return new AnalysisPortal((AnlzFrontendModule)module);
        }
        
        return null;
    }

    public void moduleRemoved(ModuleRemovedEvent e) {
        ModulePortal port = (ModulePortal)modToPort.get(e.getModule());
        
        if(port != null){
            port.dispose();
        }
    }
    
    public void reset(){
        Iterator it = modToPort.values().iterator();
        while (it.hasNext()) {
            ProcessorPortal mp = (ProcessorPortal) it.next();
            mp.dispose();
        }
        
        modToPort.clear();
        modDesk.setSideComponent(null);
    }
        
    private void addWindow(NabInternalFrame portal, boolean setLoc){
        modDesk.add(portal);
        
        try {
            portal.setClosable(false);
            portal.setIconifiable(false);
            portal.setMaximum(true);
        } catch (PropertyVetoException ex) {
            ex.printStackTrace();
        }
        
        if(setLoc){
            portal.setLocation(xLoc += 15, yLoc += 15);
            if(xLoc > 100 || yLoc > 100){
                xLoc = yLoc = 0;
            }
        }
    }
    
    public void updateModuleList(){
        Iterator it = manager.getModules();
        
        while(it.hasNext()){
            FrontendModule mod = (FrontendModule)it.next();
            moduleAdded(mod.getHandle());
        }
    }

    public Receptor getReceptor(){
        return receptor;
    }
    
    private void addControls(){
        JPanel controlPanel = new JPanel(new BorderLayout());
        modDesk = new Desktop();
        
        controlPanel.setPreferredSize(new Dimension(160, 0));
        controlPanel.add(admin);
        
        setLayout(new BorderLayout());
        
        add(modDesk);
        add(controlPanel, BorderLayout.EAST);
    }
    
    public JInternalFrame createInterfaceFrame() {
        JInternalFrame frame = new NabInternalFrame();
        modDesk.add(frame);

        return frame;
    }
}