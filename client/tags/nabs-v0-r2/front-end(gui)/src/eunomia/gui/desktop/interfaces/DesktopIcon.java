/*
 * DesktopIcon.java
 *
 * Created on August 12, 2006, 12:02 AM
 *
 */

package eunomia.gui.desktop.interfaces;

import javax.swing.Icon;
import javax.swing.JPopupMenu;

/**
 *
 * @author Mikhail Sosonkin
 */
public interface DesktopIcon {
    public String getName();
    public String getTooltip();
    public JPopupMenu getContextMenu();
    public Icon getIcon();
    public void activate();
    
    public void addDesktopIconListener(DesktopIconListener l);
    public void removeDesktopIconListener(DesktopIconListener l);
}