/*
 * StreamView.java
 *
 * Created on June 17, 2005, 6:07 PM
 */

package eunomia.gui.realtime;

import eunomia.core.managers.ReceptorManager;
import eunomia.gui.desktop.NabInternalFrame;
import java.awt.event.*;
import java.io.IOException;
import javax.swing.*;
import eunomia.core.receptor.Receptor;
import eunomia.core.receptor.listeners.ReceptorListener;
import eunomia.gui.IconResource;
import eunomia.gui.LoginDialog;
import eunomia.gui.MainGui;
import eunomia.gui.desktop.NabInternalFrameListener;
import eunomia.gui.desktop.interfaces.DesktopIcon;
import eunomia.gui.desktop.interfaces.DesktopIconListener;
import java.util.Iterator;
import java.util.LinkedList;
import org.apache.log4j.*;

/**
 *
 * @author  Mikhail Sosonkin
 */
public class ReceptorView implements ActionListener, Runnable, ReceptorListener, NabInternalFrameListener, DesktopIcon {
    private Receptor receptor;
    private RealtimePanel rtPanel;
    private JInternalFrame recFrame;
    private LinkedList listeners;
    private String tooltip;
    private JPopupMenu menu;
    private JMenuItem con, dis, login, config, delete;
    private boolean isActive;
    private LoginDialog loginDialog;
    
    private static Logger logger;
    
    static {
        logger = Logger.getLogger(ReceptorView.class);
    }
    
    public ReceptorView(Receptor rec) {
        receptor = rec;
        loginDialog = new LoginDialog();
        receptor.addReceptorListener(this);
        menu = new JPopupMenu();
        rtPanel = new RealtimePanel(rec);
        listeners = new LinkedList();
        tooltip = "Disconnected";
        initMenu();
    }
    
    private void initMenu(){
        con = menu.add("Connect");
        dis = menu.add("Disconnect");
        login = menu.add("Enter Credentials");
        menu.addSeparator();
        delete = menu.add("Delete");
        config = menu.add("Configure");
        
        delete.addActionListener(this);
        con.addActionListener(this);
        dis.addActionListener(this);
        login.addActionListener(this);
        config.addActionListener(this);
    }
    
    public void actionPerformed(ActionEvent e) {
        Object o = e.getSource();
        
        if(o == con){
            activate();
        } else if(o == dis){
            disconnect();
        } else if(o == login){
            loginDialog.askPassword(true);
        } else if(o == config){
            MainGui.v().showSettingsForReceptor(receptor);
        } else if(o == delete){
            removeReceptor();
        }
    }
    
    private void removeReceptor(){
        if(JOptionPane.showConfirmDialog(MainGui.v(), "The receptor '"+ receptor + "' will be forever remove?") == JOptionPane.YES_OPTION){
            try {
                ReceptorManager.ins.removeReceptor(receptor);
                ReceptorManager.ins.save();
            } catch(Exception e){
                e.printStackTrace();
                logger.error("Error while removing: " + receptor);
            }
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
    
    public void setReceptorFrame(JInternalFrame f){
        recFrame = f;
        ((NabInternalFrame)f).addNabInternalFrameListener(this);
    }
    
    public RealtimePanel getRealtimePanel(){
        return rtPanel;
    }
    
    public void receptorConnected(Receptor rec) {
        logger.info(receptor + " -> Obtaining State...");
        rtPanel.reset();
        receptor.getOutComm().updateReceptor();
        try {
            receptor.getOutComm().getModuleList();
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        }
        rtPanel.updateModuleList();
        isActive = false;
        
        tooltip = "Connected";
        logger.info(receptor + " -> Connected");
        iconChanged();
        recFrame.setVisible(true);
    }

    public void receptorDisconnected(Receptor rec) {
        isActive = false;
        tooltip = "Disconnected";
        logger.info(receptor + " -> Disconnected");
        iconChanged();
    }
    
    public void disconnect(){
        try {
            receptor.disconnect();
        } catch (Exception e){
            logger.error(e.getMessage());
            e.printStackTrace();
        }
    }

    public void run(){
        if(loginDialog.askPassword(true) == LoginDialog.SUBMITED){
            try {
                if(!receptor.isConnected()){
                    receptor.setCredentials(loginDialog.getUsername(), loginDialog.getPassword());
                    logger.info(receptor + " -> Connecting...");
                    receptor.connect();
                    return;
                }
            } catch(Exception ex){
                ex.printStackTrace();
                logger.error(receptor + " -> " + ex.getMessage());
                receptorDisconnected(receptor);
            }
        }
        isActive = false;
    }
    
    public void activate(){
        if(receptor.isConnected()){
            recFrame.setVisible(true);
            recFrame.toFront();
        } else {
            if(!isActive){
                isActive = true;
                new Thread(this, "Receptor View").start();
            }
        }
    }

    public void titleChange(NabInternalFrame f) {
    }

    public void frameDisplayed(NabInternalFrame f) {
    }

    public void frameHidden(NabInternalFrame f) {
    }

    public String getName() {
        return receptor.getName();
    }

    public String getTooltip() {
        return tooltip;
    }

    public JPopupMenu getContextMenu() {
        return menu;
    }

    public Icon getIcon() {
        return IconResource.getReceptorIcon();
    }

    public void frameMinimized(NabInternalFrame f) {
    }
}