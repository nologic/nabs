/*
 * MessageFactory.java
 *
 * Created on October 10, 2006, 11:56 PM
 *
 */

package eunomia.core.receptor.comm;

import eunomia.core.receptor.Receptor;
import eunomia.core.receptor.listeners.MessageReceiver;
import eunomia.messages.ByteArrayMessage;
import eunomia.messages.DatabaseDescriptor;
import eunomia.messages.FilterEntryMessage;
import eunomia.messages.Message;
import eunomia.messages.module.msg.ActionMessage;
import eunomia.messages.module.msg.ChangeFilterMessage;
import eunomia.messages.module.msg.GetFilterListMessage;
import eunomia.messages.module.msg.GetModuleControlDataMessage;
import eunomia.messages.module.msg.ModuleControlDataMessage;
import eunomia.messages.module.msg.ModuleStatusMessage;
import eunomia.messages.receptor.ModuleHandle;
import eunomia.messages.receptor.msg.cmd.AddDatabaseMessage;
import eunomia.messages.receptor.msg.cmd.AddStreamMessage;
import eunomia.messages.receptor.msg.cmd.CollectDatabaseMessage;
import eunomia.messages.receptor.msg.cmd.ConnectDatabaseMessage;
import eunomia.messages.receptor.msg.cmd.ConnectModuleMessage;
import eunomia.messages.receptor.msg.cmd.DatabaseQueryMessage;
import eunomia.messages.receptor.msg.cmd.GetAnalysisReportMessage;
import eunomia.messages.receptor.msg.cmd.GetModuleHandlesMessage;
import eunomia.messages.receptor.msg.cmd.GetModuleListeningListMessage;
import eunomia.messages.receptor.msg.cmd.GetProcessingSummaryMessage;
import eunomia.messages.receptor.msg.cmd.InstantiateModuleMessage;
import eunomia.messages.receptor.msg.cmd.RemoveStreamMessage;
import eunomia.messages.receptor.msg.cmd.SignalMessage;
import eunomia.messages.receptor.msg.cmd.StartDatabaseAnalysisMessage;
import eunomia.messages.receptor.msg.cmd.StreamConnectionMessage;
import eunomia.messages.receptor.msg.cmd.TerminateModuleMessage;
import eunomia.messages.receptor.msg.cmd.admin.AddUserMessage;
import eunomia.messages.receptor.msg.cmd.admin.ExecuteCommandMessage;
import eunomia.messages.receptor.msg.cmd.admin.RemoveUserMessage;
import eunomia.messages.receptor.protocol.ProtocolDescriptor;
import eunomia.plugin.interfaces.GUIModule;
import java.io.IOException;
import org.apache.log4j.Logger;

/**
 *
 * @author Mikhail Sosonkin
 */
public class ReceptorOutComm {
    private Receptor receptor;
    
    private static Logger logger;
    
    static {
        logger = Logger.getLogger(ReceptorOutComm.class);
    }
    
    public ReceptorOutComm(Receptor rec) {
        receptor = rec;
    }
    
    public void updateReceptor(){
        SignalMessage sm = new SignalMessage(SignalMessage.SIG_STATUS);
        receptor.sendMessage(sm);
    }
    
    public void instantiateModule(String str){
        InstantiateModuleMessage imm = new InstantiateModuleMessage();
        imm.setModName(str);
        receptor.sendMessage(imm);
    }
    
    public void terminateModule(ModuleHandle handle){
        TerminateModuleMessage tmm = new TerminateModuleMessage();
        tmm.setModuleHandle(handle);
        receptor.sendMessage(tmm);
    }
    
    public void getModuleFilterList(ModuleHandle handle){
        GetFilterListMessage gflm = new GetFilterListMessage();
        gflm.setModuleID(handle.getInstanceID());
        receptor.sendMessage(gflm);
    }
    
    public void sendChangeFilter(ModuleHandle handle, FilterEntryMessage[] whiteList, FilterEntryMessage[] blackList){
        ChangeFilterMessage cfm = new ChangeFilterMessage();
        cfm.setWhiteList(whiteList);
        cfm.setBlackList(blackList);
        
        cfm.setModuleID(handle.getInstanceID());
        receptor.sendMessage(cfm);
    }
    
    public void getModuleStatusMessage(ModuleHandle handle){
        ModuleStatusMessage msm = new ModuleStatusMessage();
        msm.setModuleID(handle.getInstanceID());
        receptor.sendMessage(msm);
    }
    
    public void sendModuleControlData(ModuleHandle handle, GUIModule mod) throws IOException {
        ModuleControlDataMessage mcdm = new ModuleControlDataMessage();
        mcdm.setModuleID(handle.getInstanceID());
        mod.getControlData(mcdm.getOutputStream());
        receptor.sendMessage(mcdm);
    }
    
    public void sendAction(ModuleHandle handle, int action){
        ActionMessage am = new ActionMessage();
        am.setModuleID(handle.getInstanceID());
        am.setAction(action);
        receptor.sendMessage(am);
    }
    
    public void getModuleList() throws IOException, ClassNotFoundException {
        Message msg = new GetModuleHandlesMessage();
        
        receptor.sendMessage(msg);
    }
    
    public void removeStream(String name){
        RemoveStreamMessage rsm = new RemoveStreamMessage();
        
        rsm.setName(name);
        
        receptor.sendMessage(rsm);
    }
    
    public void addStream(String name, String modName, ProtocolDescriptor protocol){
        AddStreamMessage asm = new AddStreamMessage();
        
        asm.setModName(modName);
        asm.setName(name);
        asm.setProtocol(protocol);
        
        receptor.sendMessage(asm);
    }
    
    public void addDatabase(DatabaseDescriptor db){
        AddDatabaseMessage adm = new AddDatabaseMessage();
        
        adm.setDbDescriptor(db);
        
        receptor.sendMessage(adm);
    }

    public void connectDatabase(DatabaseDescriptor db, boolean connect){
        ConnectDatabaseMessage cdm = new ConnectDatabaseMessage();
        cdm.setConnect(connect);
        cdm.setDbName(db.getName());
        
        receptor.sendMessage(cdm);
    }
    
    public void collectDatabase(DatabaseDescriptor db, boolean collect){
        CollectDatabaseMessage cdm = new CollectDatabaseMessage();
        cdm.setCollect(collect);
        cdm.setDbName(db.getName());
        
        receptor.sendMessage(cdm);
    }
    
    public void queryDatabase(String db, String query, MessageReceiver recv){
        DatabaseQueryMessage dqm = new DatabaseQueryMessage();
        
        dqm.setDbName(db);
        dqm.setQuery(query);
        // This needs a map back.
        receptor.sendMessage(dqm, recv);
    }
    
    public void getModuleControlData(ModuleHandle handle){
        GetModuleControlDataMessage msm = new GetModuleControlDataMessage();
        msm.setModuleID(handle.getInstanceID());
        receptor.sendMessage(msm);
    }
    
    public void connectStream(String name, boolean con){
        StreamConnectionMessage scm = new StreamConnectionMessage(name, con);
        
        receptor.sendMessage(scm);
    }
    
    public void executeCommand(String cmd){
        ExecuteCommandMessage ecm = new ExecuteCommandMessage();
        
        ecm.setCommand(cmd);
        receptor.sendMessage(ecm);
    }
    
    public void addUser(String user, String pass) {
        AddUserMessage msg = new AddUserMessage();
        
        msg.setUser(user);
        msg.setPass(pass);
        
        receptor.sendMessage(msg);
    }
    
    public void analyzeDatabase(String db, String module, ByteArrayMessage arg) {
        StartDatabaseAnalysisMessage msg = new StartDatabaseAnalysisMessage();
        
        msg.setDb(db);
        msg.setModule(module);
        msg.setParams(arg);
        
        receptor.sendMessage(msg);
    }
    
    public void getAnalysisSummaryReport(String db) {
        GetProcessingSummaryMessage msg = new GetProcessingSummaryMessage();
        
        msg.setDatabaseName(db);
        
        receptor.sendMessage(msg);
    }
    
    public void getAnalysisReport(ModuleHandle handle){
        GetAnalysisReportMessage msg = new GetAnalysisReportMessage();
        
        msg.setHandle(handle);
        
        receptor.sendMessage(msg);
    }
    
    public void getModuleListeningList(ModuleHandle handle){
        GetModuleListeningListMessage msg = new GetModuleListeningListMessage();
        
        msg.setHandle(handle);
        
        receptor.sendMessage(msg);
    }
    
    public void connectModuleToServer(ModuleHandle handle, String server, boolean connect){
        ConnectModuleMessage msg = new ConnectModuleMessage();
        
        msg.setConnect(connect);
        msg.setFlowServer(server);
        msg.setHandle(handle);
        
        receptor.sendMessage(msg);
    }
    
    public void removeUser(String user){
        RemoveUserMessage msg = new RemoveUserMessage();
        
        msg.setUsername(user);
        
        receptor.sendMessage(msg);
    }
}