/*
 * DatabaseAnalysisPanel.java
 *
 * Created on December 2, 2006, 7:24 PM
 *
 */

package eunomia.gui.archival;

import eunomia.core.receptor.Receptor;
import eunomia.gui.MainGui;
import eunomia.messages.ByteArrayMessage;
import eunomia.messages.DatabaseDescriptor;
import eunomia.plugin.interfaces.GUIStaticAnalysisModule;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataOutputStream;
import java.util.Iterator;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 *
 * @author Mikhail Sosonkin
 */
public class DatabaseAnalysisPanel extends JPanel implements ActionListener {
    private DatabaseDescriptor db;
    private Receptor receptor;
    private JButton startModule;
    private JButton submit;
    private JButton report;
    private JComboBox list;
    private JDialog dialog;
    
    public DatabaseAnalysisPanel(Receptor r) {
        receptor = r;
        addControls();
    }
    
    public DatabaseDescriptor getDatabase(){
        return db;
    }
    
    public void setDatabase(DatabaseDescriptor db) {
        this.db = db;
        updateContext(db);
    }

    private void updateContext(DatabaseDescriptor db) {
        list.removeAllItems();
        Iterator it = receptor.getState().getAnalysisModules().iterator();
        while (it.hasNext()) {
            list.addItem(it.next());
        }
    }

    public void actionPerformed(ActionEvent e) {
        Object o = e.getSource();
        
        if(o == startModule){
            Object name = list.getSelectedItem();
            if(name != null){
                try {
                    startModule(name.toString());
                } catch (InstantiationException ex) {
                    ex.printStackTrace();
                } catch (IllegalAccessException ex) {
                    ex.printStackTrace();
                } catch (ClassNotFoundException ex) {
                    ex.printStackTrace();
                }
            }
        } else if(o == submit) {
            if(dialog != null) {
                dialog.dispose();
            }
        } else if(o == report) {
            getReport();
        }
    }
    
    private void getReport() {
        receptor.getOutComm().getAnalysisSummaryReport(db.getName());
    }
    
    private void startModule(String module) throws ClassNotFoundException, InstantiationException, IllegalAccessException{
        GUIStaticAnalysisModule mod = receptor.getManager().instantiateAnalysisModule(module);
        dialog = new JDialog(MainGui.v(), module, true);
        
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setSize(300, 300);
        Container c = dialog.getContentPane();
        c.setLayout(new BorderLayout());
        
        c.add(submit, BorderLayout.SOUTH);
        c.add(mod.getArgumentsComponent());
        
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
        
        ByteArrayMessage bam = new ByteArrayMessage();
        mod.getArguments(new DataOutputStream(bam.getOutputStream()));
        receptor.getOutComm().analyzeDatabase(db.getName(), list.getSelectedItem().toString(), bam);
    }

    private void addControls() {
        JPanel buttonsPanel = new JPanel(new GridLayout(1, 2));
        
        buttonsPanel.add(startModule = new JButton("Start"));
        buttonsPanel.add(report = new JButton("Report"));
        
        add(new JScrollPane(list = new JComboBox()));
        add(buttonsPanel, BorderLayout.SOUTH);
        
        submit = new JButton("Start analysis");
        submit.addActionListener(this);
        startModule.addActionListener(this);
        report.addActionListener(this);
    }
}