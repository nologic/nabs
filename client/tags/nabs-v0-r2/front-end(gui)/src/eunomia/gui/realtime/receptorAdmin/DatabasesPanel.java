/*
 * DatabasesPanel.java
 *
 * Created on February 2, 2006, 6:00 PM
 *
 */

package eunomia.gui.realtime.receptorAdmin;
import eunomia.core.managers.listeners.ReceptorStateListener;
import eunomia.core.receptor.Receptor;
import eunomia.gui.archival.DatabaseConfigurationPanel;
import eunomia.gui.archival.TerminalManager;
import eunomia.messages.DatabaseDescriptor;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import org.apache.log4j.Logger;

/**
 *
 * @author Mikhail Sosonkin
 */
public class DatabasesPanel extends JPanel implements ListSelectionListener, ActionListener, ReceptorStateListener {
    private Receptor receptor;
    private JList list;
    private DatabaseConfigurationPanel dbPanel;
    private JButton addDb, removeDb;
    private static Logger logger;
    
    static {
        logger = Logger.getLogger(DatabasesPanel.class);
    }
    
    public DatabasesPanel(Receptor rec) {
        receptor = rec;
        
        addControls();
        
        list.addListSelectionListener(this);
        receptor.getState().addReceptorStateListener(this);
        dbPanel.setDatabase(null);
    }
    
    public void setTerminalManager(TerminalManager termMan){
        dbPanel.setTerminalManager(termMan);
    }
    
    public void update(){
        DatabaseDescriptor oldDb;
        
        oldDb = dbPanel.getDatabase();
        Object[] array = receptor.getState().getDatabases().toArray();
        list.setListData(array);
        if(oldDb == null){
            if(array.length > 0){
                dbPanel.setDatabase((DatabaseDescriptor)array[0]);
            }
        } else {
            dbPanel.setDatabase(receptor.getState().getDatabaseDescriptor(oldDb.getName()));
        }
    }
    
    public void valueChanged(ListSelectionEvent e) {
        if(!e.getValueIsAdjusting()){
            DatabaseDescriptor db = (DatabaseDescriptor)list.getSelectedValue();
            dbPanel.setDatabase(db);
        }
    }
    
    public void receptorStateChanged() {
        update();
    }
    
    public void actionPerformed(ActionEvent e){
        Object o = e.getSource();
        
        if(o == addDb){
            addDatabase();
        }
    }
    
    private void addDatabase(){
        String name;
        DatabaseDescriptor db;
        
        name = JOptionPane.showInputDialog(this, "Enter Database Configution Name");
        if(name != null){
            db = new DatabaseDescriptor();
            db.setName(name);
            db.setDbType("");
            db.setAddress("");
            db.setPort(0);
            db.setUsername("");
            db.setPassword("");
            db.setDbName("");
            db.setTableName("");
            dbPanel.setDatabase(db);
        }
    }

    private void addControls(){
        JSplitPane sPane = new JSplitPane();
        JPanel leftSide = new JPanel(new BorderLayout());
        JPanel buttonsPanel = new JPanel(new GridLayout(2, 1));
        
        setLayout(new BorderLayout());
        
        leftSide.add(new JLabel("Database Listing"), BorderLayout.NORTH);
        leftSide.add(new JScrollPane(list = new JList()));
        buttonsPanel.add(addDb = new JButton("Add New Database"));
        buttonsPanel.add(removeDb = new JButton("Remove Database"));
        leftSide.add(buttonsPanel, BorderLayout.SOUTH);
        sPane.setLeftComponent(leftSide);
        sPane.setRightComponent(dbPanel = new DatabaseConfigurationPanel(receptor));
        add(sPane);
        
        sPane.setContinuousLayout(true);
        
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        removeDb.addActionListener(this);
        addDb.addActionListener(this);
    }
}