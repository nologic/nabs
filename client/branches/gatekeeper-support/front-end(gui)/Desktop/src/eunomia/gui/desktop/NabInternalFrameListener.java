/*
 * NabInternalFrameListener.java
 *
 * Created on May 18, 2006, 10:09 PM
 *
 */

package eunomia.gui.desktop;

import javax.swing.event.InternalFrameListener;

/**
 *
 * @author Mikhail Sosonkin
 */
public interface NabInternalFrameListener {
    public void titleChange(NabInternalFrame f);
    public void frameDisplayed(NabInternalFrame f);
    public void frameHidden(NabInternalFrame f);
    public void frameMinimized(NabInternalFrame f);
}
