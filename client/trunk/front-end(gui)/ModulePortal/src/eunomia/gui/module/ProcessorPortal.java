/*
 * ModulePort.java
 *
 * Created on July 29, 2005, 4:13 PM
 *
 */

package eunomia.gui.module;
import eunomia.gui.IconResource;
import eunomia.gui.NABStrings;
import eunomia.gui.filter.FilterEditor;
import eunomia.gui.module.proc.ModuleInstanceDetails;
import eunomia.messages.module.msg.ActionMessage;
import eunomia.module.ProcFrontendModule;
import com.vivic.eunomia.module.frontend.FrontendProcessorListener;
import com.vivic.eunomia.module.frontend.FrontendProcessorModule;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;

/**
 *
 * @author Mikhail Sosonkin
 */
public class ProcessorPortal extends ModulePortal implements FrontendProcessorListener {
    public static int BUTTON_CONTROL = 0x1 << 0;
    public static int BUTTON_FILTER = 0x1 << 1;
    public static int BUTTON_STOP = 0x1 << 2;
    public static int BUTTON_START = 0x1 << 3;
    public static int BUTTON_RESET = 0x1 << 4;
    public static int BUTTON_LISTEN_ALL = 0x1 << 5;
    public static int BUTTON_LISTEN_NONE = 0x1 << 6;
    public static int BUTTON_LISTEN_CHOOSE = 0x1 << 7;
    public static int BUTTON_DETACH = 0x1 << 8;
    
    private String name;
    private ProcFrontendModule module;
    private JPanel holder;
    private JPanel mainPanel;
    private JDialog controlDialog;
    private JDialog detailsDialog;
    private ModuleInstanceDetails details;
    
    private JButton control;
    private JButton openFilter;
    private JButton stop;
    private JButton start;
    private JButton reset;
    private JButton commit;
    private JButton refresh;
    private JButton detach;
    private JButton closeListen;
    
    private JButton listenAll;
    private JButton listenNone;
    private JButton listenChoose;
    
    private boolean isClosing;
    private boolean showStreamControl;
    
    public ProcessorPortal(ProcFrontendModule inst) {
        super(inst);
        
        showStreamControl = true;
        module = inst;
        isClosing = false;
        details = new ModuleInstanceDetails(inst.getReceptor());
        details.setModule(inst);
        
        holder.add(module.getJComponent());
        
        module.addFrontendProcessorListener(this);
    }

    public void terminate(){
        int ans = JOptionPane.showConfirmDialog(this, 
                "Closing this module portal will terminate the instance on the " + NABStrings.CURRENT_RECEPTOR_NAME + ". Are you sure you want to proceed?",
                "Should I proceed?", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if(ans == JOptionPane.YES_OPTION){
            isClosing = true;
            setTitle("CLOSING - " + getTitle());

            getContentPane().setEnabled(false);
            removeInternalFrameListener(this);

            if(controlDialog != null){
                controlDialog.dispose();
            }

            module.removeFrontendProcessorListener(this);

            module.getReceptor().getOutComm().terminateModule(module.getHandle());
        } 
    }
    
    public void statusUpdated(FrontendProcessorModule mod) {
        setTitle(module.getTitle());
    }
    
    public void controlUpdated(FrontendProcessorModule mod){
    }
    
    public void controlObtained(FrontendProcessorModule mod){
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
        controlDialog.setLocationRelativeTo(mainPanel);

        buttonPanel.add(subButtonPanel, BorderLayout.EAST);
        subButtonPanel.add(refresh = new JButton("Restore to last"));
        subButtonPanel.add(commit = new JButton("Apply"));
        c.add(module.getControlComponent());
        c.add(buttonPanel, BorderLayout.SOUTH);

        commit.addActionListener(this);
        refresh.addActionListener(this);

        controlDialog.setVisible(true);
    }
    
    public void showListenChoice(){
        Container c = null;
        JPanel buttonPanel = new JPanel(new BorderLayout());
        JPanel subButtonPanel = new JPanel(new GridLayout(1, 2, 5, 5));

        detailsDialog = new JDialog(getOwner(), true);
        c = detailsDialog.getContentPane();
        c.setLayout(new BorderLayout());

        detailsDialog.setTitle("Sensor Panel: " + module.getTitle());
        detailsDialog.setSize(500, 300);
        detailsDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        detailsDialog.setLocationRelativeTo(mainPanel);

        buttonPanel.add(subButtonPanel, BorderLayout.EAST);
        subButtonPanel.add(closeListen = new JButton("Close"));
        c.add(details);
        c.add(buttonPanel, BorderLayout.SOUTH);

        closeListen.addActionListener(this);

        details.update();
        detailsDialog.setVisible(true);
    }
    
    public void openFilterEditor(){
        Frame owner = null;
        try {
            owner = JOptionPane.getFrameForComponent(mainPanel);
            FilterEditor.editFilter(owner, module);
        } catch(Exception e){
            e.printStackTrace();
            FilterEditor.editFilter(module);
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
    
    public void actionPerformed(ActionEvent e){
        Object o = e.getSource();
        
        if(o == control){
            showControl();
        } else if(o == stop){
            module.getReceptor().getOutComm().sendAction(module.getHandle(), ActionMessage.STOP);
        } else if(o == start){
            module.getReceptor().getOutComm().sendAction(module.getHandle(), ActionMessage.START);
        } else if(o == reset){
            module.getReceptor().getOutComm().sendAction(module.getHandle(), ActionMessage.RESET);
        } else if(o == openFilter){
            openFilterEditor();
        } else if(o == commit){
            commitControlData();
        } else if(o == refresh){
            refreshControlData();
        } else if(o == detach){
            detach();
        } else if(o == listenAll){
            details.selectAll(true);
        } else if(o == listenNone){
            details.selectAll(false);
        } else if(o == listenChoose){
            showListenChoice();
        } else if(o == closeListen) {
            detailsDialog.dispose();
        }
    }
    
    protected void addControls(){
        holder = getContainer();
        
        control = addToolbarButton("", "Open Control Panel Dialog for this tool", IconResource.getFlowModuleControl(), this);
        openFilter = addToolbarButton("", "Filter Setup", IconResource.getFlowModuleFilter(), this);
        //toolBar.addSeparator(seperatorDimension);
        start = addToolbarButton("", "Continues the processing of flows", IconResource.getFlowModuleStart(), this);
        stop = addToolbarButton("", "Pauses processing", IconResource.getFlowModuleStop(), this);
        reset = addToolbarButton("", "Reset data", IconResource.getFlowModuleReset(), this);
        //toolBar.addSeparator(seperatorDimension);
        listenAll = addToolbarButton("", "Listen to all sensors", IconResource.getFlowModuleListenAll(), this);
        listenNone = addToolbarButton("", "Ignore all sensors", IconResource.getFlowModuleListenNone(), this);
        listenChoose = addToolbarButton("", "Choose sensors", IconResource.getFlowModuleListenChoose(), this);
        //toolBar.addSeparator(seperatorDimension);
        detach = addToolbarButton("", "Opens the tool in a seperate window", IconResource.getFlowModuleDetach(), this);
        
        setFrameIcon(IconResource.getFlowModuleWindow());
    }
    
    public void streamListUpdated(FrontendProcessorModule mod) {
        details.update();
    }

    public void setButtonMask(int mask) {
        this.buttonMask = mask;

        control.setVisible((mask & BUTTON_CONTROL) != 0);
        openFilter.setVisible((mask & BUTTON_FILTER) != 0);
        start.setVisible((mask & BUTTON_START) != 0);
        stop.setVisible((mask & BUTTON_STOP) != 0);
        reset.setVisible((mask & BUTTON_RESET) != 0);
        listenAll.setVisible((mask & BUTTON_LISTEN_ALL) != 0);
        listenNone.setVisible((mask & BUTTON_LISTEN_NONE) != 0);
        listenChoose.setVisible((mask & BUTTON_LISTEN_CHOOSE) != 0);
        detach.setVisible((mask & BUTTON_DETACH) != 0);
        toolBar.setVisible(mask != 0);
    }
}