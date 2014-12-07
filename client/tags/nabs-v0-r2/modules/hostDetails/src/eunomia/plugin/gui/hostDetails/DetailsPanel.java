/*
 * DetailsFrame.java
 *
 * Created on August 25, 2005, 4:24 PM
 *
 */

package eunomia.plugin.gui.hostDetails;

import eunomia.plugin.interfaces.*;

import java.awt.*;
import java.awt.event.*;
import java.net.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;


/**
 *
 * @author Mikhail Sosonkin
 */

public class DetailsPanel extends JPanel implements ActionListener,
        WindowListener, ChangeListener, PopupMenuListener {
    private JTabbedPane tabs;
    private HashMap hostToPanel;
    private HashMap frameToPanel;
    private JMenuItem close, detach;
    private HostDetail curHost;
    private HostDetail clickedOn;
    private JPopupMenu menu;
    
    public DetailsPanel() {
        hostToPanel = new HashMap();
        frameToPanel = new HashMap();
        
        addControls();
        addMenu();
    }
    
    public void stateChanged(ChangeEvent e){
        DetailedView dView = (DetailedView)tabs.getSelectedComponent();
        if(dView != null){
            curHost = dView.getHostDetail();
        }
    }
    
    public void popupMenuWillBecomeVisible(PopupMenuEvent e){
        Point mousePoint = MouseInfo.getPointerInfo().getLocation();
        Point tabsPoint = tabs.getLocationOnScreen();
        int index = tabs.indexAtLocation(mousePoint.x - tabsPoint.x, mousePoint.y - tabsPoint.y);
        if(index != -1){
            DetailedView dView = (DetailedView)tabs.getComponentAt(index);
            clickedOn = dView.getHostDetail();
        } else {
            clickedOn = null;
        }
    }

    public void popupMenuWillBecomeInvisible(PopupMenuEvent e){
    }

    public void popupMenuCanceled(PopupMenuEvent e){
    }
    
    public void showHost(HostDetail host) {
        if(!hostToPanel.containsKey(host)){
            DetailedView view = host.getDetailedPanel();
            
            hostToPanel.put(host, view);
            tabs.addTab(view.getAddress().getHostAddress(), view);
            tabs.setSelectedComponent(view);
        } else {
            Object o = hostToPanel.get(host);
            if(o instanceof JFrame){
                JFrame frame = (JFrame)o;
                frame.setVisible(true);
            } else {
                tabs.setSelectedComponent((Component)hostToPanel.get(host));
            }
        }
        curHost = host;
    }
    
    public void removeHost(HostDetail host){
        if(host != null){
            Object o = hostToPanel.remove(host);
            if(o instanceof JFrame){
                JFrame frame = (JFrame)o;
                frame.setVisible(true);
                frame.dispose();
            } else {
                tabs.remove(host.getDetailedPanel());
            }
        }
    }
    
    public void detach(HostDetail host){
        if(hostToPanel.containsKey(host)){
            JComponent comp = (JComponent)hostToPanel.get(host);
            tabs.remove(comp);
            
            JFrame frame = new JFrame(host.toString() + " - Detailed View");
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.setSize(700, 500);
            
            Container c = frame.getContentPane();
            c.setLayout(new BorderLayout());
            c.add(comp);
            
            frame.setVisible(true);
            frame.addWindowListener(this);
            
            hostToPanel.put(host, frame);
            frameToPanel.put(frame, comp);
            
            if(tabs.getTabCount() == 0){
                curHost = null;
                return;
            }
            
            DetailedView selView = (DetailedView)tabs.getComponentAt(0);
            showHost(selView.getHostDetail());
        }
    }
    
    public void actionPerformed(ActionEvent e){
        Object o = e.getSource();
        
        HostDetail selectedHost = clickedOn;
        if(selectedHost == null){
            selectedHost = curHost;
        }
        
        if(o == close){
            removeHost(selectedHost);
        } else if(o == detach){
            detach(selectedHost);
        }
    }
    
    public void windowIconified(WindowEvent e) {
    }
    
    public void windowOpened(WindowEvent e) {
    }
    
    public void windowDeiconified(WindowEvent e) {
    }
    
    public void windowDeactivated(WindowEvent e) {
    }
    
    public void windowClosing(WindowEvent e) {
    }
    
    public void windowClosed(WindowEvent e) {
        DetailedView host = (DetailedView)frameToPanel.remove(e.getWindow());
        tabs.addTab(host.getAddress().getHostAddress(), host);
        hostToPanel.put(host.getHostDetail(), host);
    }
    
    public void windowActivated(WindowEvent e) {
    }
    
    private void addMenu(){
        menu = new JPopupMenu();
        menu.addPopupMenuListener(this);
        
        detach = menu.add("Detach");
        close = menu.add("Remove Host");
        
        close.addActionListener(this);
        detach.addActionListener(this);
        
        tabs.setComponentPopupMenu(menu);
    }
    
    private void addControls(){
        setLayout(new BorderLayout());
        
        add(tabs = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT));
        
        tabs.addChangeListener(this);
    }
}