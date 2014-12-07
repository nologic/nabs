/*
 * DatabaseTerminal.java
 *
 * Created on July 5, 2005, 1:18 PM
 *
 */

package eunomia.gui.archival;

import eunomia.core.data.staticData.DatabaseDownloader;
import eunomia.core.data.staticData.DatabaseTerminal;
import eunomia.core.data.staticData.exporter.CSVExporter;
import eunomia.core.data.staticData.exporter.Exporter;
import eunomia.util.database.sql.DiskResultSet;
import eunomia.core.receptor.Receptor;
import eunomia.gui.FileChooser;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.apache.log4j.*;

/**
 *
 * @author Mikhail Sosonkin
 */
public class DatabaseTerm extends JPanel implements Runnable, ActionListener, DatabaseTerminal {
    private JTextArea input;
    private JTextArea lastQuery;
    private JComboBox historyCombo;
    private JButton export;
    private JButton submit;
    private DiskResultSet drs;
    private String db;
    private Receptor receptor;
    private DiskResultSetTable rView;
    private Thread executer;
    private boolean stmtReady;
    private boolean exportReady;
    private DatabaseDownloader dd;
    private long timeSubmited;
    private LinkedList historyList;
    
    private static Logger logger;
    
    static {
        logger = Logger.getLogger(DatabaseTerm.class);
    }
    
    public DatabaseTerm(String dbName, Receptor rec){
        drs = new DiskResultSet();
        historyList = new LinkedList();
        receptor = rec;
        db = dbName;
        addControls();
        
        exportReady = false;
        
        new Thread(this, "Database Terminal").start();
    }
    
    public void run(){
        while(true){
            try {
                Thread.sleep(200);
            } catch (Exception e) {
            }
            
            if(exportReady){
                export();
            }
        }
    }
    
    public String getCurrentQuery() {
        return input.getText();
    }
    
    private void executeStatement(){
        input.setEnabled(false);
        timeSubmited = System.currentTimeMillis();
        receptor.getOutComm().queryDatabase(db, input.getText());
        stmtReady = true;
        logger.info("Submited query for DB: " + db + " Receptor: " + receptor.getName());
        if(!historyList.contains(input.getText())){
            historyList.addFirst(input.getText());
        }
        historyCombo.setModel(new DefaultComboBoxModel(historyList.toArray()));
    }
    
    public void setDataset(File index, File data) {
        try {
            drs.closeRandomReads();
            drs.setInputFiles(data.toString(), index.toString());
            drs.prepareRandomReads();
            rView.setResultSet(drs);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        input.setEnabled(true);
        input.grabFocus();
    }
    
    private void export(){
        Exporter exp = new CSVExporter();
        File file = FileChooser.getOpenFile();
        logger.info("Exporting resultset to " + file);
        
        try {
            exp.export(drs, file.toString());
        } catch (IOException ex){
            logger.error("Error exporting: " + ex.getMessage());
            return;
        }
        
        logger.info("Exported resultset to " + file);
    }
    
    public void actionPerformed(ActionEvent e){
        Object o = e.getSource();
        
        if(o == submit){
            executeStatement();
        } else if(o == export){
            exportReady = true;
        } else if(o == historyCombo){
            input.setText(historyCombo.getSelectedItem().toString());
        }
    }
    
    private void addControls() {
        JPanel buttonsPanel = new JPanel(new GridLayout(2, 1));
        JPanel buttomPanel = new JPanel(new BorderLayout());
        JPanel queryPanel = new JPanel(new BorderLayout());
        
        setLayout(new BorderLayout());
        
        buttonsPanel.add(export = new JButton("Export to CSV format"));
        buttonsPanel.add(submit = new JButton("Submit Query"));
        queryPanel.add(new JScrollPane(input = new JTextArea(2, 5)));
        queryPanel.add(historyCombo = new JComboBox(), BorderLayout.SOUTH);
        buttomPanel.add(buttonsPanel, BorderLayout.EAST);
        buttomPanel.add(queryPanel);
        
        add(rView = new DiskResultSetTable());
        add(buttomPanel, BorderLayout.SOUTH);
        
        submit.addActionListener(this);
        input.grabFocus();
        
        export.addActionListener(this);
        historyCombo.addActionListener(this);
    }

    public void lastQueryFailed() {
        input.setEnabled(true);
        input.grabFocus();
    }
}