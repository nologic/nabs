/*
 * GlobalState.java
 *
 * Created on July 15, 2005, 10:29 AM
 *
 */

package eunomia.gui;

import eunomia.util.Util;
import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import javax.swing.border.*;

/**
 *
 * @author Mikhail Sosonkin
 */
public class GlobalState extends JPanel implements MouseListener, Runnable {
    private Border clickBorder;
    private Border overBorder;
    private Border regBorder;
    private Border previous;
    
    private Runtime rt;
    private JProgressBar bar;
    private boolean doRun;
    
    public GlobalState() {
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
    
    public void run(){
        while(doRun){
            Util.threadSleep(1000);
            long max = rt.totalMemory();
            int t = (int)(((double)max)/1048576.0);
            int u = (int)(((double)(max - rt.freeMemory()))/1048576.0);

            bar.setMaximum(t);
            bar.setValue(u);
            bar.setString(u + "/" + t + "MB");
        }
    }

    private JComponent createGraph(){
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(bar = new JProgressBar());
        
        //bar.setOrientation(JProgressBar.HORIZONTAL);
        bar.setStringPainted(true);
        bar.setMinimum(0);
        bar.addMouseListener(this);
        
        regBorder = BorderFactory.createLineBorder(Color.DARK_GRAY);
        overBorder = BorderFactory.createLineBorder(Color.BLUE);
        clickBorder = BorderFactory.createLineBorder(Color.GREEN);
        
        bar.setBorder(regBorder);
        
        return panel;
    }
    
    private void addControls(){
        setLayout(new BorderLayout());
        
        add(createGraph(), BorderLayout.SOUTH);
    }

    public void mouseClicked(MouseEvent e){
        System.gc();
    }
    
    public void mousePressed(MouseEvent e){
        previous = bar.getBorder();
        bar.setBorder(clickBorder);
    }

    public void mouseReleased(MouseEvent e){
        bar.setBorder(previous);
    }

    public void mouseEntered(MouseEvent e){
        previous = bar.getBorder();
        bar.setBorder(overBorder);
    }

    public void mouseExited(MouseEvent e){
        bar.setBorder(previous);
    }
}