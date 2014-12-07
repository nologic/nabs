/*
 * Module.java
 *
 * Created on June 24, 2005, 4:09 PM
 *
 */

package com.vivic.eunomia.module.frontend;

import com.vivic.eunomia.module.EunomiaModule;
import com.vivic.eunomia.sys.frontend.ConsoleReceptor;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.swing.JComponent;

/**
 *
 * @author Mikhail Sosonkin
 */
public interface GUIModule extends EunomiaModule {
    //part getters.
    public JComponent getJComponent();
    public JComponent getControlComponent();
    public String getTitle();
    public void processMessage(DataInputStream din) throws IOException;
        
    //module settings
    public void setReceptor(ConsoleReceptor receptor);
    
    //properties
    public void setProperty(String name, Object value);
    public Object getProperty(String name);
    
    //Communications
    public void updateStatus(InputStream in) throws IOException;
    public void getControlData(OutputStream out) throws IOException;
    public void setControlData(InputStream in) throws IOException;
}