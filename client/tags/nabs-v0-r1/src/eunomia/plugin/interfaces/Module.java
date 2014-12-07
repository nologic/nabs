/*
 * Module.java
 *
 * Created on June 24, 2005, 4:09 PM
 *
 */

package eunomia.plugin.interfaces;

import eunomia.core.data.flow.Filter;
import eunomia.core.data.streamData.*;
import javax.swing.*;

/**
 *
 * @author Mikhail Sosonkin
 */
public interface Module {
    //part getters.
    public ModularFlowProcessor getFlowPocessor();
    public RefreshNotifier getRefreshNotifier();
    public JComponent getJComponent();
    public JComponent getControlComponent();
    public String getTitle();
    public Filter getFilter();
    
    //module options
    public boolean allowFullscreen();
    public boolean allowFilters();
    public boolean allowToolbar();
    public boolean isControlSeparate();
    public boolean isConfigSeparate();
    
    //module settings
    public void showLegend(boolean b);
    public void showTitle(boolean b);
    public void setStream(StreamDataSource sds);
    
    //properties
    public void setProperty(String name, Object value);
    public Object getProperty(String name);
    
    //actions
    public void start();
    public void stop();
    public void reset();
}