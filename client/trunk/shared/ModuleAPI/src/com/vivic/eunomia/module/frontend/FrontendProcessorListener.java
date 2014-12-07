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
public interface FrontendProcessorListener {
    public void statusUpdated(FrontendProcessorModule mod);
    public void controlUpdated(FrontendProcessorModule mod);
    public void controlObtained(FrontendProcessorModule mod);
    public void streamListUpdated(FrontendProcessorModule mod);
}