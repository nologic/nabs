/*
 * DefaultDesktopIcon.java
 *
 * Created on August 12, 2006, 4:47 PM
 *
 */

package eunomia.gui.desktop.icon;

import eunomia.gui.desktop.interfaces.DesktopIcon;
import eunomia.gui.desktop.interfaces.DesktopIconListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JPopupMenu;

/**
 *
 * @author Mikhail Sosonkin
 */
public class DefaultDesktopIcon implements DesktopIcon {
    private List listeners;
    private ActionEvent actionEvent;
    private String name;
    private String tooltip;
    private JPopupMenu conMenu;
    private Icon icon;
    
    public DefaultDesktopIcon(){
        listeners = new LinkedList();
        actionEvent = new ActionEvent(this, hashCode(), "Icon Activated");
    }
    
    public void addActionListener(ActionListener l){
        listeners.add(l);
    }
    
    public void removeActionListener(ActionListener l){
        listeners.remove(l);
    }
    
    public void setName(String name) {
        this.name = name;
    }

    public void setTooltip(String tooltip) {
        this.tooltip = tooltip;
    }

    public void setContextMenu(JPopupMenu conMenu) {
        this.conMenu = conMenu;
    }
    
    public String getName() {
        return name;
    }

    public String getTooltip() {
        return tooltip;
    }

    public JPopupMenu getContextMenu() {
        return conMenu;
    }
    
    public void setIcon(Icon icon){
        this.icon = icon;
    }

    public Icon getIcon() {
        if(icon == null){
            icon = new ImageIcon("icons/applications-internet.png");
        }
        
        return icon;
    }

    public void activate() {
        Iterator it = listeners.iterator();
        while (it.hasNext()) {
            Object o = it.next();
            if(o instanceof ActionListener){
                ActionListener l = (ActionListener) o;
                l.actionPerformed(actionEvent);
            }
        }
    }

    public void addDesktopIconListener(DesktopIconListener l) {
        listeners.add(l);
    }

    public void removeDesktopIconListener(DesktopIconListener l) {
        listeners.remove(l);
    }
    
    public void iconChanged() {
        Iterator it = listeners.iterator();
        while (it.hasNext()) {
            Object o = it.next();
            if(o instanceof DesktopIconListener) {
                DesktopIconListener l = (DesktopIconListener) o;
                l.iconChanged(this);
            }
        }
    }
}