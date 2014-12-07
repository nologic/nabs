/*
 * FilterEditor.java
 *
 * Created on August 1, 2005, 3:25 PM
 *
 */

package eunomia.gui.filter;
import com.vivic.eunomia.module.Descriptor;
import eunomia.core.managers.interfaces.ModuleFilterEditor;
import com.vivic.eunomia.filter.Filter;
import eunomia.module.ProcFrontendModule;
import com.vivic.eunomia.module.frontend.FrontendProcessorModule;
import eunomia.core.managers.ModuleDescriptor;
import eunomia.core.managers.ModuleLinker;
import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

/**
 *
 * @author Mikhail Sosonkin
 */
public class FilterEditor implements ModuleFilterEditor, WindowListener, ActionListener {
    private static FilterEditor ins;

    private ProcFrontendModule currentModule;
    private FilterEditorPanel editor;
    private JPanel contentPane;
    private JDialog dialog;
    private JButton commit;
    private boolean commited;
    
    public FilterEditor(Frame owner) {
        editor = new FilterEditorPanel();
        
        addControls();
    }
    
    private void setFilter(Filter f){
        commited = true;
        editor.setFilter(f, currentModule.getReceptor().getManager(), getChoices());
    }
    
    private void editModuleFilter(ProcFrontendModule module){
        currentModule = module;
        editor.setEnabled(false);
        module.getReceptor().getManager().getFilter(module, this);
    }
    
    public void editModuleFilterResp(FrontendProcessorModule module, Filter f){
        if(currentModule == module){
            editor.setEnabled(true);
            setFilter(f);
            dialog.validate();
            dialog.repaint();
            dialog.setTitle("Filter Editor");
        }
    }
    
    public static void initialize(Frame owner){
        if(ins == null){
            ins = new FilterEditor(owner);
        }
    }
    
    public static void editFilter(ProcFrontendModule module){
        if(ins != null){
            ins.editModuleFilter(module);
            ins.dialog.setVisible(true);
        }
    }
        
    public static void editFilter(Frame ownerOveride, ProcFrontendModule module){
        if(ins != null){
            ins.dialog = new JDialog(ownerOveride, true);
            ins.dialog.setTitle("Filter Editor - Obtaining Filter - Please Wait");
            ins.dialog.setSize(500, 300);
            ins.dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
            ins.dialog.addWindowListener(ins);
            ins.dialog.setContentPane(ins.contentPane);
            ins.dialog.setLocationRelativeTo(ownerOveride);
            editFilter(module);
        }
    }
    
    private String[] getChoices() {
        List list = new ArrayList();
        ModuleLinker linker = currentModule.getReceptor().getLinker();
        Iterator it = linker.getDescriptors().iterator();
        while (it.hasNext()) {
            ModuleDescriptor desc = (ModuleDescriptor) it.next();
            if(desc.getType() == Descriptor.TYPE_FLOW) {
                list.add(desc.getName());
            }
        }

        String[] ret = new String[list.size()];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = list.get(i).toString();
        }

        return ret;
    }
    
    public void actionPerformed(ActionEvent e){
        Object o = e.getSource();
        
        if(o == commit){
            commitFilter();
        }
    }
    
    private void commitFilter(){
        commited = false;
        editor.setModified(false);
        
        currentModule.getReceptor().getOutComm().sendChangeFilter(currentModule.getHandle(), editor.getFilter());
        
        dialog.dispose();
    }
    
    private void addControls() {
        JPanel buttonsPanel = new JPanel(new GridLayout(1, 1));
        JPanel buttomPanel = new JPanel(new BorderLayout());

        contentPane = new JPanel(new BorderLayout());
        
        buttonsPanel.add(commit = new JButton("Apply Filter"));
        buttomPanel.add(buttonsPanel, BorderLayout.EAST);

        contentPane.add(editor);
        contentPane.add(buttomPanel, BorderLayout.SOUTH);
        
        commit.addActionListener(this);
    }
    
    public void windowOpened(WindowEvent e) {
    }

    public void windowClosing(WindowEvent e) {
        if(!commited && editor.isModified()){
            int choice = JOptionPane.showConfirmDialog(dialog, "Do you want to save the changes?", 
                                                        "Change Warning", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
            if(choice == JOptionPane.NO_OPTION){
                dialog.dispose();
            } else if(choice == JOptionPane.YES_OPTION) {
                commitFilter();
                dialog.dispose();
            }
        } else {
            dialog.dispose();
        }
    }

    public void windowClosed(WindowEvent e) {
    }

    public void windowIconified(WindowEvent e) {
    }

    public void windowDeiconified(WindowEvent e) {
    }

    public void windowActivated(WindowEvent e) {
    }

    public void windowDeactivated(WindowEvent e) {
    }

}