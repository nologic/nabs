/*
 * ReceptorModule.java
 *
 * Created on October 23, 2005, 5:44 PM
 *
 */

package com.vivic.eunomia.module.receptor;

import com.vivic.eunomia.module.EunomiaModule;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 *
 * @author Mikhail Sosonkin
 */
public interface ReceptorModule extends EunomiaModule {
    //unload
    public void destroy();
    
    //functions
    public FlowProcessor getFlowProcessor();
    public void updateStatus(OutputStream out) throws IOException;
    public void setControlData(InputStream in) throws IOException;
    public void getControlData(OutputStream out) throws IOException;
    
    public void processMessage(DataInputStream in, DataOutputStream out) throws IOException;
    
    //actions
    public void start();
    public void stop();
    public void reset();
    
    //properties
    public void setProperty(String name, Object value);
    public Object getProperty(String name);
    
    //commands
    public Object[] getCommands();
    public Object executeCommand(Object command, Object[] parameters);
}