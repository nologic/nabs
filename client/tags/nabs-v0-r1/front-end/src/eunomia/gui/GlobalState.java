/*
 * GlobalState.java
 *
 * Created on July 15, 2005, 10:29 AM
 *
 */

package eunomia.gui;
import eunomia.core.data.streamData.client.listeners.*;
import eunomia.plugin.interfaces.*;
import eunomia.plugin.streamStatus.*;
import eunomia.core.data.flow.*;

import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import javax.swing.border.*;

/**
 *
 * @author Mikhail Sosonkin
 */
public class GlobalState extends JPanel implements RefreshNotifier, FlowProcessor, MouseListener {
    private Border clickBorder;
    private Border overBorder;
    private Border regBorder;
    private Border previous;
    
    private Runtime rt;
    private JProgressBar bar;
    private Module statMod;
    private FlowProcessor proc;
    private RefreshNotifier ref;
    
    public GlobalState() {
        rt = Runtime.getRuntime();
        statMod = new StreamStatusModule();
        statMod.setProperty("Orientation", "0");
        
        proc = statMod.getFlowPocessor();
        ref = statMod.getRefreshNotifier();
        
        addControls();
    }
    
    public void updateData(){
        long max = rt.totalMemory();
        int t = (int)(((double)max)/1048576.0);
        int u = (int)(((double)(max - rt.freeMemory()))/1048576.0);
        
        bar.setMaximum(t);
        bar.setValue(u);
        bar.setString(u + "/" + t + "MB");
        
        ref.updateData();
    }
    
    public void newFlow(Flow flow){
        proc.newFlow(flow);
    }

    private JComponent createGraph(){
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(bar = new JProgressBar());
        
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
        
        add(statMod.getJComponent(), BorderLayout.NORTH);
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

    public void setFilter(Filter filter) {
    }

    public Filter getFilter() {
        return null;
    }
}