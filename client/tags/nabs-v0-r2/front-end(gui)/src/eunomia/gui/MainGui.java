/*
 * MainGui.java
 *
 * Created on May 31, 2005, 4:39 PM
 */

package eunomia.gui;

import eunomia.Exiter;
import eunomia.NABStrings;
import eunomia.config.Settings;
import eunomia.core.managers.ReceptorManager;
import eunomia.core.receptor.Receptor;
import eunomia.gui.desktop.Desktop;
import eunomia.gui.desktop.NabInternalFrame;
import eunomia.gui.desktop.icon.DefaultDesktopIcon;
import eunomia.gui.desktop.icon.DesktopItem;
import eunomia.gui.desktop.icon.IconGroup;
import eunomia.gui.desktop.interfaces.DesktopIcon;
import eunomia.gui.filter.FilterEditor;
import eunomia.gui.realtime.RealtimeFrameManager;
import eunomia.gui.settings.WorldSettings;
import eunomia.shell.nabsh.Nabsh;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.ToolTipManager;
import javax.swing.border.Border;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Layout;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.WriterAppender;

/**
 *
 * @author  Mikhail Sosonkin
 */
public class MainGui extends JFrame implements WindowListener, ActionListener, Exiter,
                                               FrameCreator {
    public static final Font log_font = new Font("Monospaced", Font.PLAIN, 12);
    public static final Color log_color = new Color(255, 242, 223);
    public static final Color sh_text_color = Color.GREEN;
    public static final Color sh_color = Color.BLACK;
    public static final Color sh_pass_color = Color.GRAY;

    private Desktop desktop;
    private HashMap components;
    private LinkedList exiters;
    private JTextArea logPane;
    private JSplitPane split;
    private DesktopIcon settings, about;
    private RealtimeFrameManager rtMan;
    private About aboutDialog;
    private WorldSettings wSettings;

    private static Logger logger;
    private static OutputDirector outDir;
    private static MainGui ins;

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
        super(NABStrings.MAIN_WINDOW_TITLE);

        ins = this;

        ToolTipManager.sharedInstance().setInitialDelay(150);

        try {
            Class cls = Settings.class;
            FilterEditor.initialize(this);
            FileChooser.setParent(this);
            ReceptorManager.ins.load();
            rtMan = new RealtimeFrameManager(this, new IconGroup());
        } catch(Exception e){
            logger.error("Unable to initialize: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }

        components = new HashMap();
        exiters = new LinkedList();
        setSize(600, 800);
        setLocation(0, 0);
        try {
            setBounds(GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds());
        } catch(Exception e){
            e.printStackTrace();
        }
        addWindowListener(this);

        logger.info("Opening GUI");
        addControls();
        outDir.setTextArea(logPane);

        addMenu();
        initComponents();
        rtMan.loadReceptors();
        setVisible(true);
        desktop.addGroup("receptors", rtMan.getGroup());
    }

    public JInternalFrame createInterfaceFrame() {
        JInternalFrame frame = new NabInternalFrame();
        desktop.add(frame);

        frame.setClosable(true);
        frame.setResizable(true);
        frame.setMaximizable(true);

        return frame;
    }

    public void actionPerformed(ActionEvent e) {
        Object o = e.getSource();
        Object f;

        if(o == about){
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
    
    public void showSettingsForReceptor(Receptor rec) {
        wSettings.getReceptorServerManager().setShowReceptor(rec);
        JInternalFrame frame = (JInternalFrame)components.get(settings);
        frame.setVisible(true);
        try {
            frame.setSelected(true);
        } catch(Exception ex){
        }
        frame.toFront();
    }

    private void initComponents(){
        JInternalFrame frame;

        frame = wSettings = new WorldSettings();
        desktop.add(frame);
        components.put(settings, frame);
        addExiter((Exiter)frame);

        aboutDialog = new About(this);
    }

    private void addMenu(){
        DefaultDesktopIcon dIcon = new DefaultDesktopIcon();
        dIcon.setName("Settings");
        dIcon.addActionListener(this);
        dIcon.setIcon(IconResource.getSettingsIcon());
        settings = dIcon;

        dIcon = new DefaultDesktopIcon();
        dIcon.setName("About");
        dIcon.addActionListener(this);
        dIcon.setIcon(IconResource.getAboutIcon());
        about = dIcon;

        IconGroup group = desktop.createGroup("Settings");
        group.setTitle("Settings");

        group.addItem(new DesktopItem(settings));
        group.addItem(new DesktopItem(about));
    }

    private void addControls(){
        Container c = getContentPane();
        JPanel buttomPanel = new JPanel(new BorderLayout());
        JPanel topPanel = new JPanel(new BorderLayout());
        JPanel buttomRightPanel = new JPanel(new BorderLayout());
        JPanel shellEntryPanel = new JPanel(new BorderLayout());
        JScrollPane rtViewScroll;

        c.setLayout(new BorderLayout());
        split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true);
        split.setDividerSize(7);
        split.setOneTouchExpandable(true);

        buttomPanel.add(new JScrollPane(logPane = new JTextArea()));
        buttomPanel.add(buttomRightPanel, BorderLayout.EAST);
        topPanel.add(desktop = new Desktop());
        split.setTopComponent(topPanel);
        split.setBottomComponent(buttomPanel);
        shellEntryPanel.add(new Nabsh());
        //shellEntryPanel.add(new RWMeter(), BorderLayout.EAST);
        c.add(split);
        c.add(shellEntryPanel, BorderLayout.SOUTH);

        shellEntryPanel.setBackground(log_color);
        logPane.setEditable(false);
        Border empty = BorderFactory.createEmptyBorder();
        buttomPanel.setBorder(empty);
        buttomRightPanel.setBorder(empty);
        logPane.setBorder(empty);
        logPane.setFont(log_font);
        logPane.setBackground(log_color);
        desktop.setMemoryBar(true);
    }

    public void startExitSequence(){
        logger.info("Shutting down components");
        Iterator it = exiters.iterator();
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

    public void addExiter(Exiter exiter){
        exiters.add(exiter);
    }

    public void removeExiter(Exiter exiter){
        exiters.remove(exiter);
    }

    public static void main(String[] arg){
        new MainGui();
    }

    public static MainGui v(){
        return ins;
    }
}