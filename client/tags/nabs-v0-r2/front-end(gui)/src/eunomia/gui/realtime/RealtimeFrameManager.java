/*
 * RealtimeFrame.java
 *
 * Created on June 8, 2005, 3:54 PM
 */

package eunomia.gui.realtime;

import javax.swing.*;
import java.util.*;

import eunomia.core.managers.listeners.*;
import eunomia.core.managers.*;
import eunomia.*;
import eunomia.core.receptor.*;
import eunomia.gui.FrameCreator;
import eunomia.gui.desktop.icon.DesktopItem;
import eunomia.gui.desktop.icon.IconGroup;

/**
 *
 * @author  Mikhail Sosonkin
 */

public class RealtimeFrameManager implements Exiter, ReceptorManagerListener {
    private IconGroup group; 
    private HashMap receptorToView;
    private HashMap viewToFrame;
    private HashMap itemToIcon;
    private FrameCreator creator;
            
    public RealtimeFrameManager(FrameCreator creator, IconGroup iGroup) {
        group = iGroup;
        this.creator = creator;
        receptorToView = new HashMap();
        viewToFrame = new HashMap();
        itemToIcon = new HashMap();
        group.setTitle("Receptors");
        
        iGroup.addItem(new DesktopItem(new AddReceptorIcon()));

        ReceptorManager.ins.addReceptorManagerListener(this);
    }
    
    public void loadReceptors(){
        Iterator it = ReceptorManager.ins.getReceptors().iterator();
        while(it.hasNext()){
            receptorAdded((Receptor)it.next());
        }
    }
    
    public IconGroup getGroup(){
        return group;
    }
    
    public void receptorAdded(Receptor rec) {
        ReceptorView rv = new ReceptorView(rec);
        JInternalFrame frame = creator.createInterfaceFrame();
        DesktopItem item = new DesktopItem(rv);
        
        rv.setReceptorFrame(frame);
        receptorToView.put(rec, rv);
        viewToFrame.put(rv, frame);
        itemToIcon.put(rv, item);
        group.addItem(item);
        
        frame.setTitle("Realtime View: " + rec);
        frame.setSize(900, 700);
        frame.setDefaultCloseOperation(JInternalFrame.HIDE_ON_CLOSE);
        frame.setContentPane(rv.getRealtimePanel());
    }
    
    public void receptorRemoved(Receptor rec) {
        ReceptorView rv = (ReceptorView)receptorToView.get(rec);
        
        group.removeItem((DesktopItem)itemToIcon.get(rv));
    }
    
    public void startExitSequence() {
    }
}