/*
 * RealtimePanel.java
 *
 * Created on June 17, 2005, 5:15 PM
 */

package eunomia.gui.realtime;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

import eunomia.plugin.interfaces.*;
import eunomia.core.data.streamData.*;
import eunomia.core.managers.*;
import eunomia.gui.*;
import eunomia.gui.module.*;
import eunomia.plugin.streamStatus.*;

/**
 *  This class is crap and needs to be completely redone.
 *  Before that happens, Module Manager is required.
 *
 * @author  Mikhail Sosonkin
 */
public class RealtimePanel extends JPanel implements ActionListener {
    private ModulePortal hostView;
    private Module ssm;
    private StreamDataSource ds;
    private JDesktopPane modDesk;
    private JFrame detachmentFrame;
    private LinkedList modNameList;
    private LinkedList modInstances;
    private JMenuItem newLC, newPie;
    
    public RealtimePanel(StreamDataSource sds) {
        ds = sds;
        modNameList = new LinkedList();
        modInstances = new LinkedList();
        
        addControls();
        startDefaultModules();
        initializeData();
    }
    
    private void startDefaultModules(){
        ModulePortal hostView = new ModulePortal("eunomia.plugin.hostView.HostView");
        ModulePortal lossyHistogram = new ModulePortal("eunomia.plugin.lossyHistogram.LossyHistogramModule");
        ModulePortal pieChart = new ModulePortal("eunomia.plugin.pieChart.PieChartModule");
        
        hostView.setClosable(false);
        
        modInstances.add(hostView);
        modInstances.add(lossyHistogram);
        modInstances.add(pieChart);
        
        lossyHistogram.setSize(550, 400);
        lossyHistogram.setLocation(0, 170);
        pieChart.setSize(330, 250);
        pieChart.setLocation(350, 0);
    
        modDesk.add(hostView);
        modDesk.add(pieChart);
        modDesk.add(lossyHistogram);
        
        try {
            lossyHistogram.setSelected(true);
        } catch(Exception e){
            e.printStackTrace();
        }
        lossyHistogram.toFront();
    }
    
    public void actionPerformed(ActionEvent e){
        Object o = e.getSource();
        
        if(o == newLC){
            try {
                if(hostView != null && hostView.getModule() != null){
                    ModulePortal lossyHistogram = new ModulePortal("eunomia.plugin.lossyHistogram.LossyHistogramModule");
                    modInstances.add(lossyHistogram);
                    modDesk.add(lossyHistogram);
                    lossyHistogram.instantiate();
                    Module mod = lossyHistogram.getModule();
                    mod.setStream(ds);
                    ds.registerRaw(mod.getFlowPocessor());
                    mod.setProperty("HostView", hostView.getModule());
                    lossyHistogram.setVisible(true);
                }
            } catch(Exception ex){
                ex.printStackTrace();
            }
        } else if(o == newPie){
            try {
                ModulePortal pieChart = new ModulePortal("eunomia.plugin.pieChart.PieChartModule");
                modInstances.add(pieChart);
                modDesk.add(pieChart);
                pieChart.instantiate();
                Module mod = pieChart.getModule();
                mod.setStream(ds);
                ds.registerRaw(mod.getFlowPocessor());
                pieChart.setVisible(true);
            } catch(Exception ex){
                ex.printStackTrace();
            }
        }
    }
    
    public StreamDataSource getStreamDataSource(){
        return ds;
    }
    
    public void initializeData(){
        try {
            Iterator it = modInstances.iterator();
            while(it.hasNext()){
                ModulePortal modP = (ModulePortal)it.next();
                modP.instantiate();
                Module mod = modP.getModule();
                mod.setStream(ds);
                ds.registerRaw(mod.getFlowPocessor());
            }
            
            //HACK.
            ModulePortal modP = (ModulePortal)modInstances.get(1);
            hostView = (ModulePortal)modInstances.get(0);
            modP.getModule().setProperty("HostView", hostView.getModule());

            ds.initiate();
        } catch (Exception e){
            e.printStackTrace();
        }
    }
    
    private void addControls(){
        JMenuBar menuBar;
        
        modDesk = new JDesktopPane();
        modDesk.setDesktopManager(new MainDesktopManager());

        setLayout(new BorderLayout());
        
        add(menuBar = new JMenuBar(), BorderLayout.NORTH);
        add(new JScrollPane(modDesk));
        
        //special module.
        ssm = new StreamStatusModule();
        ds.registerRaw(ssm.getFlowPocessor());
        DataManager.ins.registerWithUpdater(ssm.getRefreshNotifier());
        add(ssm.getJComponent(), BorderLayout.SOUTH);
        
        JMenu newMenu = new JMenu("New");
        newLC = newMenu.add("Open New Heavy-Hitters View");
        newPie = newMenu.add("Open New Network View");
        menuBar.add(newMenu);
        
        newLC.addActionListener(this);
        newPie.addActionListener(this);
    }

    public JFrame getDetachmentFrame() {
        return detachmentFrame;
    }

    public void setDetachmentFrame(JFrame detachmentFrame) {
        this.detachmentFrame = detachmentFrame;
    }
}