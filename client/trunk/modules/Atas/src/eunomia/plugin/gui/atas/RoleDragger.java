/*
 * RoleDragger.java
 *
 * Created on February 9, 2007, 2:45 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package eunomia.plugin.gui.atas;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;


public class RoleDragger implements MouseListener, MouseMotionListener {
    private boolean inDrag;
    private Role dragRole;
    private Role prevRole;
    private AtasMainPanel panelToRefresh;
    
    private int origX;
    private int origY;
    
    private int diffX;
    private int diffY;
    
    public void mousePressed(MouseEvent e) {
        if(e.getModifiers() == MouseEvent.BUTTON3_MASK) 
            return;
        
        // Obtain mouse coordinates at time of press.
        origX = e.getX();
        origY = e.getY();
        
        inDrag = false;
        dragRole = panelToRefresh.getRoleAt(origX, origY);
        
        if(dragRole != null && !dragRole.getDraggable()) {
            dragRole = panelToRefresh.handleIntersectionDrag(dragRole, origX, origY);
            if(dragRole == null) 
                return;
        }
        
        if(dragRole == null) {
            return;
        }
        
        if(prevRole != null) {
            prevRole.setSelected(false);
        }
        
        prevRole = dragRole;
        prevRole.setSelected(true);
        panelToRefresh.repaint();
        
        diffX = origX - dragRole.getX();
        diffY = origY - dragRole.getY();
        inDrag = true;
        System.out.println("Dragging role: " + dragRole.getRoleName());
    }
    
    
    public void mouseDragged(MouseEvent e) {
        // Obtain mouse coordinates at time of press.
        int x = e.getX();
        int y = e.getY();
        
        if(panelToRefresh.isResizeRoleMode()) {
            Role r = panelToRefresh.getResizeRole();
            r.resize(x, y);
            panelToRefresh.repaint();
            inDrag = false;
        } else if(inDrag) {
            dragRole.setX(x - diffX);
            dragRole.setY(y - diffY);
            panelToRefresh.repaint();
        }
        
    }
    
    
    public void mouseReleased(MouseEvent e) {
        // clear inDrag to indicate no drag in progress
        if(inDrag) {
            inDrag = false;
            panelToRefresh.checkIntersections(dragRole);
        }
    }
    
    /** Creates a new instance of RoleDragger */
    public RoleDragger(AtasMainPanel panel) {
        panelToRefresh = panel;
    }
    
    public void mouseClicked(MouseEvent e) {}
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}
    public void mouseMoved(MouseEvent e) {
        panelToRefresh.mouseIsAt(e.getPoint());
    }//System.out.println(""+e.getX()+","+e.getY());}
}
