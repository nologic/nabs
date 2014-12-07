/*
 * GUIPluginListener.java
 *
 * Created on February 2, 2006, 7:14 PM
 */

package com.vivic.eunomia.module.frontend;

/**
 *
 * @author Mikhail Sosonkin
 */
public interface GUIPluginListener {
    public void statusUpdated(GUIModule mod);
    public void controlUpdated(GUIModule mod);
    public void controlObtained(GUIModule mod);
    public void streamListUpdated(GUIModule mod);
}