/*
 * StaticAnalysisModule.java
 *
 * Created on November 21, 2006, 8:59 PM
 *
 */

package com.vivic.eunomia.module.receptor;

import com.vivic.eunomia.module.EunomiaModule;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

/**
 *
 * @author Mikhail Sosonkin
 */
public interface ReceptorAnalysisModule extends EunomiaModule {
    //unload
    public void destroy();
    
    //functions
    public void updateStatus(OutputStream out) throws IOException;
    public void setControlData(InputStream in) throws IOException;
    public void getControlData(OutputStream out) throws IOException;
    
    public void processMessage(DataInputStream in, DataOutputStream out) throws IOException;

    //properties
    public void setProperty(String name, Object value);
    public Object getProperty(String name);
    
    //commands
    public Object[] getCommands();
    public Object executeCommand(Object command, Object[] parameters);
    
    //Thread main.
    public void threadMain();
}