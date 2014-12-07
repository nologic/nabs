/*
 * DesktopTaskBar.java
 *
 * Created on May 11, 2006, 12:20 AM
 *
 */

package eunomia.gui.desktop;

import eunomia.gui.listeners.DesktopManagerListerner;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;

/**
 *
 * @author Mikhail Sosonkin
 */
public class TaskBar extends JPanel implements DesktopManagerListerner, ActionListener, NabInternalFrameListener {
    private static Dimension buttonSize;
    
    private JDesktopPane dt;
    private HashMap frameToButton;
    private HashMap buttonToFrame;
    private JToggleButton current;
    private JPanel contents;
    
    static {
        buttonSize = new Dimension(160, 19);
    }
    
    public TaskBar() {
        frameToButton = new HashMap();
        buttonToFrame = new HashMap();
        
        contents = new JPanel();
        setLayout(new BorderLayout());
        JScrollPane scroll;
        add(scroll = new JScrollPane(contents));
        contents.setLayout(new WrappingFlowLayout(scroll, false));
        
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        setPreferredSize(new Dimension(1, 27));
    }
    
    public void setDesktop(JDesktopPane desk){
        dt = desk;
    }

    private JToggleButton getNewButton(String name){
        JToggleButton button = new JToggleButton(name);

        button.addActionListener(this);
        button.setSelected(false);
        button.setPreferredSize(buttonSize);
        button.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        
        return button;
    }

    public void actionPerformed(ActionEvent e) {
        Object o = e.getSource();
        
        if(o instanceof JToggleButton){
            JInternalFrame f = (JInternalFrame)buttonToFrame.get(o);
            f.show();
            f.toFront();
            openFrame(f);
        }
    }

    public void titleChange(NabInternalFrame f) {
        JToggleButton button = (JToggleButton)frameToButton.get(f);
        if(button != null){
            button.setText(f.getTitle());
        }
    }
    
    public void frameDisplayed(NabInternalFrame f) {
        openFrame(f);
    }
    
    public void frameHidden(NabInternalFrame f) {
        closeFrame(f);
    }

    public Dimension getButtonSize() {
        return buttonSize;
    }
    
    private void closeFrame(JInternalFrame f){
        JToggleButton button = (JToggleButton)frameToButton.remove(f);
        if(button != null){
            buttonToFrame.remove(button);
            contents.remove(button);
            validate();
            repaint();
        }
    }
    
    private void openFrame(JInternalFrame f){
        JToggleButton button = (JToggleButton)frameToButton.get(f);
        if(button == null){
            button = getNewButton(f.getTitle());
            
            button.setIcon(f.getFrameIcon());

            frameToButton.put(f, button);
            buttonToFrame.put(button, f);

            contents.add(button);
            validate();
            repaint();
        } else {
            if(current != null){
                current.setSelected(false);
            }
            
            current = button;
            current.setSelected(true);
        }
    }

    public void frameFocused(JInternalFrame f) {
        openFrame(f);
    }
    
    public void frameOpened(JInternalFrame f) {
    }
    
    public void frameClosed(JInternalFrame f) {
    }

    public void internalFrameOpened(InternalFrameEvent e) {
    }

    public void frameMinimized(NabInternalFrame f) {
        f.setSilentVisible(false);
        if(current != null){
            current.setSelected(false);
        }
        current = null;
    }
}