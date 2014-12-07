/*
 * GlobalState.java
 *
 * Created on July 15, 2005, 10:29 AM
 *
 */

package eunomia.gui.desktop;

import eunomia.util.Util;
import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import javax.swing.border.*;

/**
 *
 * @author Mikhail Sosonkin
 */
public class MemoryBar extends JLabel implements MouseListener, Runnable {
    private Border clickBorder;
    private Border overBorder;
    private Border regBorder;
    private Border previous;
    
    private Runtime rt;
    private boolean doRun;
    
    private int total;
    private int taken;
    
    public MemoryBar() {
        rt = Runtime.getRuntime();
        
        addControls();
        
        Thread t = new Thread(this, "Global State");
        doRun = true;
        t.setPriority(Thread.MIN_PRIORITY);
        t.start();
    }
    
    public void terminate(){
        doRun = false;
    }
    
    public void paint(Graphics g) {
        double divide = (double)taken/(double)total;
        int length = (int)(this.getWidth() * divide);
        
        g.setColor(Color.LIGHT_GRAY);
        g.fillRect(2, 2, length - 2, getHeight() - 4);
        
        super.paint(g);
    }
    
    public void run(){
        while(doRun){
            Util.threadSleep(1000);
            long max = rt.totalMemory();
            total = (int)(((double)max)/1048576.0);
            taken = (int)(((double)(max - rt.freeMemory()))/1048576.0);

            setText(taken + "/" + total + "MB");
        }
    }

    private void addControls(){
        regBorder = BorderFactory.createLineBorder(Color.DARK_GRAY);
        overBorder = BorderFactory.createLineBorder(Color.BLUE);
        clickBorder = BorderFactory.createLineBorder(Color.GREEN);
        
        setPreferredSize(new Dimension(100, 0));
        setOpaque(false);
        addMouseListener(this);
        setHorizontalAlignment(JLabel.CENTER);
    }

    public void mouseClicked(MouseEvent e){
        System.gc();
    }
    
    public void mousePressed(MouseEvent e){
        previous = getBorder();
        setBorder(clickBorder);
    }

    public void mouseReleased(MouseEvent e){
        setBorder(previous);
    }

    public void mouseEntered(MouseEvent e){
        previous = getBorder();
        setBorder(overBorder);
    }

    public void mouseExited(MouseEvent e){
        setBorder(previous);
    }
}