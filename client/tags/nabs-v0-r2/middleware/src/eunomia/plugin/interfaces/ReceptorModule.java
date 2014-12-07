/*
 * ReceptorModule.java
 *
 * Created on October 23, 2005, 5:44 PM
 *
 */

package eunomia.plugin.interfaces;

import eunomia.flow.*;
import eunomia.messages.Message;
import eunomia.messages.module.ModuleMessage;
import java.io.*;

/**
 *
 * @author Mikhail Sosonkin
 */
public interface ReceptorModule {
    //load/unload
    public void destroy();
    
    //functions
    public FlowProcessor getFlowProcessor();
    public void updateStatus(OutputStream out) throws IOException;
    public void setControlData(InputStream in) throws IOException;
    public void getControlData(OutputStream out) throws IOException;
    public Message processMessage(ModuleMessage msg) throws IOException;
    
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