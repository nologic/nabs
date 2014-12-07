/*
 * DesktopManagerListerner.java
 *
 * Created on May 11, 2006, 12:26 AM
 */

package eunomia.gui.listeners;

import javax.swing.JInternalFrame;

/**
 *
 * @author Mikhail Sosonkin
 */
public interface DesktopManagerListerner {
    public void frameFocused(JInternalFrame f);
    public void frameOpened(JInternalFrame f);
    public void frameClosed(JInternalFrame f);
}