/*
 * DatabaseConfigurationPanel.java
 *
 * Created on June 28, 2005, 6:24 PM
 *
 */

package eunomia.gui.archival;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

import eunomia.gui.*;
import eunomia.core.data.staticData.*;
import eunomia.core.managers.listeners.*;
import eunomia.core.managers.*;

import org.apache.log4j.*;

/**
 *
 * @author Mikhail Sosonkin
 */
public class DatabaseConfigurationPanel extends JPanel implements ActionListener {
    private JButton apply, openTerm;
    private JTextField serverIP, serverPort, username, password, dbName, tableName;
    private Database db;
    private JLabel name;
    private TerminalManager termMan;
    
    private static Logger logger;
    
    static {
        logger = Logger.getLogger(DatabaseConfigurationPanel.class);
    }
    
    public DatabaseConfigurationPanel(TerminalManager tMan) {
        termMan = tMan;
        addControls();
    }
    
    public void setDatabase(Database db){
        this.db = db;

        name.setText(db.getName());
        serverIP.setText(db.getAddress());
        serverPort.setText(Integer.toString(db.getPort()));
        username.setText(db.getUserName());
        password.setText(db.getPassword());
        dbName.setText(db.getDatabaseName());
        tableName.setText(db.getMainTable());
    }
    
    private void applySettings(){
        try {
            db.setAddress(serverIP.getText());
            db.setPort(Integer.parseInt(serverPort.getText()));
            db.setCredentials(username.getText(), password.getText());
            db.setDatabaseName(dbName.getText());
            db.setMainTable(tableName.getText());
            DatabaseManager.ins.saveDatabases();
        } catch(Exception e){
            e.printStackTrace();
            logger.error("Exception: " + e.getMessage());
        }
    }
    
    private void openTerminal(){
        termMan.openTerminal(db);
    }

    public void actionPerformed(ActionEvent e){
        Object o = e.getSource();
        
        if(o == apply){
            applySettings();
        } else if(o == openTerm){
            openTerminal();
        }
    }
    
    private void addControls(){
        JPanel namePanel = new JPanel(new BorderLayout());
        JPanel configPanel = new JPanel();
        JPanel configPanelHolder = new JPanel(new BorderLayout());
        JPanel serverConfig = new JPanel(new GridLayout(6, 2, 0, 5));
        JPanel statePanel = new JPanel(new GridLayout(3, 2, 0, 5));
        JPanel sourcesHolderPanel = new JPanel(new BorderLayout());
        JPanel recordersHolderPanel = new JPanel(new BorderLayout());
        JPanel buttonsPanel = new JPanel(new GridLayout(1, 2));
        
        configPanel.setLayout(new BoxLayout(configPanel, BoxLayout.Y_AXIS));
        setLayout(new BorderLayout());
        
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

        namePanel.add(name = new JLabel());
        configPanel.add(namePanel);
        configPanel.add(new JLabel(" "));
        configPanel.add(serverConfig);
        configPanel.add(statePanel);
        configPanel.add(new JLabel(" "));
        configPanelHolder.add(configPanel, BorderLayout.NORTH);
        JScrollPane scroll = new JScrollPane(configPanelHolder);
        buttonsPanel.add(apply = new JButton("Apply changes"));
        buttonsPanel.add(openTerm = new JButton("Open Terminal"));
        add(scroll);
        add(buttonsPanel, BorderLayout.SOUTH);
        scroll.getVerticalScrollBar().setBlockIncrement(5);
        scroll.getVerticalScrollBar().setUnitIncrement(5);
        
        namePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Database Configuration Name"));
        serverConfig.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Server Configuration"));
        statePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Database State"));
        
        apply.addActionListener(this);
        openTerm.addActionListener(this);
        openTerm.addMouseListener(MyMouseListener.ins);
        apply.addMouseListener(MyMouseListener.ins);
    }
}