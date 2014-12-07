/*
 * NABStrings.java
 *
 * Created on May 28, 2006, 1:09 AM
 */

package eunomia.gui;

/**
 *
 * @author Mikhail Sosonkin
 */
public abstract class NABStrings {
    public static final String CURRENT_RECEPTOR_NAME = "Sieve";
    public static final String MAIN_WINDOW_TITLE = "NABS: Network Security Monitor";
    public static final String MAIN_MENU_TITLE = "Menu Options";
    
    public static final String SETTINGS_WINDOW_TITLE = "Global Settings";
    
    public static final String MAIN_MENU_SETTINGS = "Settings";
    public static final String MAIN_MENU_ABOUT = "About";
    
    public static final String SETTINGS_RECEPTORS_TAB = CURRENT_RECEPTOR_NAME + "s";
    public static final String SETTINGS_MODULES_TAB = "Modules";
    public static final String SETTINGS_COLOR_TAB = "Color";
    
    public static final String ABOUT_WINDOW_TITLE = "About";
    public static final String ABOUT_CONTENT = "" +
            "<html>" +
            "<body link=\"#000099\" bgcolor=\"#FFFFF0\">" +
            "<center><big><big><strong><font color=\"#800080\">" +
            "The NABS Console</font></strong></big></big><br>" +
            "<hr width=\"75%\">" +
            "<br>" +
            "<u>Vivic LLC.</u>" +
            "<br>" +
            "<br>" +
            "<strong>Contact:</strong><br>" +
            "Nasir Memon<br>" +
            "<a href=\"mailto:memon@poly.edu\">memon@poly.edu</a><br>" +
            "<br>" +
            "<small>Version 1.0 Beta, August 2007</small><br>" +
            "</center>" +
            "</body>" +
            "</html>";
}