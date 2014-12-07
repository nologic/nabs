/*
 * RealtimeFrame.java
 *
 * Created on June 8, 2005, 3:54 PM
 */

package eunomia.gui.realtime;

import eunomia.core.managers.ReceptorManager;
import eunomia.core.managers.listeners.ReceptorManagerListener;
import eunomia.core.receptor.Receptor;
import eunomia.gui.MainGui;
import eunomia.gui.NABStrings;
import eunomia.gui.desktop.NabInternalFrame;
import eunomia.gui.desktop.icon.DesktopItem;
import eunomia.gui.desktop.icon.IconGroup;
import eunomia.gui.interfaces.Exiter;
import eunomia.gui.interfaces.FrameCreator;
import java.util.HashMap;
import java.util.Iterator;
import javax.swing.JInternalFrame;

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
        group.setTitle(NABStrings.CURRENT_RECEPTOR_NAME);
        
        iGroup.addItem(new DesktopItem(new AddReceptorIcon()));

        ReceptorManager.ins.addReceptorManagerListener(this);
        MainGui.v().addExiter(this);
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
        
        frame.setSize(900, 700);
        frame.setDefaultCloseOperation(JInternalFrame.HIDE_ON_CLOSE);
        frame.setContentPane(rv.getRealtimePanel());
    }
    
    public NabInternalFrame getReceptorFrame(Receptor r){
        ReceptorView view = (ReceptorView)receptorToView.get(r);
        if(view != null) {
            return (NabInternalFrame)viewToFrame.get(view);
        }
        
        return null;
    }
    
    public void receptorRemoved(Receptor rec) {
        ReceptorView rv = (ReceptorView)receptorToView.get(rec);
        
        group.removeItem((DesktopItem)itemToIcon.get(rv));
    }

    public void startExitSequence() {
        Iterator it = receptorToView.values().iterator();
        while (it.hasNext()) {
            ReceptorView rv = (ReceptorView) it.next();
            rv.getRealtimePanel().saveState();
        }
    }
}