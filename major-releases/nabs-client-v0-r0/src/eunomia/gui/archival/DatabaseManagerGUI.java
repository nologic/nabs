/*
 * DatabaseManagerGUI.java
 *
 * Created on June 28, 2005, 6:16 PM
 *
 */

package eunomia.gui.archival;

import eunomia.*;
import eunomia.gui.*;
import eunomia.core.managers.*;
import eunomia.core.managers.listeners.*;
import eunomia.core.data.staticData.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import java.util.*;

/**
 *
 * @author Mikhail Sosonkin
 */
public class DatabaseManagerGUI extends JInternalFrame implements InternalFrameListener,
        Exiter, ListSelectionListener, ActionListener, DatabaseManagerListener {
    
    private HashMap dbToMenu;
    private HashMap menuToDb;
    private JSplitPane split;
    private JList list;
    private JButton add, remove;
    private DatabaseConfigurationPanel confPan;
    private TerminalManager termMan;
    private JMenu archMenu;
    
    public DatabaseManagerGUI(TerminalManager tMan) {
        super("Database Manager");
        
        termMan = tMan;
        dbToMenu = new HashMap();
        menuToDb = new HashMap();
        
        setSize(500, 400);
        setDefaultCloseOperation(JInternalFrame.HIDE_ON_CLOSE);
        setMaximizable(true);
        setResizable(true);
        setClosable(true);
        addInternalFrameListener(this);
        addControls();
        initMenu();
        
        DatabaseManager.ins.addDatabaseManagerListener(this);
        updateList();
        if(list.getModel().getSize() != 0){
            list.setSelectedIndex(0);
        }
    }
    
    private void initMenu(){
        archMenu = new JMenu("Analysis & Mining");
        addToMenu();
    }
    
    public JMenu getTerminalMenu(){
        return archMenu;
    }
    
    public void startExitSequence() {
        try {
            DatabaseManager.ins.saveDatabases();
        } catch(Exception e){
        }
    }

    private void updateList(){
        Object[] arr = DatabaseManager.ins.getDatabaseList().toArray();
        list.setListData(arr);
    }
    
    private void addToMenu(){
        Object[] arr = DatabaseManager.ins.getDatabaseList().toArray();
        for(int i = 0; i < arr.length; i++){
            addToMenu(arr[i]);
        }
    }
    
    private void addToMenu(Object db){
        JMenuItem item = new JMenuItem(db.toString());
        
        dbToMenu.put(db, item);
        menuToDb.put(item, db);
        archMenu.add(item);
        item.addActionListener(this);
    }
    
    private void removeFromMenu(Object db){
        Component item = (Component)dbToMenu.get(db);
        archMenu.remove(item);
        dbToMenu.remove(db);
        menuToDb.remove(item);
    }
    
    public void valueChanged(ListSelectionEvent e) {
        if(!e.getValueIsAdjusting()){
            Object sel = list.getSelectedValue();
            if(sel != null){
                confPan.setDatabase((Database)sel);
            }
        }
    }

    private void addDatabase(){
        String name = JOptionPane.showInputDialog(this, "Enter the name for the database");
        if(name != null){
            try {
                DatabaseManager.ins.createDefaultDatabase(name);
            } catch(Exception e){
                e.printStackTrace();
            }
        }
    }
    
    private void removeDatabase(){
        Database db = (Database)list.getSelectedValue();
        if(db != null && JOptionPane.showConfirmDialog(this, "The database \""+ db + "\" will be forever remove? \n(The data will not be removed by this process)") == JOptionPane.YES_OPTION){
            DatabaseManager.ins.removeDatabase(db);
        }
    }
    
    public void actionPerformed(ActionEvent e) {
        Object o = e.getSource();
        if(o == add){
            addDatabase();
        } else if(o == remove){
            removeDatabase();
        } else if(o instanceof JMenuItem){
            Database db = (Database)menuToDb.get(o);
            termMan.openTerminal(db);
        }
    }
    
    public void databaseAdded(Database db){
        updateList();
        list.setSelectedValue(db, true);
        //seems redundant but the event doesn't work for some reason.
        confPan.setDatabase(db);
        addToMenu(db);
    }
    
    public void databaseRemoved(Database db){
        removeFromMenu(db);
        updateList();
    }

    private void addControls(){
        JPanel mainPanel = new JPanel(new BorderLayout());
        JPanel buttonsPanel = new JPanel(new GridLayout(1, 2));
        Container c = getContentPane();
        split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        
        c.setLayout(new BorderLayout());
        
        buttonsPanel.add(add = new JButton("Add"));
        buttonsPanel.add(remove = new JButton("Remove"));
        mainPanel.add(new JLabel("Databases"), BorderLayout.NORTH);
        mainPanel.add(new JScrollPane(list = new JList()));
        mainPanel.add(buttonsPanel, BorderLayout.SOUTH);
        split.setLeftComponent(mainPanel);
        split.setRightComponent(confPan = new DatabaseConfigurationPanel(termMan));
        c.add(split);
        
        split.setDividerSize(4);
        split.setContinuousLayout(true);
        
        add.addActionListener(this);
        remove.addActionListener(this);
        remove.addMouseListener(MyMouseListener.ins);
        add.addMouseListener(MyMouseListener.ins);
        
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.addListSelectionListener(this);
    }

    public void internalFrameActivated(InternalFrameEvent e) {
    }

    public void internalFrameClosed(InternalFrameEvent e) {
    }

    public void internalFrameClosing(InternalFrameEvent e) {
    }

    public void internalFrameDeactivated(InternalFrameEvent e) {
    }

    public void internalFrameDeiconified(InternalFrameEvent e) {
    }

    public void internalFrameIconified(InternalFrameEvent e) {
    }

    public void internalFrameOpened(InternalFrameEvent e) {
        split.setDividerLocation(0.4);
    }
}