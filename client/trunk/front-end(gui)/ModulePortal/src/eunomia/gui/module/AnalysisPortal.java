/*
 * ReportViewer.java
 *
 * Created on December 5, 2006, 10:22 PM
 *
 */

package eunomia.gui.module;

import eunomia.core.managers.event.state.AddDatabaseEvent;
import eunomia.core.managers.event.state.AddDatabaseTypeEvent;
import eunomia.core.managers.event.state.AddModuleEvent;
import eunomia.core.managers.event.state.AddStreamServerEvent;
import eunomia.core.managers.event.state.ReceptorUserAddedEvent;
import eunomia.core.managers.event.state.ReceptorUserRemovedEvent;
import eunomia.core.managers.event.state.RemoveDatabaseEvent;
import eunomia.core.managers.event.state.RemoveStreamServerEvent;
import eunomia.core.managers.event.state.StreamStatusChangedEvent;
import eunomia.core.managers.listeners.ReceptorStateListener;
import eunomia.gui.IconResource;
import eunomia.gui.NABStrings;
import eunomia.messages.DatabaseDescriptor;
import eunomia.module.AnlzFrontendModule;
import com.vivic.eunomia.module.frontend.FrontendAnalysisListener;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import org.apache.log4j.Logger;

/**
 *
 * @author Mikhail Sosonkin
 */
public class AnalysisPortal extends ModulePortal {
    private String name;
    private AnlzFrontendModule module;
    private JPanel holder;
    private JButton detach;
    private JButton control;
    private JButton refresh;
    private JButton commit;
    private JDialog controlDialog;

    private static Logger logger;

    
    static {
        logger = Logger.getLogger(AnalysisPortal.class);
    }
    
    public AnalysisPortal(AnlzFrontendModule inst) {
        super(inst);
        module = inst;
        
        holder.add(module.getJComponent());
    }
    
    public void terminate(){
        int ans = JOptionPane.showConfirmDialog(this, 
                "Closing this module portal will terminate the instance on the " + NABStrings.CURRENT_RECEPTOR_NAME + ". Are you sure you want to proceed?",
                "Should I proceed?", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        
        if(ans == JOptionPane.YES_OPTION) {
            module.getReceptor().getOutComm().terminateModule(module.getHandle());
        }
    }
    
    public void actionPerformed(ActionEvent e) {
        Object o = e.getSource();

        if(o == control){
            showControl();
        } else if(o == detach) {
            detach();
        } else if(o == commit){
            commitControlData();
        } else if(o == refresh){
            refreshControlData();
        }
    }
    
    public void commitControlData(){
        try {
            module.getReceptor().getOutComm().sendModuleControlData(module.getHandle(), module);
            controlDialog.dispose();
        } catch(Exception e){
            e.printStackTrace();
        }
    }
    
    public void refreshControlData(){
        try {
            module.getReceptor().getOutComm().getModuleControlData(module.getHandle());
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    
    protected void addControls(){
        holder = getContainer();
        
        control = addToolbarButton("", "Open Control Panel Dialog for this tool", IconResource.getFlowModuleControl(), this);
        detach = addToolbarButton("", "Opens the module in a seperate window", IconResource.getFlowModuleDetach(), this);
        
        setFrameIcon(IconResource.getAnlzModuleWindow());
    }

    public void setButtonMask(int mask) {
    }
    
    public void showControl(){
        Container c = null;
        JPanel buttonPanel = new JPanel(new BorderLayout());
        JPanel subButtonPanel = new JPanel(new GridLayout(1, 2, 5, 5));

        controlDialog = new JDialog(getOwner(), true);
        c = controlDialog.getContentPane();
        c.setLayout(new BorderLayout());

        controlDialog.setTitle("Control Panel: " + module.getTitle());
        controlDialog.setSize(700, 500);
        controlDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        controlDialog.setLocationRelativeTo(getContainer());

        buttonPanel.add(subButtonPanel, BorderLayout.EAST);
        subButtonPanel.add(refresh = new JButton("Restore to last"));
        subButtonPanel.add(commit = new JButton("Apply"));
        c.add(module.getControlComponent());
        c.add(buttonPanel, BorderLayout.SOUTH);

        commit.addActionListener(this);
        refresh.addActionListener(this);

        controlDialog.setVisible(true);
    }
}