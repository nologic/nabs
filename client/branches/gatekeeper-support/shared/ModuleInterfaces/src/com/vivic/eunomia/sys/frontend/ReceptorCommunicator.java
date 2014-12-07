/*
 * ReceptorCommunicator.java
 *
 * Created on July 21, 2007, 1:27 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.vivic.eunomia.sys.frontend;

import com.vivic.eunomia.module.frontend.GUIModule;
import eunomia.flow.Filter;
import eunomia.messages.DatabaseDescriptor;
import eunomia.messages.receptor.ModuleHandle;
import eunomia.messages.receptor.protocol.ProtocolDescriptor;
import java.io.IOException;

/**
 *
 * @author Mikhail Sosonkin
 */
public interface ReceptorCommunicator {
    public void updateReceptor();
    public void instantiateModule(String str);
    public void terminateModule(ModuleHandle handle);
    public void getModuleFilterList(ModuleHandle handle);
    public void sendChangeFilter(ModuleHandle handle, Filter filter);
    public void getModuleStatusMessage(ModuleHandle handle);
    public void sendModuleControlData(ModuleHandle handle, GUIModule mod) throws IOException;
    public void sendAction(ModuleHandle handle, int action);
    public void getModuleList() throws IOException, ClassNotFoundException;
    public void removeStream(String name);
    public void addStream(String name, String modName, ProtocolDescriptor protocol);
    public void addDatabase(DatabaseDescriptor db);
    public void connectDatabase(DatabaseDescriptor db, boolean connect);
    public void collectDatabase(DatabaseDescriptor db, boolean collect, String coll);
    public void queryDatabase(String db, String query);
    public void getModuleControlData(ModuleHandle handle);
    public void connectStream(String name, boolean con);
    public void executeCommand(String cmd);
    public void addUser(String user, String pass);
    public void startAnalysisModule(String module);
    public void getAnalysisSummaryReport();
    public void getAnalysisReport(ModuleHandle handle);
    public void getModuleListeningList(ModuleHandle handle);
    public void connectModuleToServer(ModuleHandle handle, String server, boolean connect);
    public void connectDefuaultModuleToServers(ModuleHandle handle, boolean con);
    public void removeUser(String user);
    public void startAnalysis(ModuleHandle module, String[] databases, byte[] bytes);
    public void getAnalysisParameters(ModuleHandle handle);
    public void getModuleJar(String module, int type);
    public void getAdminStatus();
    public void setReceptorUser(String user, String new_pass, String old_pass);
    public void deleteReceptorUser(String user);
}
