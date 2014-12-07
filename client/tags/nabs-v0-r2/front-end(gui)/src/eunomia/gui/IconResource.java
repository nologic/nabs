/*
 * IconResource.java
 *
 * Created on December 11, 2006, 9:47 PM
 *
 */

package eunomia.gui;

import javax.swing.Icon;
import javax.swing.ImageIcon;

/**
 *
 * @author Mikhail Sosonkin
 */
public class IconResource {
    private static Icon receptorIcon;
    private static Icon settingsIcon;
    private static Icon aboutIcon;
    private static Icon flowModuleStop;
    private static Icon flowModuleReset;
    private static Icon flowModuleStart;
    private static Icon flowModuleDetach;
    private static Icon flowModuleFilter;
    private static Icon flowModuleControl;
    private static Icon flowModuleWindow;
    private static Icon receptorAdminister;
    private static Icon receptorAddNew;
    
    public static Icon getReceptorIcon() {
        return receptorIcon;
    }
    
    public static Icon getSettingsIcon() {
        return settingsIcon;
    }
    
    public static Icon getAboutIcon() {
        return aboutIcon;
    }
    
    public static Icon getFlowModuleStop() {
        return flowModuleStop;
    }

    public static Icon getFlowModuleReset() {
        return flowModuleReset;
    }

    public static Icon getFlowModuleStart() {
        return flowModuleStart;
    }
    
    public static Icon getFlowModuleDetach() {
        return flowModuleDetach;
    }

    public static Icon getFlowModuleFilter() {
        return flowModuleFilter;
    }

    public static Icon getFlowModuleControl() {
        return flowModuleControl;
    }
    
    public static Icon getFlowModuleWindow() {
        return flowModuleWindow;
    }

    public static Icon getReceptorAdminister() {
        return receptorAdminister;
    }
    
    public static Icon getReceptorAddNew() {
        return receptorAddNew;
    }

    static {
        receptorIcon = new ImageIcon(ClassLoader.getSystemResource("icons/applications-internet.png"));
        settingsIcon = new ImageIcon(ClassLoader.getSystemResource("icons/preferences-system.png"));
        aboutIcon = new ImageIcon(ClassLoader.getSystemResource("icons/contact-new.png"));
        flowModuleStop = new ImageIcon(ClassLoader.getSystemResource("icons/flowPortal/media-playback-pause.png"));
        flowModuleReset = new ImageIcon(ClassLoader.getSystemResource("icons/flowPortal/emblem-symbolic-link.png"));
        flowModuleStart = new ImageIcon(ClassLoader.getSystemResource("icons/flowPortal/media-playback-start.png"));
        flowModuleDetach = new ImageIcon(ClassLoader.getSystemResource("icons/flowPortal/view-fullscreen.png"));
        flowModuleFilter = new ImageIcon(ClassLoader.getSystemResource("icons/flowPortal/edit-clear.png"));
        flowModuleControl = new ImageIcon(ClassLoader.getSystemResource("icons/flowPortal/applications-system.png"));
        flowModuleWindow = new ImageIcon(ClassLoader.getSystemResource("icons/flowPortal/quake3.png"));
        receptorAdminister = new ImageIcon(ClassLoader.getSystemResource("icons/document-properties.png"));
        receptorAddNew = new ImageIcon(ClassLoader.getSystemResource("icons/system-software-update.png"));
    }
}