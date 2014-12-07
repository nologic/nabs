/*
 * ModuleStartMenu.java
 *
 * Created on January 14, 2007, 9:54 PM
 *
 */

package eunomia.gui.realtime.receptorAdmin;

import eunomia.core.managers.event.state.AddDatabaseEvent;
import eunomia.core.managers.event.state.AddDatabaseTypeEvent;
import eunomia.core.managers.event.state.AddModuleEvent;
import eunomia.core.managers.event.state.AddStreamServerEvent;
import eunomia.core.managers.event.state.ReceptorUserAddedEvent;
import eunomia.core.managers.event.state.ReceptorUserRemovedEvent;
import eunomia.core.managers.event.state.RemoveDatabaseEvent;
import eunomia.core.managers.event.state.RemoveStreamServerEvent;
import eunomia.core.managers.event.state.StreamStatusChangedEvent;
import eunomia.core.managers.listeners.ReceptorStateListener;
import eunomia.core.receptor.Receptor;
import eunomia.messages.receptor.ModuleHandle;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

/**
 *
 * @author Mikhail Sosonkin
 */
public class ModuleStartMenu extends JPopupMenu implements ActionListener, ReceptorStateListener {
    private JMenuItem rtModuleLabel;
    private JMenuItem dbModuleLabel;
    private Receptor receptor;
    
    public ModuleStartMenu(Receptor rec) {
        receptor = rec;
        
        rtModuleLabel = new JMenuItem("Realtime Modules");
        dbModuleLabel = new JMenuItem("Analysis Modules");
        rtModuleLabel.setEnabled(false);
        dbModuleLabel.setEnabled(false);
        
        receptor.getState().addReceptorStateListener(this);

        this.add(rtModuleLabel);
        this.addSeparator();
        this.add(dbModuleLabel);
    }

    public void actionPerformed(ActionEvent e) {
        Object o = e.getSource();
        
        if(o instanceof RealtimeMenuItem) {
            JMenuItem item = (JMenuItem)o;
            receptor.getOutComm().instantiateModule(item.getText());
        } else if(o instanceof AnalysisMenuItem) {
            JMenuItem item = (JMenuItem)o;
            receptor.getOutComm().startAnalysisModule(item.getText());
        }
    }
    
    private int indexOf(Component c) {
        Component[] comps = getComponents();
        for (int i = 0; i < comps.length; i++) {
            if(comps[i] == c) {
                return i;
            }
        }
        
        return -1;
    }

    public void databaseAdded(AddDatabaseEvent e) {
    }

    public void databaseRemoved(RemoveDatabaseEvent e) {
    }

    public void databaseTypeAdded(AddDatabaseTypeEvent e) {
    }

    public void moduleAdded(AddModuleEvent e) {
        String module = e.getModule();
        
        if(module.equals("streamStatus")){
            return;
        }

        Component[] comps = getComponents();
        for (int i = 0; i < comps.length; i++) {
            Component comp = comps[i];
            
            if( (e.getType() == ModuleHandle.TYPE_PROC && comp instanceof RealtimeMenuItem && ((RealtimeMenuItem)comp).getText().equals(module)) ||
                (e.getType() == ModuleHandle.TYPE_ANLZ && comp instanceof AnalysisMenuItem && ((AnalysisMenuItem)comp).getText().equals(module))) {
                return;
            }
        }
        
        JMenuItem item = null;
        JMenuItem addAfter = null;
        if(e.getType() == ModuleHandle.TYPE_PROC){
            item = new RealtimeMenuItem(module);
            addAfter = rtModuleLabel;
        } else if(e.getType() == ModuleHandle.TYPE_ANLZ) {
            item = new AnalysisMenuItem(module);
            addAfter = dbModuleLabel;        
        } else {
            return;
        }
        
        int add = indexOf(addAfter);
        this.add(item, add + 1);

        item.addActionListener(this);
        
        this.revalidate();
        this.repaint();
    }

    public void streamServerAdded(AddStreamServerEvent e) {
    }

    public void streamServerRemoved(RemoveStreamServerEvent e) {
    }

    public void streamStatusChanged(StreamStatusChangedEvent e) {
    }

    public void receptorUserAdded(ReceptorUserAddedEvent e) {
    }

    public void receptorUserRemoved(ReceptorUserRemovedEvent e) {
    }
    
    private class RealtimeMenuItem extends JMenuItem {
        public RealtimeMenuItem(String text){
            super(text);
        }
    }
    
    private class AnalysisMenuItem extends JMenuItem {
        public AnalysisMenuItem(String text){
            super(text);
        }
    }
}