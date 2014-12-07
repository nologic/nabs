/*
 * RealtimePanel.java
 *
 * Created on June 17, 2005, 5:15 PM
 */

package eunomia.gui.realtime;

import eunomia.gui.NABStrings;
import eunomia.core.managers.ModuleManager;
import eunomia.core.managers.event.state.module.ModuleAddedEvent;
import eunomia.core.managers.event.state.module.ModuleListChangedEvent;
import eunomia.core.managers.event.state.module.ModuleRemovedEvent;
import eunomia.core.managers.listeners.ModuleManagerListener;
import eunomia.core.receptor.Receptor;
import eunomia.gui.IconResource;
import eunomia.gui.MainGui;
import eunomia.gui.archival.DatabaseReportsPanel;
import eunomia.gui.archival.TerminalManager;
import eunomia.gui.desktop.Desktop;
import eunomia.gui.desktop.NabInternalFrame;
import eunomia.gui.interfaces.FrameCreator;
import eunomia.gui.module.AnalysisPortal;
import eunomia.gui.module.ModulePortal;
import eunomia.gui.module.ProcessorPortal;
import eunomia.gui.realtime.receptorAdmin.ModuleStartMenu;
import eunomia.messages.receptor.ModuleHandle;
import eunomia.module.AnlzFrontendModule;
import eunomia.module.FrontendModule;
import eunomia.module.ProcFrontendModule;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Iterator;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import javax.swing.JToolBar;

/**
 *
 * @author  Mikhail Sosonkin
 */
public class RealtimePanel extends JPanel implements ActionListener, ModuleManagerListener, FrameCreator {
    private Desktop modDesk;
    //private JButton openAdmin;
    private JButton detach;
    private JButton startModule;
    private JButton showAnalysis;
    
    private Receptor receptor;
    private ReceptorAdmin admin;
    private ModuleStartMenu modMenu;
    private HashMap modToPort;
    private ModuleManager manager;
    private JInternalFrame reportFrame;
    private DatabaseReportsPanel analPanel;
    
    private int xLoc, yLoc;
    private boolean doCon;
    
    public RealtimePanel(Receptor rec) {
        receptor = rec;
        manager = receptor.getManager();

        modToPort = new HashMap();
        manager.addModuleManagerListener(this);
        
        xLoc = yLoc = 0;
        
        addControls();
        createReportFrame();
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
    
    public void moduleAdded(ModuleHandle handle) {
        FrontendModule module = manager.getModule(handle);
        ModulePortal portal = null;
        
        ModulePortal modPortal = getPortal(module);
        if(modPortal != null){
            addWindow(modPortal, !modPortal.restoreWindowState());
            modPortal.setVisible(true);
            modToPort.put(module, modPortal);
        }
    }

    private ModulePortal getPortal(FrontendModule module){
        if(module instanceof ProcFrontendModule) {
            ProcFrontendModule mod = (ProcFrontendModule)module;
            if(module.getHandle().getModuleName().equals("streamStatus")){
                modDesk.setSideComponent(mod.getJComponent());
                modDesk.revalidate();
                modDesk.repaint();
                return null;
            }
            
            return new ProcessorPortal(mod);
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
    
    public void actionPerformed(ActionEvent e){
        Object o = e.getSource();
        
        /*if(o == openAdmin){
            admin.setVisible(true);
        } else*/ if(o == detach){
            NabInternalFrame frame = MainGui.v().getRealtimeFrameManager().getReceptorFrame(receptor);
            frame.detach();
        } else if(o == startModule) {
            modMenu.show(startModule, 5, 5);
        } else if(o == showAnalysis) {
            analPanel.getNewReport();
            reportFrame.show();
            reportFrame.toFront();
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
        
        //toolBar.add(openAdmin = makeButton("Administer", "Open administrative window", IconResource.getReceptorAdminister()));
        toolBar.add(startModule = makeButton("Start Module", "Start a new module instance", IconResource.getReceptorStartModule()));
        toolBar.add(showAnalysis = makeButton("Show Analysis", "Show database analysis summary window", IconResource.getReceptorShowSummary()));
        toolBar.addSeparator();
        toolBar.add(detach = makeButton(null, "Detach the " + NABStrings.CURRENT_RECEPTOR_NAME + " Console window", IconResource.getFlowModuleDetach()));
        
        modDesk.add(admin = new ReceptorAdmin(receptor));
        admin.setTerminalManager(new TerminalManager(this));
        modMenu = new ModuleStartMenu(receptor);

        admin.setVisible(true);
        
        toolBar.setFloatable(false);
        toolBar.setRollover(true);
        //openAdmin.addActionListener(this);
        detach.addActionListener(this);
        startModule.addActionListener(this);
        showAnalysis.addActionListener(this);
    }
    
    private void createReportFrame() {
        reportFrame = createInterfaceFrame();
        reportFrame.setTitle("Analysis Report");
        reportFrame.setFrameIcon(IconResource.getReceptorShowSummary());
        reportFrame.setSize(370, 200);
        reportFrame.setDefaultCloseOperation(JInternalFrame.HIDE_ON_CLOSE);
        
        Container c = reportFrame.getContentPane();
        c.setLayout(new BorderLayout());
        c.add(analPanel = new DatabaseReportsPanel(receptor));
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