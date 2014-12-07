/*
 * StreamConfigurationPanel.java
 *
 * Created on June 15, 2005, 5:13 PM
 */

package eunomia.gui.realtime;

import javax.swing.*;
import java.awt.*;
import java.text.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;

import eunomia.gui.*;
import eunomia.core.data.streamData.*;
import eunomia.core.managers.listeners.*;
import eunomia.core.managers.*;
import eunomia.core.data.staticData.*;

import org.apache.log4j.*;

/**
 *
 * @author  Mikhail Sosonkin
 */
public class StreamConfigurationPanel extends JPanel implements ActionListener, 
            StreamManagerListener, DatabaseManagerListener {
    private JButton apply;
    private JLabel name, isCollecting, isActive, isLossy;
    private JTextField serverIP, serverPort;
    private JCheckBox useServer;
    private HashMap checkToSources;
    private HashMap checkToRecorders;
    private HashMap sourcesToCheck;
    private HashMap recordersToCheck;
    private JPanel sourcesPanel;
    private JPanel recordersPanel;
    private StreamDataSource stream;
    private static Logger logger;
    
    static {
        logger = Logger.getLogger(StreamConfigurationPanel.class);
    }
    
    public StreamConfigurationPanel() {
        checkToSources = new HashMap();
        checkToRecorders = new HashMap();
        sourcesToCheck = new HashMap();
        recordersToCheck = new HashMap();
        addControls();
        
        StreamManager.ins.addStreamManagerListener(this);
        DatabaseManager.ins.addDatabaseManagerListener(this);
        
        Iterator it = StreamManager.ins.getStreamList().iterator();
        while(it.hasNext()){
            streamAddedNoFire((StreamDataSource)it.next());
        }
        updateSourcesPanel();
        
        it = DatabaseManager.ins.getDatabaseList().iterator();
        while(it.hasNext()){
            databaseAddedNoFire((Database)it.next());
        }
        updateRecordersPanel();
    }

    private void applySettings(){
        if(stream == null){
            return;
        }
        
        int port = 0;
        String ip = serverIP.getText();
        try {
            port = Integer.parseInt(serverPort.getText());
            stream.setServer(ip, port);
            StreamManager.ins.saveStreams();
        } catch(NumberFormatException e){
            logger.error("Unable to parse port string: " + e.getMessage());
        } catch(IOException e){
            logger.error(e.getMessage());
        }
    }
    
    private void updateSourcesPanel(){
        validate();
        repaint();
    }
    
    private void updateRecordersPanel(){
        validate();
        repaint();
    }
    
    private void streamAddedNoFire(StreamDataSource sds) {
        JCheckBox box = new JCheckBox(sds.toString());
        box.addActionListener(this);
        checkToSources.put(box, sds);
        sourcesToCheck.put(sds, box);
        sourcesPanel.add(box);
    }
    
    public void streamAdded(StreamDataSource sds) {
        streamAddedNoFire(sds);
        updateSourcesPanel();
    }
    
    public void streamRemoved(StreamDataSource sds) {
        JCheckBox box = (JCheckBox)sourcesToCheck.get(sds);
        box.removeActionListener(this);
        sourcesToCheck.remove(sds);
        checkToSources.remove(box);
        sourcesPanel.remove(box);
        updateSourcesPanel();
    }
    
    private void databaseAddedNoFire(Database db){
        JCheckBox box = new JCheckBox(db.toString());
        box.addActionListener(this);
        checkToRecorders.put(box, db);
        recordersToCheck.put(db, box);
        recordersPanel.add(box);
    }
    
    public void databaseAdded(Database db) {
        databaseAddedNoFire(db);
        updateRecordersPanel();
    }
    
    public void databaseRemovedNoFire(Database db) {
        JCheckBox box = (JCheckBox)recordersToCheck.get(db);
        box.removeActionListener(this);
        recordersToCheck.remove(db);
        checkToRecorders.remove(box);
        recordersPanel.remove(box);
    }

    public void databaseRemoved(Database db) {
        databaseRemovedNoFire(db);
        updateRecordersPanel();
    }
    
    public void actionPerformed(ActionEvent e) {
        Object o = e.getSource();
        
        if(o == apply){
            applySettings();
        } else if(o == useServer){
            boolean s = useServer.isSelected();
            stream.setUseServer(s);
            serverPort.setEnabled(s);
            serverIP.setEnabled(s);
        } else if(o instanceof JCheckBox){
            JCheckBox box = (JCheckBox)o;
            if(checkToSources.containsKey(o)){
                StreamDataSource sds = (StreamDataSource)checkToSources.get(o);
                if(box.isSelected()){
                    stream.addStream(sds);
                } else {
                    stream.removeStream(sds);
                }
            } else if(checkToRecorders.containsKey(o)){
                Database db = (Database)checkToRecorders.get(o);
                try {
                    if(box.isSelected()){
                        stream.addDatabase(db);
                    } else {
                        stream.removeDatabase(db);
                    }
                } catch (Exception ex){
                    logger.error("Unable to add change database status: " + ex.getMessage());
                }
            }
        }
    }

    public void setStreamSource(StreamDataSource sds){
        if(sds == null){
            return;
        }
                
        setEnabled(true);
        stream = sds;
        name.setText(sds.toString());
        resetCheckBoxes();
        
        Iterator it = sds.getOtherStreams().iterator();
        while(it.hasNext()){
            JCheckBox box = (JCheckBox)sourcesToCheck.get(it.next());
            if(box != null){
                box.setSelected(true);
            }
        }
        
        it = sds.getDatabaseList().iterator();
        while(it.hasNext()){
            JCheckBox box = (JCheckBox)recordersToCheck.get(it.next());
            if(box != null){
                box.setSelected(true);
            }
        }
        
        isCollecting.setText("" + sds.getCollect());
        isActive.setText("" + sds.getActive());
        serverIP.setText(sds.getIP());
        serverPort.setText(sds.getPort() + "");
        
        boolean s = sds.getUseServer();
        useServer.setSelected(s);
        serverPort.setEnabled(s);
        serverIP.setEnabled(s);
    }
    
    private void resetCheckBoxes(){
        Iterator it = sourcesToCheck.values().iterator();
        
        while(it.hasNext()){
            JCheckBox box = (JCheckBox)it.next();
            box.setSelected(false);
            box.setEnabled(true);
            if(checkToSources.get(box) == stream){
                box.setSelected(true);
                box.setEnabled(false);
            }
        }

        it = recordersToCheck.values().iterator();
        
        while(it.hasNext()){
            JCheckBox box = (JCheckBox)it.next();
            box.setSelected(false);
            box.setEnabled(true);
        }
    }
    
    private void addControls(){
        JPanel namePanel = new JPanel(new BorderLayout());
        JPanel configPanel = new JPanel();
        JPanel configPanelHolder = new JPanel(new BorderLayout());
        JPanel serverConfig = new JPanel(new GridLayout(3, 2, 0, 5));
        JPanel statePanel = new JPanel(new GridLayout(2, 2, 0, 5));
        JPanel sourcesHolderPanel = new JPanel(new BorderLayout());
        JPanel recordersHolderPanel = new JPanel(new BorderLayout());
        
        configPanel.setLayout(new BoxLayout(configPanel, BoxLayout.Y_AXIS));
        setLayout(new BorderLayout());
        
        statePanel.add(new JLabel("Active: "));
        statePanel.add(isActive = new JLabel());
        statePanel.add(new JLabel("Collecting: "));
        statePanel.add(isCollecting = new JLabel());
        
        serverConfig.add(useServer = new JCheckBox("User Server"));
        serverConfig.add(new JPanel());
        serverConfig.add(new JLabel("Server IP:"));
        serverConfig.add(serverIP = new JTextField());
        serverConfig.add(new JLabel("Server Port:"));
        serverConfig.add(serverPort = new JTextField());
        
        namePanel.add(name = new JLabel());
        sourcesHolderPanel.add(sourcesPanel = new JPanel());
        recordersHolderPanel.add(recordersPanel = new JPanel());
        configPanel.add(namePanel);
        configPanel.add(new JLabel(" "));
        configPanel.add(serverConfig);
        configPanel.add(statePanel);
        configPanel.add(new JLabel(" "));
        configPanel.add(sourcesHolderPanel);
        configPanel.add(recordersHolderPanel);
        configPanelHolder.add(configPanel, BorderLayout.NORTH);
        JScrollPane scroll = new JScrollPane(configPanelHolder);
        add(scroll);
        add(apply = new JButton("Apply changes"), BorderLayout.SOUTH);
        
        sourcesPanel.setLayout(new BoxLayout(sourcesPanel, BoxLayout.Y_AXIS));
        recordersPanel.setLayout(new BoxLayout(recordersPanel, BoxLayout.Y_AXIS));
        
        scroll.getVerticalScrollBar().setBlockIncrement(5);
        scroll.getVerticalScrollBar().setUnitIncrement(5);
        
        namePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Receptor Name"));
        sourcesPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Data Sources"));
        recordersPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Databases"));
        serverConfig.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Server Configuration"));
        statePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Receptor State"));
        
        useServer.addActionListener(this);
        apply.addActionListener(this);
        apply.addMouseListener(MyMouseListener.ins);
    }
}