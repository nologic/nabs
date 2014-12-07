/*
 * MessageFactory.java
 *
 * Created on October 10, 2006, 11:56 PM
 *
 */

package eunomia.core.receptor.comm;

import eunomia.core.receptor.Receptor;
import com.vivic.eunomia.filter.Filter;
import eunomia.messages.DatabaseDescriptor;
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
import eunomia.messages.receptor.msg.cmd.ConnectModuleDefaultMessage;
import eunomia.messages.receptor.msg.cmd.ConnectModuleMessage;
import eunomia.messages.receptor.msg.cmd.DatabaseQueryMessage;
import eunomia.messages.receptor.msg.cmd.GetModuleHandlesMessage;
import eunomia.messages.receptor.msg.cmd.GetModuleJarMessage;
import eunomia.messages.receptor.msg.cmd.GetModuleListeningListMessage;
import eunomia.messages.receptor.msg.cmd.InstantiateModuleMessage;
import eunomia.messages.receptor.msg.cmd.RemoveStreamMessage;
import eunomia.messages.receptor.msg.cmd.SignalMessage;
import eunomia.messages.receptor.msg.cmd.InstantiateAnalysisModuleMessage;
import eunomia.messages.receptor.msg.cmd.StreamConnectionMessage;
import eunomia.messages.receptor.msg.cmd.TerminateModuleMessage;
import eunomia.messages.receptor.msg.cmd.admin.AdminSignalMessage;
import eunomia.messages.receptor.msg.cmd.admin.SetUserMessage;
import eunomia.messages.receptor.msg.cmd.admin.ExecuteCommandMessage;
import eunomia.messages.receptor.msg.cmd.admin.RemoveUserMessage;
import eunomia.messages.receptor.protocol.ProtocolDescriptor;
import eunomia.module.ReportingFrontendModule;
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
        gflm.setModuleHandle(handle);
        receptor.sendMessage(gflm);
    }
    
    public void sendChangeFilter(ModuleHandle handle, Filter filter){
        ChangeFilterMessage cfm = filter.getChangeFilterMessage();
        
        cfm.setModuleHandle(handle);
        receptor.sendMessage(cfm);
    }
    
    public void getModuleStatusMessage(ModuleHandle handle){
        ModuleStatusMessage msm = new ModuleStatusMessage();
        msm.setModuleHandle(handle);
        receptor.sendMessage(msm);
        
        if(receptor.isRootAuthenticated()) {
            this.getAdminStatus();
        }
    }
    
    public void sendModuleControlData(ModuleHandle handle, Object module) throws IOException {
        if(module instanceof ReportingFrontendModule) {
            ReportingFrontendModule mod = (ReportingFrontendModule)module;
            
            ModuleControlDataMessage mcdm = new ModuleControlDataMessage();
            mcdm.setModuleHandle(handle);
            mod.getControlData(mcdm.getOutputStream());
            receptor.sendMessage(mcdm);
        }
    }
    
    public void sendAction(ModuleHandle handle, int action){
        ActionMessage am = new ActionMessage();
        am.setModuleHandle(handle);
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
    
    public void collectDatabase(DatabaseDescriptor db, boolean collect, String coll){
        CollectDatabaseMessage cdm = new CollectDatabaseMessage();
        
        cdm.setCollect(collect);
        cdm.setDbName(db.getName());
        cdm.setCollector(coll);
        
        receptor.sendMessage(cdm);
    }
    
    public void queryDatabase(String db, String query){
        DatabaseQueryMessage dqm = new DatabaseQueryMessage();
        
        dqm.setDbName(db);
        dqm.setQuery(query);

        receptor.sendMessage(dqm);
    }
    
    public void getModuleControlData(ModuleHandle handle){
        GetModuleControlDataMessage msm = new GetModuleControlDataMessage();
        msm.setModuleHandle(handle);
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
        SetUserMessage msg = new SetUserMessage();
        
        msg.setUser(user);
        msg.setPass(pass);
        
        receptor.sendMessage(msg);
    }
    
    public void startAnalysisModule(String module) {
        InstantiateAnalysisModuleMessage msg = new InstantiateAnalysisModuleMessage();
        
        msg.setModule(module);
        
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
    
    public void connectDefuaultModuleToServers(ModuleHandle handle, boolean con) {
        ConnectModuleDefaultMessage msg = new ConnectModuleDefaultMessage();
        
        msg.setDoAdd(con);
        msg.setModuleHandle(handle);
        
        receptor.sendMessage(msg);
    }
    
    public void removeUser(String user){
        RemoveUserMessage msg = new RemoveUserMessage();
        
        msg.setUsername(user);
        
        receptor.sendMessage(msg);
    }

    public void getModuleJar(String module, int type) {
        GetModuleJarMessage msg = new GetModuleJarMessage();
        
        msg.setType(type);
        msg.setModule(module);
        
        receptor.sendMessage(msg);
    }
    
    public void getAdminStatus() {
        AdminSignalMessage msg = new AdminSignalMessage(AdminSignalMessage.SIG_STATUS);
        
        receptor.sendMessage(msg);
    }
    
    public void setReceptorUser(String user, String new_pass, String old_pass) {
        SetUserMessage msg = new SetUserMessage();
        
        msg.setUser(user);
        msg.setPass(new_pass);
        msg.setOldPass(old_pass);
        
        receptor.sendMessage(msg);
    }
    
    public void deleteReceptorUser(String user) {
        RemoveUserMessage msg = new RemoveUserMessage();
        
        msg.setUsername(user);
        
        receptor.sendMessage(msg);
    }
}