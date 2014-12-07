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
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

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
    private JCheckBox connectDb;
    private TerminalManager termMan;
    private CollectPanel cPanel;

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
    
    public void setDatabase(DatabaseDescriptor db, boolean isNew){
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
            connectDb.setEnabled(true);
            types.setSelectedItem(db.getDbType());
            cPanel.setList(receptor.getState().getCollectors().toArray());
            cPanel.setDatabaseDescriptor(db);
            if(isNew) {
                applySettings();
            }
        } else {
            name.setText("");
            serverIP.setText("");
            serverPort.setText("");
            username.setText("");
            password.setText("");
            dbName.setText("");
            tableName.setText("");
            connectDb.setSelected(false);
            connectDb.setEnabled(false);
        }
    }
    
    public DatabaseDescriptor getDatabase(){
        return db;
    }
    
    public void applySettings(){
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
            termMan.openTerminal(db, receptor);
        }
    }
    
    private void collect(String coll, boolean bool){
        if(db.isConnected()){
            receptor.getOutComm().collectDatabase(db, bool, coll);
        } else {
            logger.info("Must be connected in order to collect");
        }
    }
    
    private void connect(){
        logger.info("Connecting DB: " + db);
        boolean set = connectDb.isSelected();
        connectDb.setSelected(!set);
        receptor.getOutComm().connectDatabase(db, set);
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
            }
        }
    }
    
    private void addControls(){
        JPanel configurationPanel = new JPanel(new BorderLayout());
        JPanel namePanel = new JPanel(new BorderLayout());
        JPanel configPanel = new JPanel();
        JPanel configPanelHolder = new JPanel(new BorderLayout());
        JPanel serverConfig = new JPanel(new GridLayout(7, 2, 0, 5));
        JPanel statePanel = new JPanel(new GridLayout(1, 1, 0, 5));
        JPanel sourcesHolderPanel = new JPanel(new BorderLayout());
        JPanel recordersHolderPanel = new JPanel(new BorderLayout());
        JPanel buttonsPanel = new JPanel(new GridLayout(1, 2));
        JPanel execAnal = new JPanel(new BorderLayout());
        
        configPanel.setLayout(new BoxLayout(configPanel, BoxLayout.Y_AXIS));
        
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

        namePanel.add(name = new JLabel(" "));
        configPanel.add(namePanel);
        configPanel.add(new JLabel(" "));
        configPanel.add(serverConfig);
        configPanel.add(statePanel);
        configPanel.add(cPanel = new CollectPanel());
        
        configPanelHolder.add(configPanel, BorderLayout.NORTH);
        JScrollPane scroll = new JScrollPane(configPanelHolder);
        buttonsPanel.add(apply = new JButton("Apply changes"));
        buttonsPanel.add(openTerm = new JButton("Open Terminal"));
        
        statePanel.add(connectDb = new JCheckBox("Connect To Database"));
        
        configurationPanel.add(scroll);
        configurationPanel.add(buttonsPanel, BorderLayout.SOUTH);
        
        add(configurationPanel);
        
        scroll.getVerticalScrollBar().setBlockIncrement(5);
        scroll.getVerticalScrollBar().setUnitIncrement(5);
        
        namePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Database Configuration Name"));
        serverConfig.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Server Configuration"));
        statePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Database State"));
        cPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Collectors"));
        
        cPanel.setPreferredSize(new Dimension(0, 150));
        
        connectDb.addActionListener(this);
        
        apply.addActionListener(this);
        openTerm.addActionListener(this);
    }
    
    private class CollectPanel extends JPanel implements TableModel {
        private String[] colNames = new String[] {"Activate", "Collector"};
        private Class[] colClass = new Class[] {Boolean.class, String.class};
        
        private Object[] list;
        private DatabaseDescriptor db;
        private JTable table;
        private TableModelEvent event;
        
        public CollectPanel() {
            list = new String[0];
            event = new TableModelEvent(this);
            setLayout(new BorderLayout());
            
            add(new JScrollPane(table = new JTable(this)));
        }
        
        public void setList(Object[] l){
            list = l;
            table.tableChanged(event);
        }
        
        public void setDatabaseDescriptor(DatabaseDescriptor desc) {
            db = desc;
            table.tableChanged(event);
        }
        
        public int getRowCount() {
            return list.length;
        }

        public int getColumnCount() {
            return colNames.length;
        }

        public String getColumnName(int c) {
            return colNames[c];
        }

        public Class getColumnClass(int c) {
            return colClass[c];
        }

        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return columnIndex == 0;
        }

        public Object getValueAt(int rowIndex, int columnIndex) {
            switch(columnIndex) {
                case 0: return (db == null?Boolean.FALSE:Boolean.valueOf(db.getCollectors().contains(list[rowIndex])));
                case 1: return list[rowIndex];
            }
            
            return null;
        }

        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            if(aValue instanceof Boolean) {
                collect(list[rowIndex].toString(), ((Boolean)aValue).booleanValue());
            }
        }

        public void addTableModelListener(TableModelListener l) {
        }

        public void removeTableModelListener(TableModelListener l) {
        }
    }
}