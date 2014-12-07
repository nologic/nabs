/*
 * NabInternalFrame.java
 *
 * Created on May 17, 2006, 9:22 PM
 *
 */

package eunomia.gui.desktop;

import java.awt.Component;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;
import javax.swing.plaf.basic.BasicInternalFrameUI;

/**
 *
 * @author Mikhail Sosonkin
 */
public class NabInternalFrame extends JInternalFrame {
    private JFrame frame;
    private JPopupMenu menu;
    private JMenuItem detach;
    private LinkedList listeners;
    private boolean isDetached;
    
    public NabInternalFrame() {
        this("");
    }
    
    public NabInternalFrame(String title) {
        super(title);
        
        listeners = new LinkedList();
        isDetached = false;
        
        createMenu();
        setMaximizable(true);
        setClosable(true);
        setIconifiable(true);
        setResizable(true);
        
        BasicInternalFrameUI ui = (BasicInternalFrameUI)getUI();
        if(ui != null && ui.getNorthPane() != null){
            ui.getNorthPane().addMouseListener(new MListener());
        }
    }
    
    public void addNabInternalFrameListener(NabInternalFrameListener l){
        listeners.add(l);
    }
    
    public void removeNabInternalFrameListener(NabInternalFrameListener l){
        listeners.remove(l);
    }
    
    private void fireTitleChanged(){
        if(listeners != null){
            Iterator it = listeners.iterator();
            while (it.hasNext()) {
                NabInternalFrameListener f = (NabInternalFrameListener) it.next();
                f.titleChange(this);
            }
        }
    }
    
    private void fireFrameDisplayed(){
        if(listeners != null){
            Iterator it = listeners.iterator();
            while (it.hasNext()) {
                NabInternalFrameListener f = (NabInternalFrameListener) it.next();
                f.frameDisplayed(this);
            }
        }
    }
    
    private void fireFrameHidden(){
        if(listeners != null){
            Iterator it = listeners.iterator();
            while (it.hasNext()) {
                NabInternalFrameListener f = (NabInternalFrameListener) it.next();
                f.frameHidden(this);
            }
        }
    }
    
    private void fireFrameMinimized(){
        if(listeners != null){
            Iterator it = listeners.iterator();
            while (it.hasNext()) {
                NabInternalFrameListener f = (NabInternalFrameListener) it.next();
                f.frameMinimized(this);
            }
        }
    }
    
    public void setTitle(String t){
        super.setTitle(t);
        fireTitleChanged();
    }
    
    private void createMenu(){
        menu = new JPopupMenu();
        
        detach = menu.add("Detach");
        
        menu.setComponentPopupMenu(menu);
        
        menu.setLightWeightPopupEnabled(false);
        detach.addActionListener(new AListener());
    }
    
    public void detach(){
        if(frame == null){
            frame = new JFrame();
            frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
            frame.setSize(getSize());
            frame.addWindowListener(new WListener());
            frame.setLocationRelativeTo(this);
            frame.setTitle(getTitle());
        }
        
        setVisible(false);
    
        Component c = getGlassPane();
        boolean v = c.isVisible();
        frame.setGlassPane(c);
        frame.setContentPane(getContentPane());
        
        c.setVisible(v);
        frame.setVisible(true);
        isDetached = true;
    }
    
    private void maybeShowPopup(MouseEvent e) {
        if (e.isPopupTrigger()) {
            menu.show(e.getComponent(), e.getX(), e.getY());
        }
    }
    
    public void setVisible(boolean b){
        if(isDetached){
            frame.toFront();
        } else {
            super.setVisible(b);
            if(b) {
                fireFrameDisplayed();
            } else {
                fireFrameHidden();
            }
        }
    }
    
    public void setSilentVisible(boolean b){
        super.setVisible(b);
    }
    
    public void setIcon(boolean b) {
        if(b){
            fireFrameMinimized();
        }
    }
    
    private class AListener implements ActionListener {
        public void actionPerformed(ActionEvent e){
            Object o = e.getSource();

            if(o == detach){
                detach();
            }
        }
    }
    
    private class MListener implements MouseListener {
        public void mousePressed(MouseEvent e) {
            maybeShowPopup(e);
        }

        public void mouseReleased(MouseEvent e) {
            maybeShowPopup(e);
        }

        public void mouseEntered(MouseEvent e) {
        }

        public void mouseExited(MouseEvent e){
        }

        public void mouseClicked(MouseEvent e){
        }
    }   
    private class WListener implements WindowListener {
        public void windowActivated(WindowEvent e) {
        }

        public void windowClosed(WindowEvent e) {
        }

        public void windowClosing(WindowEvent e) {
            isDetached = false;
            Component c = frame.getGlassPane();
            boolean v = c.isVisible();
            setGlassPane(c);
            setContentPane(frame.getContentPane());
            c.setVisible(v);
            setVisible(true);
            frame.setVisible(false);
        }

        public void windowDeactivated(WindowEvent e) {
        }

        public void windowDeiconified(WindowEvent e) {
        }

        public void windowIconified(WindowEvent e) {
        }

        public void windowOpened(WindowEvent e) {
        }
    }
}
