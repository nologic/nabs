/*
 * DatabaseConfigurationPanel.java
 *
 * Created on June 28, 2005, 6:24 PM
 *
 */

package eunomia.gui.archival;

import eunomia.core.receptor.Receptor;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import eunomia.messages.DatabaseDescriptor;

import org.apache.log4j.*;

/**
 *
 * @author Mikhail Sosonkin
 */
public class DatabaseConfigurationPanel extends JPanel implements ActionListener {
    private JButton apply, openTerm;
    private JTextField serverIP, serverPort, username, password, dbName, tableName;
    private JComboBox types;
    private DatabaseDescriptor db;
    private JLabel name;
    private Receptor receptor;
    private JCheckBox connectDb, collectDb;
    private TerminalManager termMan;
    private DatabaseAnalysisPanel dbAnalPanel;
    private DatabaseReportsPanel dbReportsPanel;
    
    private static Logger logger;
    
    static {
        logger = Logger.getLogger(DatabaseConfigurationPanel.class);
    }
    
    public DatabaseConfigurationPanel(Receptor rec) {
        receptor = rec;
        addControls();
    }
    
    public void setTerminalManager(TerminalManager tMan) {
        termMan = tMan;
    }
    
    public void setDatabase(DatabaseDescriptor db){
        this.db = db;
        
        types.setModel(new DefaultComboBoxModel(receptor.getState().getDatabaseTypes().toArray()));
        if(db != null){
            name.setText(db.getName());
            serverIP.setText(db.getAddress());
            serverPort.setText(Integer.toString(db.getPort()));
            username.setText(db.getUsername());
            password.setText(db.getPassword());
            dbName.setText(db.getDbName());
            tableName.setText(db.getTableName());
            connectDb.setSelected(db.isConnected());
            collectDb.setSelected(db.isCollecting());
            connectDb.setEnabled(true);
            collectDb.setEnabled(true);
            dbAnalPanel.setDatabase(db);
            dbReportsPanel.setDatabase(db);
            types.setSelectedItem(db.getDbType());
        } else {
            name.setText("");
            serverIP.setText("");
            serverPort.setText("");
            username.setText("");
            password.setText("");
            dbName.setText("");
            tableName.setText("");
            connectDb.setSelected(false);
            collectDb.setSelected(false);
            connectDb.setEnabled(false);
            collectDb.setEnabled(false);
        }
    }
    
    public DatabaseDescriptor getDatabase(){
        return db;
    }
    
    private void applySettings(){
        try {
            db.setAddress(serverIP.getText());
            db.setPort(Integer.parseInt(serverPort.getText()));
            db.setUsername(username.getText());
            if(password.getText().equals("")){
                db.setPassword(null);
            } else {
                db.setPassword(password.getText());
            }
            db.setDbName(dbName.getText());
            db.setTableName(tableName.getText());
            db.setDbType(types.getSelectedItem().toString());
            receptor.getOutComm().addDatabase(db);
        } catch(Exception e){
            e.printStackTrace();
            logger.error("Exception: " + e.getMessage());
        }
    }
    
    private void openTerminal(){
        if(termMan != null){
            termMan.openTerminal(db.getName(), receptor);
        }
    }
    
    private void collect(){
        try {
            if(connectDb.isSelected()){
                receptor.getOutComm().collectDatabase(db, collectDb.isSelected());
            } else {
                logger.info("Must be connected in order to collect");
            }
        } catch(Exception e){
            e.printStackTrace();
            logger.error("Exception: " + e.getMessage());
        }
    }
    
    private void connect(){
        try {
            logger.info("Connecting DB: " + db);
            receptor.getOutComm().connectDatabase(db, connectDb.isSelected());
        } catch(Exception e){
            e.printStackTrace();
            logger.error("Exception: " + e.getMessage());
        }
    }

    public void actionPerformed(ActionEvent e){
        if(db != null){
            Object o = e.getSource();

            if(o == apply){
                applySettings();
            } else if(o == openTerm){
                openTerminal();
            } else if(o == connectDb){
                connect();
            } else if(o == collectDb){
                collect();
            }
            
            connectDb.setSelected(db.isConnected());
            collectDb.setSelected(db.isCollecting());
        }
    }
    
    private void addControls(){
        JTabbedPane tabs = new JTabbedPane();
        JPanel analysisPanel = new JPanel();
        JPanel configurationPanel = new JPanel(new BorderLayout());
        JPanel namePanel = new JPanel(new BorderLayout());
        JPanel configPanel = new JPanel();
        JPanel configPanelHolder = new JPanel(new BorderLayout());
        JPanel serverConfig = new JPanel(new GridLayout(7, 2, 0, 5));
        JPanel statePanel = new JPanel(new GridLayout(2, 1, 0, 5));
        JPanel sourcesHolderPanel = new JPanel(new BorderLayout());
        JPanel recordersHolderPanel = new JPanel(new BorderLayout());
        JPanel buttonsPanel = new JPanel(new GridLayout(1, 2));
        JPanel execAnal = new JPanel(new BorderLayout());
        JPanel analReport = new JPanel(new BorderLayout());
        
        configPanel.setLayout(new BoxLayout(configPanel, BoxLayout.Y_AXIS));
        analysisPanel.setLayout(new BoxLayout(analysisPanel, BoxLayout.Y_AXIS));
        
        setLayout(new BorderLayout());
        
        serverConfig.add(new JLabel("Database Type:"));
        serverConfig.add(types = new JComboBox());
        serverConfig.add(new JLabel("Server Address:"));
        serverConfig.add(serverIP = new JTextField());
        serverConfig.add(new JLabel("Server Port:"));
        serverConfig.add(serverPort = new JTextField());
        serverConfig.add(new JLabel("User Name:"));
        serverConfig.add(username = new JTextField());
        serverConfig.add(new JLabel("Password:"));
        serverConfig.add(password = new JPasswordField());
        serverConfig.add(new JLabel("Database Name:"));
        serverConfig.add(dbName = new JTextField());
        serverConfig.add(new JLabel("Table Name:"));
        serverConfig.add(tableName = new JTextField());

        execAnal.add(dbAnalPanel = new DatabaseAnalysisPanel(receptor));
        analReport.add(dbReportsPanel = new DatabaseReportsPanel(receptor));
        
        namePanel.add(name = new JLabel(" "));
        configPanel.add(namePanel);
        configPanel.add(new JLabel(" "));
        configPanel.add(serverConfig);
        configPanel.add(statePanel);
        
        analysisPanel.add(execAnal);
        analysisPanel.add(analReport);
        
        configPanelHolder.add(configPanel, BorderLayout.NORTH);
        JScrollPane scroll = new JScrollPane(configPanelHolder);
        buttonsPanel.add(apply = new JButton("Apply changes"));
        buttonsPanel.add(openTerm = new JButton("Open Terminal"));
        
        statePanel.add(connectDb = new JCheckBox("Connect To Database"));
        statePanel.add(collectDb = new JCheckBox("Collect Flows"));
        
        configurationPanel.add(scroll);
        configurationPanel.add(buttonsPanel, BorderLayout.SOUTH);
        
        tabs.add("Configuration", configurationPanel);
        tabs.add("Analysis", analysisPanel);
        
        add(tabs);
        
        scroll.getVerticalScrollBar().setBlockIncrement(5);
        scroll.getVerticalScrollBar().setUnitIncrement(5);
        
        namePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Database Configuration Name"));
        serverConfig.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Server Configuration"));
        statePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Database State"));
        execAnal.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Analysis"));
        analReport.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Analysis Reports"));
        
        connectDb.addActionListener(this);
        collectDb.addActionListener(this);
        
        apply.addActionListener(this);
        openTerm.addActionListener(this);
        dbReportsPanel.setPreferredSize(new Dimension(100, 100));
    }
}