/*
 * MainDesktopManager.java
 *
 * Created on July 7, 2005, 12:32 PM
 *
 */

package eunomia.gui;

import java.awt.*;
import javax.swing.*;

/**
 *
 * @author Mikhail Sosonkin
 */
public class MainDesktopManager extends DefaultDesktopManager {
    private JDesktopPane dPane;
    private Dimension dim;
    
    public MainDesktopManager() {
        dim = new Dimension();
    }
    
    public void openFrame(JInternalFrame f) {
        super.openFrame(f);
        if(dPane == null){
            dPane = getDesktopPane(f);
        }
        ensureDesktopSize();
    }
    
    public void closeFrame(JInternalFrame f) {
        super.closeFrame(f);
        if(dPane == null){
            dPane = getDesktopPane(f);
        }
        ensureDesktopSize();
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