/*
 * ReceptorState.java
 *
 * Created on July 21, 2007, 1:31 AM
 *
 */

package com.vivic.eunomia.sys.frontend;

import eunomia.messages.DatabaseDescriptor;
import java.util.List;

/**
 *
 * @author Mikhail Sosonkin
 */
public interface ConsoleReceptorState {
    public List getStreamServers();
    public List getModules();
    public List getFlowModules();
    public List getDatabases();
    public List getAnalysisModules();
    public List getDatabaseTypes();
    public List getCollectors();
    public Object getStreamServer(String name);
    public DatabaseDescriptor getDatabaseDescriptor(String name);
    public List getReceptorUsers();
    public List getReceptorModules();
}
