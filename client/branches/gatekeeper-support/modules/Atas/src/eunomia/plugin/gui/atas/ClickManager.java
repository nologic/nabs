/*
 * ClickManager.java
 *
 * Created on February 10, 2007, 2:18 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package eunomia.plugin.gui.atas;

import com.vivic.eunomia.module.EunomiaModule;
import eunomia.messages.receptor.ModuleHandle;
import com.vivic.eunomia.module.frontend.GUIModule;
import com.vivic.eunomia.sys.frontend.ConsoleModuleManager;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

/**
 *
 * @author SDR30011
 */
public class ClickManager implements ActionListener, PopupMenuListener{
    private AtasMainPanel panel;
    private JPopupMenu popupMenu;
    private JMenu hidden;
    private ConsoleModuleManager manager;
    private HashMap itemToHandle;
    private Host curHost;

    /** Creates a new instance of ClickManager */
    public ClickManager(AtasMainPanel p, ConsoleModuleManager manager) {
        this.manager = manager;
        panel = p;
        itemToHandle = new HashMap();
        popupMenu = new JPopupMenu();
        hidden = new JMenu("Unhide Set");
        hidden.addActionListener(this);
        popupMenu.add(hidden);
        popupMenu.addPopupMenuListener(this);
    }
    
    public JPopupMenu getPopupMenu() {
        return popupMenu;
    }
    
    public void mouseClicked(MouseEvent e) {
        hidden.removeAll();
    }
    
    private void makeUnhideMenu() {
        Object[] roles = panel.getRoles();
        hidden.removeAll();
        for(int i = 0; i < roles.length; i++) {
            Role role = (Role)roles[i];
            if( !role.getVisible() && role.getDisplayable()) {
                JMenuItem mi = new JMenuItem(role.getRoleName());
                mi.addActionListener(this);
                mi.setActionCommand("Show");
                hidden.add(mi);
            }
        }
        
        if(hidden.getItemCount() == 0) {
            JMenuItem mi = new JMenuItem("No hidden sets");
            mi.setEnabled(false);
            hidden.add(mi);
        }
        
        popupMenu.add(hidden);
    }
    
    public void makeHideMenu(String roleName) {
        JMenuItem mi = new JMenuItem("Hide this set");
        mi.addActionListener(this);
        mi.setActionCommand("Hide" + roleName);
        popupMenu.add(mi);
    }
    
    public void makeHostMenu(String hostInfo) {
        JMenuItem mi = new JMenuItem("Host information");
        mi.addActionListener(this);
        mi.setActionCommand("Host" + hostInfo);
        popupMenu.add(mi);
        popupMenu.addSeparator();
        
        itemToHandle.clear();
        List hvList = manager.getModuleHandles("hostDetails", ModuleHandle.TYPE_PROC);
        if(hvList.size() > 0){
            Iterator it = hvList.iterator();
            while (it.hasNext()) {
                ModuleHandle hand = (ModuleHandle) it.next();
                JMenuItem item = popupMenu.add("View in: " + hand.toString());
                itemToHandle.put(item, hand);
                item.addActionListener(this);
            }
        } else {
            popupMenu.add("No hostDetails instances found");
        }
    }
    
    public void actionPerformed(ActionEvent e) {
        JMenuItem item = (JMenuItem)e.getSource();
        if(item.getActionCommand().equals("Show")) {
            panel.setRoleVisible(item.getText(), true);
        } else if(item.getActionCommand().startsWith("Hide")) {
            panel.setRoleVisible(item.getActionCommand().substring(4), false);
        } else if(item.getActionCommand().startsWith("Host")) {
            String s = item.getActionCommand().substring(4);
            JOptionPane.showMessageDialog(null, s);
        } else if(itemToHandle.containsKey(item)) {
            ModuleHandle handle = (ModuleHandle)itemToHandle.get(item);
            EunomiaModule mod = manager.getEunomiaModule(handle);
            ((GUIModule)mod).setProperty("AH", Long.toString(curHost.getHostInfo().getIp()));
        }
    }

    public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
        Point panelPoint = panel.getLocationOnScreen();
        Point mousePoint = MouseInfo.getPointerInfo().getLocation();
        
        int x = (int)(mousePoint.getX() - panelPoint.getX());
        int y = (int)(mousePoint.getY() - panelPoint.getY());
        
        Role role = panel.getRoleAt(x, y);
        popupMenu.removeAll();

        if (role == null) {
            makeUnhideMenu();
        } else {
            Host host = role.getHostAt(x, y);
            if (host == null) {
                makeHideMenu(role.getRoleName());
            } else {
                curHost = host;
                makeHostMenu(host.toString());
            }
        }
    }

    public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
    }

    public void popupMenuCanceled(PopupMenuEvent e) {
    }
    
}
