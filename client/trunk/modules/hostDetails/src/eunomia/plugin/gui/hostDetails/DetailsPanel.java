/*
 * DetailsFrame.java
 *
 * Created on August 25, 2005, 4:24 PM
 *
 */

package eunomia.plugin.gui.hostDetails;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.HashMap;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

/**
 *
 * @author Mikhail Sosonkin
 */

public class DetailsPanel extends JPanel implements ActionListener,
        WindowListener, ChangeListener, PopupMenuListener {
    
    private JTabbedPane tabs;
    private HashMap hostToPanel;
    private HashMap frameToPanel;
    private JMenuItem close, detach, add;
    private HostDetail curHost;
    private HostDetail clickedOn;
    private JPopupMenu menu;
    private JLabel instLabel;
    private Main main;
    
    public DetailsPanel(Main main) {
        this.main = main;
        hostToPanel = new HashMap();
        frameToPanel = new HashMap();
        
        addControls();
        addMenu();
    }
    
    public void setInstText(String str) {
        instLabel = new JLabel();
        
        instLabel.setText(str);
        instLabel.setVerticalAlignment(JLabel.CENTER);
        instLabel.setHorizontalAlignment(JLabel.CENTER);
        
        tabs.addTab("Welcome", new JScrollPane(instLabel));
    }
    
    public void stateChanged(ChangeEvent e){
        Object o = tabs.getSelectedComponent();
        if(o instanceof DetailedView) {
            DetailedView dView = (DetailedView)o;
            if(dView != null){
                curHost = dView.getHostDetail();
            }
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
            Object compo = hostToPanel.get(host);
            JFrame frame;
            
            if(compo instanceof JComponent) {
                JComponent comp = (JComponent)compo;
                tabs.remove(comp);

                frame = new JFrame(host.toString() + " - Detailed View");
                frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                frame.setSize(700, 500);

                Container c = frame.getContentPane();
                c.setLayout(new BorderLayout());
                c.add(comp);

                frame.addWindowListener(this);
                
                hostToPanel.put(host, frame);
                frameToPanel.put(frame, comp);
            } else {
                frame = (JFrame)compo;
            }
            
            frame.setVisible(true);
            
            if(tabs.getTabCount() == 0){
                curHost = null;
                return;
            }
            
            Object o = tabs.getComponentAt(0);
            if(!(o instanceof DetailedView)) {
                if(tabs.getTabCount() > 1) {
                    o = tabs.getComponentAt(1);
                }
            }
            
            if(o instanceof DetailedView) {
                DetailedView selView = (DetailedView)o;
                showHost(selView.getHostDetail());
            }
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
        } else if(o == add) {
            main.addHost(this);
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
        add = menu.add("Add Host");
        
        close.addActionListener(this);
        detach.addActionListener(this);
        add.addActionListener(this);
        
        tabs.setComponentPopupMenu(menu);
    }
    
    private void addControls(){
        setLayout(new BorderLayout());
        
        add(tabs = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT));
        tabs.addChangeListener(this);
    }
}