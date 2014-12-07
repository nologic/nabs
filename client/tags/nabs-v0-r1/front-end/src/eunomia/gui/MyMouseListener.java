package eunomia.gui;

import java.awt.event.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;

public class MyMouseListener implements MouseListener {
    public static MyMouseListener ins = new MyMouseListener();
    
    private Color buttonC, buttonB;

    private MyMouseListener(){
    }
    
    public void mouseClicked(MouseEvent e) {
        Object o = e.getSource();
    }
    
    public void mouseEntered(MouseEvent e) {
        Object o = e.getSource();
        if(o instanceof JButton){
            JButton button = (JButton)o;
            buttonC = button.getForeground();
            buttonB = button.getBackground();
            button.setForeground(Color.orange);
            button.setBackground(Color.darkGray);
        }
    }
    
    public void mouseExited(MouseEvent e) {
        Object o = e.getSource();
        if(o instanceof JButton){
            JButton button = (JButton)o;
            button.setForeground(buttonC);
            button.setBackground(buttonB);
        }
    }
    
    public void mousePressed(MouseEvent e) {
    }
    
    public void mouseReleased(MouseEvent e) {
    }    
}