/*
 * DetailsFrame.java
 *
 * Created on August 25, 2005, 4:24 PM
 *
 */

package eunomia.plugin.hostView;

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

public class DetailsFrame extends JFrame implements RefreshNotifier, ActionListener,
        WindowListener, ChangeListener, PopupMenuListener {
    private JTabbedPane tabs;
    private HashMap hostToPanel;
    private HashMap frameToPanel;
    private JMenuItem close, detach;
    private InetAddress curHost;
    private InetAddress clickedOn;
    private JPopupMenu menu;
    
    public DetailsFrame() {
        super("Detailed Host View");
        
        hostToPanel = new HashMap();
        frameToPanel = new HashMap();
        
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        setSize(800, 600);
        
        addControls();
        addMenu();
    }
    
    public void stateChanged(ChangeEvent e){
        DetailedView dView = (DetailedView)tabs.getSelectedComponent();
        if(dView != null){
            curHost = dView.getHost();
        }
    }
    
    public void popupMenuWillBecomeVisible(PopupMenuEvent e){
        Point mousePoint = MouseInfo.getPointerInfo().getLocation();
        Point tabsPoint = tabs.getLocationOnScreen();
        int index = tabs.indexAtLocation(mousePoint.x - tabsPoint.x, mousePoint.y - tabsPoint.y);
        if(index != -1){
            DetailedView dView = (DetailedView)tabs.getComponentAt(index);
            clickedOn = dView.getHost();
        } else {
            clickedOn = null;
        }
    }

    public void popupMenuWillBecomeInvisible(PopupMenuEvent e){
    }

    public void popupMenuCanceled(PopupMenuEvent e){
    }
    
    public void showHost(InetAddress host, HostData hData) {
        if(!hostToPanel.containsKey(host)){
            DetailedView view = new DetailedView(host, hData);
            
            hostToPanel.put(host, view);
            tabs.addTab(host.getHostAddress(), view);
            tabs.setSelectedComponent(view);
            setVisible(true);
        } else {
            Object o = hostToPanel.get(host);
            if(o instanceof JFrame){
                JFrame frame = (JFrame)o;
                frame.setVisible(true);
            } else {
                setVisible(true);
                tabs.setSelectedComponent((Component)hostToPanel.get(host));
            }
        }
        curHost = host;
    }
    
    public void removeHost(InetAddress host){
        if(host != null){
            Object o = hostToPanel.remove(host);
            if(o instanceof JFrame){
                JFrame frame = (JFrame)o;
                frame.setVisible(true);
                frame.dispose();
            } else {
                tabs.remove((Component)o);
            }
        }
    }
    
    public void updateData(){
        Iterator it = hostToPanel.keySet().iterator();
        while(it.hasNext()){
            Object key = it.next();
            Object o = hostToPanel.get(key);
            DetailedView dv = null;
            
            if(o instanceof DetailedView){
                dv = (DetailedView)o;
            } else {
                dv = (DetailedView)frameToPanel.get(o);
            }
            
            if(dv != null){
                dv.refresh();
            }
        }
    }
    
    public void detach(InetAddress host){
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
            showHost(selView.getHost(), selView.getHostData());
        }
    }
    
    public void actionPerformed(ActionEvent e){
        Object o = e.getSource();
        
        InetAddress selectedHost = clickedOn;
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
        tabs.addTab(host.getHost().getHostAddress(), host);
        hostToPanel.put(host.getHost(), host);
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
        Container c = getContentPane();
        
        c.setLayout(new BorderLayout());
        
        c.add(tabs = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT));
        
        tabs.addChangeListener(this);
    }
}