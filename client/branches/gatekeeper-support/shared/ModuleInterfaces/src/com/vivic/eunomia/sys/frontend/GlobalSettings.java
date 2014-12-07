/*
 * GlobalSettings.java
 *
 * Created on July 21, 2007, 2:11 AM
 *
 */

package com.vivic.eunomia.sys.frontend;

import eunomia.config.ConfigChangeListener;
import java.awt.Color;

/**
 *
 * @author Mikhail Sosonkin
 */
public interface GlobalSettings {
    public Color getTypeColor(int type);
    public Color getTypeColor(String type);
    public void addConfigChangeListener(ConfigChangeListener l);
    public void removeConfigChangeListener(ConfigChangeListener l);
    public int getRefreshInterval();
}