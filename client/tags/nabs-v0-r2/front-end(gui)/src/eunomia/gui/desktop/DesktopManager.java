/*
 * MainDesktopManager.java
 *
 * Created on July 7, 2005, 12:32 PM
 *
 */

package eunomia.gui.desktop;

import eunomia.gui.listeners.DesktopManagerListerner;
import java.awt.*;
import java.util.*;
import javax.swing.*;

/**
 *
 * @author Mikhail Sosonkin
 */
public class DesktopManager extends DefaultDesktopManager {
    private JDesktopPane dPane;
    private Dimension dim;
    
    private LinkedList listeners;
    
    public DesktopManager() {
        dim = new Dimension();
        listeners = new LinkedList();
    }
    
    public void addDesktopManagerListerner(DesktopManagerListerner l){
        listeners.add(l);
    }
    
    public void removeDesktopManagerListerner(DesktopManagerListerner l){
        listeners.remove(l);
    }
    
    private void fireFrameFocused(JInternalFrame f){
        Iterator it = listeners.iterator();
        
        while (it.hasNext()) {
            DesktopManagerListerner l = (DesktopManagerListerner) it.next();
            l.frameFocused(f);
        }
    }
    
    private void fireFrameOpened(JInternalFrame f){
        Iterator it = listeners.iterator();
        
        while (it.hasNext()) {
            DesktopManagerListerner l = (DesktopManagerListerner) it.next();
            l.frameOpened(f);
        }
    }

    private void fireFrameClosed(JInternalFrame f){
        Iterator it = listeners.iterator();
        
        while (it.hasNext()) {
            DesktopManagerListerner l = (DesktopManagerListerner) it.next();
            l.frameClosed(f);
        }
    }

    public void openFrame(JInternalFrame f) {
        super.openFrame(f);
        if(dPane == null){
            dPane = getDesktopPane(f);
        }
        ensureDesktopSize();
        fireFrameOpened(f);
    }
    
    public void closeFrame(JInternalFrame f) {
        super.closeFrame(f);
        if(dPane == null){
            dPane = getDesktopPane(f);
        }
        ensureDesktopSize();
        fireFrameClosed(f);
    }
    
    public void dragFrame(JComponent f, int newX, int newY) {
        super.dragFrame(f, newX, newY);
        if(dPane == null){
            dPane = getDesktopPane(f);
        }
        ensureDesktopSize();
    }
    
    public void activateFrame(JInternalFrame f) {
        super.activateFrame(f);
        if(dPane == null){
            dPane = getDesktopPane(f);
        }
        ensureDesktopSize();
        fireFrameFocused(f);
    }
    
    private void ensureDesktopSize(){
        if(dPane != null){
            int height = 0;
            int width = 0;

            JInternalFrame[] frames = dPane.getAllFrames();
            for(int i = 0; i < frames.length; i++){
                JInternalFrame f = frames[i];
                
                int tmpWidth = f.getWidth() + f.getX();
                if(width < tmpWidth){
                    width = tmpWidth;
                }

                int tmpHeight = f.getHeight() + f.getY();
                if(height < tmpHeight){
                    height = tmpHeight;
                }
            }
            dim.setSize(width, height);
            dPane.setPreferredSize(dim);
            dPane.revalidate();
        }
    }
    
    private JDesktopPane getDesktopPane( JComponent frame ) {
        JDesktopPane pane = null;
	Component c = frame.getParent();

        // Find the JDesktopPane
        while ( pane == null ) {
	    if ( c instanceof JDesktopPane ) {
	        pane = (JDesktopPane)c;
	    } else if ( c == null ) {
	        break;
	    } else {
	        c = c.getParent();
	    }
	}

	return pane;
    }
}