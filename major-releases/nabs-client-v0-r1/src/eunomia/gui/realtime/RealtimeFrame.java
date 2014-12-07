/*
 * RealtimeFrame.java
 *
 * Created on June 8, 2005, 3:54 PM
 */

package eunomia.gui.realtime;

import javax.swing.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;

import eunomia.core.managers.listeners.*;
import eunomia.core.managers.*;
import eunomia.core.data.streamData.*;
import eunomia.*;
import javax.swing.event.*;

/**
 *
 * @author  Mikhail Sosonkin
 */

public class RealtimeFrame extends JInternalFrame implements Exiter, 
        StreamManagerListener, ActionListener, WindowListener {
    private JPanel streamsView;
    private JTabbedPane tabContainer;
    private HashMap streamToPanel;
    private JMenuItem detach;
    
    public RealtimeFrame() {
        super("Realtime Views");
        
        streamToPanel = new HashMap();
        
        setSize(700, 700);
        setDefaultCloseOperation(JInternalFrame.HIDE_ON_CLOSE);
        setMaximizable(true);
        setResizable(true);
        setClosable(true);
        addMenu();
        addControls();
        
        StreamManager.ins.addStreamManagerListener(this);
        
        Iterator it = StreamManager.ins.getStreamList().iterator();
        while(it.hasNext()){
            streamAddedNoFire((StreamDataSource)it.next());
        }
    }
    
    private void addMenu(){
        JMenuBar bar = new JMenuBar();
        JMenu options = new JMenu("Options");
        
        detach = options.add("Detach Current View");
        
        bar.add(options);
        setJMenuBar(bar);
        
        detach.addActionListener(this);
    }
    
    private void showStream(StreamDataSource sds){
        StreamView sv = (StreamView)streamToPanel.get(sds);
        tabContainer.addTab(sds.toString(), sv.getRealtimePanel());
    }
    
    private void streamAddedNoFire(StreamDataSource sds) {
        StreamView sv = new StreamView(sds);
        
        streamToPanel.put(sds, sv);
        streamsView.add(sv);
        
        showStream(sds);
    }
    
    public void streamAdded(StreamDataSource sds) {
        streamAddedNoFire(sds);
        tabContainer.validate();
        tabContainer.repaint();
    }
    
    public void streamRemoved(StreamDataSource sds) {
        StreamView sv = (StreamView)streamToPanel.get(sds);
        streamsView.remove(sv);
        tabContainer.remove(sv.getRealtimePanel());
        tabContainer.validate();
        tabContainer.repaint();
    }
        
    private void addControls(){
        JPanel streamsViewHolder = new JPanel(new BorderLayout());
        tabContainer = new JTabbedPane(JTabbedPane.TOP);
        
        Container c = getContentPane();
        
        streamsView = new JPanel();
        BoxLayout layout = new BoxLayout(streamsView, BoxLayout.Y_AXIS);
        streamsView.setLayout(layout);
        
        streamsViewHolder.add(streamsView, BorderLayout.NORTH);
        tabContainer.addTab("Overview", new JScrollPane(streamsViewHolder));
        c.add(tabContainer);
    }
    
    public void startExitSequence() {
    }
    
    private void detachCurrent(){
        if(tabContainer.getSelectedIndex() != 0){
            RealtimePanel rt = (RealtimePanel)tabContainer.getSelectedComponent();
            JFrame dFrame = rt.getDetachmentFrame();
            if(dFrame == null){
                dFrame = new JFrame("Receptor: " + rt.getStreamDataSource().toString());
                dFrame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
                dFrame.setSize(700, 700);
                dFrame.addWindowListener(this);
                rt.setDetachmentFrame(dFrame);
            }
            tabContainer.remove(rt);
            dFrame.setContentPane(rt);
            dFrame.setVisible(true);
        }
    }
    
    public void actionPerformed(ActionEvent e) {
        Object o = e.getSource();
        
        if(o == detach){
            detachCurrent();
        }
    }

    public void windowActivated(WindowEvent e) {
    }

    public void windowClosed(WindowEvent e) {
    }

    public void windowClosing(WindowEvent e) {
        JFrame frame = (JFrame)e.getWindow();
        RealtimePanel rt = (RealtimePanel)frame.getContentPane();
        tabContainer.add(rt.getStreamDataSource().toString(), rt);
    }

    public void windowDeactivated(WindowEvent e) {
    }

    public void windowDeiconified(WindowEvent e) {
    }

    public void windowIconified(WindowEvent e) {
    }

    public void windowOpened(WindowEvent e) {
    }
}