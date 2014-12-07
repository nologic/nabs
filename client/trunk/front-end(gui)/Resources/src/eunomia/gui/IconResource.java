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
    private static Icon flowModuleListenAll;
    private static Icon flowModuleListenNone;
    private static Icon flowModuleListenChoose;
    private static Icon anlzModuleWindow;
    private static Icon receptorAdminister;
    private static Icon receptorAddNew;
    private static Icon receptorStartModule;
    private static Icon receptorShowSummary;
    
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
    
    public static Icon getReceptorStartModule() {
        return receptorStartModule;
    }

    public static Icon getAnlzModuleWindow() {
        return anlzModuleWindow;
    }

    public static Icon getReceptorShowSummary() {
        return receptorShowSummary;
    }

    public static Icon getFlowModuleListenAll() {
        return flowModuleListenAll;
    }

    public static Icon getFlowModuleListenNone() {
        return flowModuleListenNone;
    }

    public static Icon getFlowModuleListenChoose() {
        return flowModuleListenChoose;
    }

    static {
        receptorIcon = loadIcon("icons/applications-internet.png");
        settingsIcon = loadIcon("icons/preferences-system.png");
        aboutIcon = loadIcon("icons/contact-new.png");
        flowModuleStop = loadIcon("icons/flowPortal/media-playback-pause.png");
        flowModuleReset = loadIcon("icons/flowPortal/edit-redo.png");
        flowModuleStart = loadIcon("icons/flowPortal/media-playback-start.png");
        flowModuleDetach = loadIcon("icons/flowPortal/view-fullscreen.png");
        flowModuleFilter = loadIcon("icons/flowPortal/edit-clear.png");
        flowModuleControl = loadIcon("icons/flowPortal/applications-system.png");
        flowModuleWindow = loadIcon("icons/flowPortal/Burn.png");
        anlzModuleWindow = loadIcon("icons/flowPortal/edit-find-replace.png");
        receptorAdminister = loadIcon("icons/document-properties.png");
        receptorAddNew = loadIcon("icons/system-software-update.png");
        receptorStartModule = loadIcon("icons/window-new.png");
        receptorShowSummary = loadIcon("icons/address-book-new.png");
        flowModuleListenAll = loadIcon("icons/flowPortal/list-add.png");
        flowModuleListenNone = loadIcon("icons/flowPortal/list-remove.png");
        flowModuleListenChoose = loadIcon("icons/flowPortal/mail-send-receive.png");
    }
    
    private static Icon loadIcon(String rname) {
        try {
            return new ImageIcon(ClassLoader.getSystemResource(rname));
        } catch (Exception e) {
            System.out.println("Icon not found: " + rname);
            return null;
        }
    }
}