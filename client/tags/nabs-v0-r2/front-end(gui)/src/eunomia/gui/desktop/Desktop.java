/*
 * Desktop.java
 *
 * Created on May 11, 2006, 11:47 PM
 *
 */

package eunomia.gui.desktop;

import eunomia.gui.GlobalState;
import eunomia.gui.desktop.icon.DesktopFocusManager;
import eunomia.gui.desktop.icon.IconGroup;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.HashMap;
import java.util.Iterator;
import javax.swing.*;

/**
 *
 * @author Mikhail Sosonkin
 */
public class Desktop extends JPanel {
    private JDesktopPane dt;
    private DesktopManager man;
    private TaskBar bar;
    private JPanel bottom;
    private JComponent sideComp;
    private HashMap nameToGroup;
    private GlobalState state;
    
    public Desktop() {
        nameToGroup = new HashMap();
        addControls();
    }
    
    public void setSideComponent(JComponent c){
        if(sideComp != null){
            bottom.remove(sideComp);
            bottom.revalidate();
        }
        
        if(c != null){
            sideComp = c;
            bottom.add(sideComp, BorderLayout.EAST);
            bar.setPreferredSize(sideComp.getPreferredSize());
        }
    }
    
    public IconGroup getGroup(String name){
        return (IconGroup)nameToGroup.get(name);
    }
    
    public void addGroup(String name, IconGroup group){
        group.setFocusManager(new DesktopFocusManager());
        dt.add(group);

        int x = 10;
        int y = 10;
        Iterator it = nameToGroup.values().iterator();
        while (it.hasNext()) {
            IconGroup grp = (IconGroup) it.next();
            x += (grp.getX() + grp.getWidth());
        }
        
        group.setLocation(x, y);
        nameToGroup.put(name, group);
    }
    
    public IconGroup createGroup(String name){
        IconGroup group = getGroup(name);
        
        if(group == null){
            group = new IconGroup();
            addGroup(name, group);
        } else {
            group = null;
        }
        
        return group;
    }
    
    public void add(JComponent c){
        if(c instanceof NabInternalFrame){
            NabInternalFrame f = (NabInternalFrame)c;
            dt.add(c);
            f.addNabInternalFrameListener(bar);
            
            //HACK: Desktop Manager doesn't get called on add.
            bar.frameOpened(f);
        } else {
            throw new UnsupportedOperationException("Only NabInternalFrame can be added to the desktop");
        }
    }
    
    public void setMemoryBar(boolean val){
        if(val){
            state = new GlobalState();
            bottom.add(state, BorderLayout.EAST);
        } else {
            state.terminate();
            bottom.remove(state);
            state = null;
        }
    }
    
    private void addControls(){
        bottom = new JPanel(new BorderLayout());
        
        setLayout(new BorderLayout());
        
        dt = new JDesktopPane();
        man = new DesktopManager();
        bar = new TaskBar();
        
        dt.setDesktopManager(man);
        man.addDesktopManagerListerner(bar);
        bar.setDesktop(dt);
        
        bottom.add(bar);
        super.add(bottom, BorderLayout.SOUTH);
        super.add(new JScrollPane(dt));
    }
}
