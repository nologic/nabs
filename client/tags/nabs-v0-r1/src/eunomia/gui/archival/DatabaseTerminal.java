/*
 * DatabaseTerminal.java
 *
 * Created on July 5, 2005, 1:18 PM
 *
 */

package eunomia.gui.archival;

import java.sql.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;

import eunomia.core.data.staticData.*;
import eunomia.core.data.staticData.exporter.*;
import eunomia.gui.*;

import org.apache.log4j.*;

/**
 *
 * @author Mikhail Sosonkin
 */
public class DatabaseTerminal extends JPanel implements Runnable, ActionListener {
    private JTextField input;
    private JTextArea lastQuery;
    private JButton export;
    
    private Database db;
    private Statement stmt;
    private DiskResultSetTable rView;
    private DiskResultSet drs;
    private Thread executer;
    private boolean stmtReady;
    private boolean exportReady;
    
    private static Logger logger;
    
    static {
        logger = Logger.getLogger(DatabaseTerminal.class);
    }
    
    public DatabaseTerminal(Database database){
        db = database;
        addControls();
        drs = new DiskResultSet("drs_" + database +".bin");
        stmtReady = false;
        exportReady = false;
        
        executer = new Thread(this);
        executer.start();
    }
    
    public void run(){
        try {
            logger.info("Connecting to " + db);
            db.connect();
            stmt = db.getNewStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            stmt.setFetchSize(Integer.MIN_VALUE);
            logger.info("Connected to " + db);
        } catch (SQLException ex){
            logger.error("Unable to connect to " + db);
        }
        
        while(true){
            try{
                Thread.sleep(200);
            } catch(Exception e){
            }
            
            if(stmtReady){
                executeStatement();
                stmtReady = false;
            } else if(exportReady){
                export();
                exportReady = false;
            }
        }
    }
    
    private void executeStatement(){
        ResultSet set;
        input.setEnabled(false);
        try {
            stmt.execute(input.getText());
            set = stmt.getResultSet();
            drs.retrieveResultSet(set);
            rView.setResultSet(drs);
        } catch(Exception ex){
            logger.error("(Unable to execute) " + ex.getMessage());
            ex.printStackTrace();
        } catch(OutOfMemoryError t){
            t.printStackTrace();
            logger.error("Not enough memory to store response, try a smaller limit.");
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
        
        if(o == input){
            stmtReady = true;
        } else if(o == export){
            exportReady = true;
        }
    }
    
    private void addControls(){
        JPanel buttomPanel = new JPanel(new BorderLayout());
        
        setLayout(new BorderLayout());
        
        buttomPanel.add(export = new JButton("Export to CSV format"), BorderLayout.EAST);
        buttomPanel.add(input = new JTextField());
                
        add(rView = new DiskResultSetTable());
        add(buttomPanel, BorderLayout.SOUTH);
        
        input.addActionListener(this);
        input.grabFocus();
        
        export.addActionListener(this);
    }
}