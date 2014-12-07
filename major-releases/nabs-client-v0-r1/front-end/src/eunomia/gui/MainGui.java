/*
 * MainGui.java
 *
 * Created on May 31, 2005, 4:39 PM
 */

package eunomia.gui;

import java.awt.*;
import java.awt.event.*;
import java.net.InetAddress;
import javax.swing.*;
import java.util.*;
import java.io.*;

import eunomia.core.managers.*;
import eunomia.*;
import eunomia.config.Settings;
import eunomia.core.data.flow.Filter;
import eunomia.core.data.flow.FilterEntry;
import eunomia.gui.realtime.*;

import org.apache.log4j.*;
import eunomia.gui.archival.TerminalManager;
import eunomia.gui.archival.DatabaseManagerGUI;

/**
 *
 * @author  Mikhail Sosonkin
 */
public class MainGui extends JFrame implements WindowListener, ActionListener, Exiter,
                                               FrameCreator {
    private JDesktopPane desktop;
    private HashMap components;
    private JTextArea logPane;
    private JSplitPane split;
    private JMenuItem exit, streamer, archiver, streamMan, dbMan, settings, about;
    private TerminalManager termMan;
    private GlobalState gState;
    private About aboutDialog;
    private DatabaseManagerGUI dbManGUI;
    
    private static Logger logger;
    private static OutputDirector outDir;
    
    static {
        outDir = new OutputDirector();
        outDir.setTxtOptions(65536, 2048);
        Layout layout = new PatternLayout("%d{HH:mm:ss} %-5p: %m%n");
        WriterAppender wa = new WriterAppender(layout, outDir);
        BasicConfigurator.configure(wa);
        logger = Logger.getLogger(MainGui.class);
        
        logger.info("Start up");
        System.setOut(new PrintStream(outDir));
    }
    
    public MainGui() {
        super("NABS: Network Security Monitor");
        
        ToolTipManager.sharedInstance().setInitialDelay(150);
 
        gState = new GlobalState();
        //should be moved out of here.. temporary solution to loading everything.
        try {
            Class cls = Settings.class; 
            DatabaseManager.ins.loadDatabases();
            StreamManager.ins.addDefaultProcs(gState);
            StreamManager.ins.loadStreams();
            FilterEditor.initialize(this);
            FileChooser.setParent(this);
        } catch(Exception e){
            logger.error("Unable to initialize: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
        
        components = new HashMap();
        setSize(600, 800);
        setLocation(0, 0);
        try {
            setBounds(GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds());
        } catch(Exception e){
            e.printStackTrace();
        }
        addWindowListener(this);

        addControls();
        outDir.setTextArea(logPane);
        
        logger.info("Opening GUI");
        termMan = new TerminalManager(this);
        dbManGUI = new DatabaseManagerGUI(termMan);
        addMenu();
        initComponents();
        setVisible(true);
        
        DataManager.ins.registerWithUpdater(gState);
    }

    public JInternalFrame createInterfaceFrame() {
        JInternalFrame frame = new JInternalFrame();
        desktop.add(frame);
        
        frame.setClosable(true);
        frame.setResizable(true);
        frame.setMaximizable(true);
        
        return frame;
    }

    public void actionPerformed(ActionEvent e) {
        Object o = e.getSource();
        Object f;
        
        if(o == exit){
            startExitSequence();
        } else if(o == about){
            aboutDialog.setLocationRelativeTo(this);
            aboutDialog.setVisible(true);
        } else if( (f = components.get(o)) != null){
            JInternalFrame frame = (JInternalFrame)f;
            frame.setVisible(true);
            try {
                frame.setSelected(true);
            } catch(Exception ex){
            }
            frame.toFront();
        }
    }

    private void initComponents(){
        JInternalFrame frame;
        
        frame = new RealtimeFrame();
        desktop.add(frame);
        components.put(streamer, frame);

        frame = new StreamServerManager();
        desktop.add(frame);
        components.put(streamMan, frame);
        
        frame = dbManGUI;
        desktop.add(frame);
        components.put(dbMan, frame);

        frame = new WorldSettings();
        desktop.add(frame);
        components.put(settings, frame);
        
        aboutDialog = new About(this);
    }
    
    private void addMenu(){
        JMenuBar bar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        JMenu settingsMenu = new JMenu("Settings");
        JMenu monMenu = new JMenu("Monitoring");
        JMenu helpMenu = new JMenu("Help");
        
        exit = fileMenu.add("Exit");
        
        settings = settingsMenu.add("Color Settings");
        streamMan = settingsMenu.add("Receptor Settings");
        dbMan = settingsMenu.add("Database Settings");
        
        streamer = monMenu.add("Real-Time Views");
        monMenu.add(dbManGUI.getTerminalMenu());
        
        about = helpMenu.add("About");
        
        bar.add(fileMenu);
        bar.add(settingsMenu);
        bar.add(monMenu);
        bar.add(helpMenu);
        setJMenuBar(bar);
        
        streamMan.addActionListener(this);
        streamer.addActionListener(this);
        dbMan.addActionListener(this);
        exit.addActionListener(this);
        about.addActionListener(this);
        settings.addActionListener(this);
    }
    
    private void addControls(){
        Container c = getContentPane();
        JPanel buttomPanel = new JPanel(new BorderLayout());
        
        c.setLayout(new BorderLayout());
        split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true);
        split.setDividerSize(7);
        split.setOneTouchExpandable(true);
        
        buttomPanel.add(new JScrollPane(logPane = new JTextArea()));
        buttomPanel.add(gState, BorderLayout.EAST);
        gState.setPreferredSize(new Dimension(100, 100));
        split.setTopComponent(new JScrollPane(desktop = new JDesktopPane()));
        split.setBottomComponent(buttomPanel);
        c.add(split);
        
        logPane.setEditable(false);
        desktop.setDesktopManager(new MainDesktopManager());
    }
    
    public void startExitSequence(){
        logger.info("Shutting down components");
        Iterator it = components.values().iterator();
        while(it.hasNext()){
            ((Exiter)it.next()).startExitSequence();
        }
        
        logger.info("Terminating");
        System.exit(0);
    }
    
    public void windowIconified(WindowEvent e) {
    }
    
    public void windowOpened(WindowEvent e) {
        split.setDividerLocation(0.85);
        FilterEditor.setLocationRelative(this);
    }
    
    public void windowDeiconified(WindowEvent e) {
    }
    
    public void windowDeactivated(WindowEvent e) {
    }
    
    public void windowClosing(WindowEvent e) {
        startExitSequence();
    }
    
    public void windowClosed(WindowEvent e) {
    }
    
    public void windowActivated(WindowEvent e) {
    }

    public static void main(String[] arg){
        new MainGui();
    }
}