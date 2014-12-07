/*
 * Module.java
 *
 * Created on June 24, 2005, 4:09 PM
 *
 */

package eunomia.plugin.interfaces;

import eunomia.core.receptor.listeners.MessageReceiver;
import eunomia.flow.Filter;
import eunomia.core.receptor.*;
import java.io.*;
import javax.swing.*;

/**
 *
 * @author Mikhail Sosonkin
 */
public interface GUIModule {
    //part getters.
    public JComponent getJComponent();
    public JComponent getControlComponent();
    public String getTitle();
    public MessageReceiver getReceiver();
        
    //module settings
    public void setReceptor(Receptor receptor);
    public Receptor getReceptor();
    
    //properties
    public void setProperty(String name, Object value);
    public Object getProperty(String name);
    
    //Communications
    public void updateStatus(InputStream in) throws IOException;
    public void getControlData(OutputStream out) throws IOException;
    public void setControlData(InputStream in) throws IOException;
}