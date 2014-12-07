/*
 * ConsoleInterface.java
 *
 * Created on July 21, 2007, 1:22 AM
 *
 */

package com.vivic.eunomia.sys.frontend;

import eunomia.messages.Message;

/**
 *
 * @author Mikhail Sosonkin
 */
public interface ConsoleReceptor {
    public ReceptorCommunicator getOutComm();
    public ConsoleReceptorState getState();
    public ConsoleModuleManager getManager();
    public GlobalSettings getGlobalSettings();
    
    public boolean isConnected();
    public boolean isAuthenticated();
    public boolean isRootAuthenticated();
    public String getIP();
    public int getPort();
    public String getName();
    public int getRefreshRate();
    public void sendMessage(Message msg);
    public void setProperty(String key, String value);
    public String getProperty(String key);
    public int getSerialNumber();
}
