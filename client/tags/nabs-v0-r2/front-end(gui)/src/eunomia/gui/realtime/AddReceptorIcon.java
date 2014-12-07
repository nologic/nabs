/*
 * AddReceptorIcon.java
 *
 * Created on December 20, 2006, 8:27 AM
 *
 */

package eunomia.gui.realtime;

import eunomia.core.managers.ReceptorManager;
import eunomia.gui.IconResource;
import eunomia.gui.MainGui;
import eunomia.gui.desktop.interfaces.DesktopIcon;
import eunomia.gui.desktop.interfaces.DesktopIconListener;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import javax.swing.Icon;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import org.apache.log4j.Logger;

/**
 *
 * @author Mikhail Sosonkin
 */
public class AddReceptorIcon implements DesktopIcon {
    private List listeners;
    
    private static Logger logger;

    static {
        logger = Logger.getLogger(AddReceptorIcon.class);
    }
    
    public AddReceptorIcon() {
        listeners = new LinkedList();
    }

    public String getName() {
        return "New Receptor";
    }

    public String getTooltip() {
        return "A quick link to adding a new receptor";
    }

    public JPopupMenu getContextMenu() {
        return null;
    }

    public Icon getIcon() {
        return IconResource.getReceptorAddNew();
    }

    public void activate() {
        String val = (String)JOptionPane.showInputDialog(MainGui.v(), "Enter the Receptor ip and port in the form of IP:[Port]", "Add new receptor",
                JOptionPane.QUESTION_MESSAGE, null, null, "127.0.0.1:4185");
        
        if(val != null){
            try {
                addNewReceptor(val);
            } catch (Exception ex) {
                logger.error("Unable to add new receptor: " + ex.getMessage());
            }
        }
    }
    
    private void addNewReceptor(String spec) throws IOException {
        int port = 4185;
        String ip;
        
        String[] split = spec.split(":");
        ip = split[0];
        
        if(split.length == 2){
            port = Integer.parseInt(split[1]);
        }
        
        String name = ip + ":" + port;
        if(ReceptorManager.ins.addReceptor(name, ip, port, 1500) == null){
            logger.info("Receptor by name '" + name + "' already exists.");
        } else {
            ReceptorManager.ins.save();
        }
    }

    public void addDesktopIconListener(DesktopIconListener l){
        listeners.add(l);
    }
    
    public void removeDesktopIconListener(DesktopIconListener l){
        listeners.remove(l);
    }
    
    public void iconChanged() {
        Iterator it = listeners.iterator();
        while (it.hasNext()) {
            DesktopIconListener l = (DesktopIconListener) it.next();
            l.iconChanged(this);
        }
    }
    
}
