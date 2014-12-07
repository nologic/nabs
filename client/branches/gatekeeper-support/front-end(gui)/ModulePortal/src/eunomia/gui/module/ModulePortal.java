/*
 * ModulePortal.java
 *
 * Created on January 14, 2007, 9:11 PM
 *
 */

package eunomia.gui.module;

import eunomia.gui.desktop.NabInternalFrame;
import eunomia.module.FrontendModule;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.border.Border;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;

/**
 *
 * @author Mikhail Sosonkin
 */
public abstract class ModulePortal extends NabInternalFrame implements ActionListener, 
        InternalFrameListener {
    
    protected static final Border toolBarBorder;
    protected static final Dimension seperatorDimension;
    
    protected FrontendModule module;
    protected int buttonMask;
    
    private boolean showModuleID;
    
    static {
        seperatorDimension = new Dimension(20, 20);
        toolBarBorder = BorderFactory.createEtchedBorder();
    }

    
    public ModulePortal(FrontendModule module){
        showModuleID = true;
        
        this.module = module;

        setSize(600, 500);
        setTitle(module.getHandle().getModuleName());
        setMaximizable(true);
        setResizable(true);
        setIconifiable(true);
        addInternalFrameListener(this);
        setDefaultCloseOperation(JInternalFrame.DO_NOTHING_ON_CLOSE);
        addControls();
    }
    
    public abstract void terminate();
    public abstract void setButtonMask(int mask);
    protected abstract void addControls();
    
    public int getButtonMask() {
        return buttonMask;
    }
    
    public FrontendModule getModule() {
        return module;
    }
    
    public boolean restoreWindowState() {
        String locdim = module.getReceptor().getProperty(module.getHandle().getInstanceID() + "");
        
        if(locdim != null){
            String[] pd = locdim.split(",");
            try {
                Point p = new Point(Integer.parseInt(pd[0]), Integer.parseInt(pd[1]));
                Dimension d = new Dimension(Integer.parseInt(pd[2]), Integer.parseInt(pd[3]));
            
                setLocation(p);
                setSize(d);
                return true;
            } catch (Exception e){
            }
        }
        
        return false;
    }

    public void saveWindowState() {
        Dimension d = getSize();
        Point p = getLocation();
        
        String locdim = (int)p.getX() + "," + (int)p.getY() + "," + (int)d.getWidth() + "," + (int)d.getHeight();
        module.getReceptor().setProperty(module.getHandle().getInstanceID() + "", locdim);
    }
    
    public void setTitle(String title) {
        String prefix = "[" + module.getHandle().getInstanceID() + "] - ";
        
        if(!showModuleID) {
            prefix = "";
        }
        
        super.setTitle(prefix + title);
    }
    
    public void internalFrameActivated(InternalFrameEvent e) {
    }
    
    public void internalFrameClosed(InternalFrameEvent e) {
    }
    
    public void internalFrameClosing(InternalFrameEvent e) {
        terminate();
    }
    
    public void internalFrameDeactivated(InternalFrameEvent e) {
    }
    
    public void internalFrameDeiconified(InternalFrameEvent e) {
    }
    
    public void internalFrameIconified(InternalFrameEvent e) {
    }
    
    public void internalFrameOpened(InternalFrameEvent e) {
    }
    
    public static JButton makeButton(String text, String tp, Icon icon){
        JButton button = new JButton(text);
        
        Font font = button.getFont();
        button.setToolTipText(tp);
        button.setFont(new Font(font.getName(), Font.BOLD, 9));
        button.setIcon(icon);
        
        return button;
    }

    public boolean isShowModuleID() {
        return showModuleID;
    }

    public void setShowModuleID(boolean showModuleID) {
        this.showModuleID = showModuleID;
    }
}