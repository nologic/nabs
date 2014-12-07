/*
 * DatabaseTerminal.java
 *
 * Created on July 5, 2005, 1:18 PM
 *
 */

package eunomia.gui.archival;

import eunomia.core.data.staticData.DatabaseDownloader;
import eunomia.messages.Message;
import eunomia.util.database.sql.DiskResultSet;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import eunomia.core.data.staticData.exporter.*;
import eunomia.core.receptor.Receptor;
import eunomia.core.receptor.listeners.MessageReceiver;
import eunomia.gui.*;
import eunomia.messages.receptor.msg.rsp.DatabaseTerminalOpenMessage;
import java.util.LinkedList;

import org.apache.log4j.*;

/**
 *
 * @author Mikhail Sosonkin
 */
public class DatabaseTerminal extends JPanel implements Runnable, ActionListener, MessageReceiver {
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
    private DatabaseTerminalOpenMessage dtom;
    private DatabaseDownloader dd;
    private long timeSubmited;
    private LinkedList historyList;
    
    private static Logger logger;
    
    static {
        logger = Logger.getLogger(DatabaseTerminal.class);
    }
    
    public DatabaseTerminal(String dbName, Receptor rec){
        historyList = new LinkedList();
        receptor = rec;
        db = dbName;
        addControls();
        
        stmtReady = false;
        exportReady = false;
        
        dd = new DatabaseDownloader(rec.getName() + "_" + db + ".dat", rec.getName() + "_" + db + ".idx");
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
            
            if(!stmtReady){
                continue;
            }

            if(dtom == null){
                long timeDiff = System.currentTimeMillis() - timeSubmited;
                if(timeDiff > 30000){ // 30 sec timeout
                    stmtReady = false;
                    input.setEnabled(true);
                    input.grabFocus();
                    logger.info("Query timed out for DB: " + db + " + Receptor: " + receptor.getName());
                }
                continue;
            }
            
            try {
                logger.info("Downloading data for DB: " + db + " Receptor: " + receptor.getName());
                rView.setEnabled(false);
                drs = dd.downloadResultSet(receptor.getIP(), dtom.getPort1(), dtom.getPort2(), dtom.getRandom1(), dtom.getRandom2());
                rView.setEnabled(true);
                if(drs != null){
                    logger.info("Data downloaded for DB: " + db + " Receptor: " + receptor.getName());
                    rView.setResultSet(drs);
                } else {
                    rView.clear();
                    logger.info("No data for DB: " + db + " Receptor: " + receptor.getName());
                }
            } catch (Exception e){
                e.printStackTrace();
                rView.clear();
                logger.info("Unable to display result set: " + e.getMessage());
            }

            stmtReady = false;
            input.setEnabled(true);
            input.grabFocus();
        }
    }
    
    private void executeStatement(){
        input.setEnabled(false);
        timeSubmited = System.currentTimeMillis();
        receptor.getOutComm().queryDatabase(db, input.getText(), this);
        stmtReady = true;
        logger.info("Submited query for DB: " + db + " Receptor: " + receptor.getName());
        if(!historyList.contains(input.getText())){
            historyList.addFirst(input.getText());
        }
        historyCombo.setModel(new DefaultComboBoxModel(historyList.toArray()));
    }
    
    public void messageResponse(Message msg) {
        if(msg instanceof DatabaseTerminalOpenMessage){
            dtom = (DatabaseTerminalOpenMessage)msg;
        }
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
}