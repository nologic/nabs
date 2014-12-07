/*
 * ClientComm.java
 *
 * Created on September 6, 2005, 12:56 PM
 *
 */

package eunomia.comm;

import com.vivic.eunomia.module.Descriptor;
import com.vivic.eunomia.module.receptor.FlowProcessor;
import eunomia.Main;
import eunomia.comm.auth.Authenticator;
import eunomia.comm.auth.exceptions.AuthenticationException;
import eunomia.comm.interfaces.ErrorMessenger;
import eunomia.managers.connectable.ConnectTuple;
import eunomia.managers.module.LibraryDescriptor;
import eunomia.managers.module.ModuleFile;
import eunomia.messages.receptor.auth.AuthenticationMessage;
import eunomia.messages.receptor.msg.cmd.admin.AdminMessage;
import eunomia.module.AnlzMiddlewareModule;
import eunomia.module.ProcMiddlewareModule;
import com.vivic.eunomia.module.receptor.ReceptorProcessorModule;
import eunomia.receptor.FlowServer;
import com.vivic.eunomia.module.flow.FlowModule;
import com.vivic.eunomia.sys.util.EunomiaUtils;
import eunomia.data.Database;
import eunomia.data.DatabaseTerminal;
import eunomia.exception.ManagerException;
import eunomia.exception.ReceptorManagerConnectInProcess;
import eunomia.exception.ResponceDelayException;
import eunomia.exception.ResponceFailureException;
import com.vivic.eunomia.filter.Filter;
import eunomia.managers.ClientManager;
import eunomia.managers.DatabaseManager;
import eunomia.managers.ModuleManager;
import eunomia.managers.ReceptorManager;
import eunomia.managers.StateManager;
import eunomia.managers.UserManager;
import eunomia.messages.DatabaseDescriptor;
import eunomia.messages.Message;
import eunomia.messages.module.ModuleMessage;
import eunomia.messages.module.msg.ActionMessage;
import eunomia.messages.module.msg.AnalysisParametersMessage;
import eunomia.messages.module.msg.ChangeFilterMessage;
import eunomia.messages.module.msg.GetAnalysisParametersMessage;
import eunomia.messages.module.msg.GetFilterListMessage;
import eunomia.messages.module.msg.GetModuleControlDataMessage;
import eunomia.messages.module.msg.ModuleControlDataMessage;
import eunomia.messages.module.msg.ModuleInterCommMessage;
import eunomia.messages.module.msg.ModuleStatusMessage;
import eunomia.messages.module.msg.StartAnalysisMessage;
import eunomia.messages.receptor.ModuleHandle;
import eunomia.messages.receptor.msg.cmd.AddDatabaseMessage;
import eunomia.messages.receptor.msg.cmd.AddStreamMessage;
import eunomia.messages.receptor.msg.cmd.CollectDatabaseMessage;
import eunomia.messages.receptor.msg.cmd.ConnectDatabaseMessage;
import eunomia.messages.receptor.msg.cmd.ConnectModuleDefaultMessage;
import eunomia.messages.receptor.msg.cmd.ConnectModuleMessage;
import eunomia.messages.receptor.msg.cmd.DatabaseQueryMessage;
import eunomia.messages.receptor.msg.cmd.GetAnalysisReportMessage;
import eunomia.messages.receptor.msg.cmd.GetModuleHandlesMessage;
import eunomia.messages.receptor.msg.cmd.GetModuleJarMessage;
import eunomia.messages.receptor.msg.cmd.GetModuleListeningListMessage;
import eunomia.messages.receptor.msg.cmd.InstantiateAnalysisModuleMessage;
import eunomia.messages.receptor.msg.cmd.InstantiateModuleMessage;
import eunomia.messages.receptor.msg.cmd.RemoveDatabaseMessage;
import eunomia.messages.receptor.msg.cmd.RemoveStreamMessage;
import eunomia.messages.receptor.msg.cmd.SignalMessage;
import eunomia.messages.receptor.msg.cmd.StreamConnectionMessage;
import eunomia.messages.receptor.msg.cmd.TerminateModuleMessage;
import eunomia.messages.receptor.msg.rsp.CommandResultMessage;
import eunomia.messages.receptor.msg.rsp.DatabaseQueryResultSetMessage;
import eunomia.messages.receptor.msg.rsp.FailureMessage;
import eunomia.messages.receptor.msg.rsp.ModuleHandleListMessage;
import eunomia.messages.receptor.msg.rsp.ModuleJarMessage;
import eunomia.messages.receptor.msg.rsp.ModuleListeningListMessage;
import eunomia.messages.receptor.msg.rsp.StatusMessage;
import eunomia.messages.receptor.msg.rsp.SuccessMessage;
import eunomia.messages.receptor.msg.rsp.UnknownMessage;
import eunomia.messages.receptor.ncm.AnalysisReportMessage;
import eunomia.messages.receptor.ncm.AnalysisSummaryMessage;
import eunomia.messages.receptor.ncm.ErrorMessage;
import eunomia.messages.receptor.ncm.ModuleConnectionStatusMessage;
import eunomia.util.DynamicOutputStream;
import com.vivic.eunomia.sys.util.Util;
import eunomia.util.io.StreamPipe;
import eunomia.util.oo.LargeTransfer;
import eunomia.util.oo.NabObjectInput;
import eunomia.util.oo.NabObjectOutput;
import eunomia.comm.ReceptorClassLocator;
import eunomia.module.ReportingModule;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.security.GeneralSecurityException;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import javax.net.ssl.SSLException;

import org.apache.log4j.Logger;


/**
 *
 * @author Mikhail Sosonkin
 */
public class ClientComm implements Runnable, ErrorMessenger {
    private UserState state;
    private Socket cSock;
    private NabObjectInput mesgInStream;
    private NabObjectOutput mesgOutStream;
    private Authenticator auth;
    private Authenticator adminAuth;
    private AdminComm adminComm;
    private boolean doFlush;
    private boolean hasDisconnected;
    
    private static Logger logger;
    
    static {
        logger = Logger.getLogger(ClientComm.class);
    }
    
    public ClientComm(Socket clientSocket) {
        hasDisconnected = false;
        cSock = clientSocket;
        
        logger.info("Client connected: " + cSock.getRemoteSocketAddress());
        
        new Thread(this).start();
    }
    
    public void run(){
        try {
            mesgInStream = new NabObjectInput(cSock.getInputStream(), new ReceptorClassLocator());
            mesgOutStream = new NabObjectOutput(new BufferedOutputStream(cSock.getOutputStream(), 65536));
            mesgOutStream.flush();
        } catch(Exception e){
            logger.error("Unable to create Client instance: " + e.getMessage());
            e.printStackTrace();
        }
        auth = new Authenticator();
        if(UserManager.v().getUserCount() > 1){
            adminAuth = new Authenticator();
        } else {
            adminAuth = auth;
        }
        adminComm = new AdminComm(this);
        adminComm.setShell(new Shell(this));
        adminAuth.setAllowRoot(true);
        
        doFlush = true;
        new Thread(new Flusher()).start();
        
        while(true){
            Message rsp = null;
            Message msg = null;
            
            try {
                msg = (Message)mesgInStream.readObject();
            } catch (ClassNotFoundException cnfe){
                logger.error("Unknown Message from: " + cSock.getRemoteSocketAddress());
                rsp = new UnknownMessage(cnfe.getMessage());
            } catch (SSLException se) {
                //logger.error("Error reading message: " /*+ se.getMessage()*/);
                disconnect();
                return;
            } catch (IOException ioe){
                //logger.error("Error reading message: "/* + ioe.getMessage()*/);
                disconnect();
                return;
            }
            
            if(rsp == null){
                try {
                    rsp = processMessage(msg);
                } catch(ResponceFailureException rfe){
                    rfe.printStackTrace();
                    rsp = new FailureMessage(msg, rfe.getMessage());
                } catch(ResponceDelayException rde){
                    continue;
                } catch(Exception ex){
                    ex.printStackTrace();
                    rsp = new FailureMessage(msg, ex.getMessage());
                }
            }
            
            try {
                sendMessage(rsp);
            } catch(IOException ioe){
                //logger.error("Error writting message: " + ioe.getMessage());
                disconnect();
                ioe.printStackTrace();
                return;
            }
        }
    }
    
    public void disconnect() {
        if(!hasDisconnected) {
            hasDisconnected = true;
            Main.removeEventer(adminComm);
            logger.info("Client disconnected: " + cSock.getRemoteSocketAddress());
            ClientManager.v().clientDisconnected(this);
            doFlush = false;
            try {
                mesgInStream.close();
                mesgOutStream.close();
            } catch (IOException ex) {
            }
        }
    }
    
    public void sendMessage(Message msg) throws IOException {
        synchronized(mesgOutStream){
            mesgOutStream.writeObject(msg);
        }
    }
    
    public void flushMessages(){
        synchronized(mesgOutStream){
            try {
                mesgOutStream.flush();
            } catch (Exception e){
                // ignore it, any errors will be handled by other functions.
            }
        }
    }
    
    public void broadCastSend(Message msg) throws IOException {
        ClientManager.v().broadCastSend(msg);
    }
    
    public void broadCaseRecv(Message msg) throws IOException {
        sendMessage(msg);
    }
    
    private void delayedResponse(Message msg, Message resp, boolean rawResponse) throws IOException {
        if(!rawResponse){
            if(resp == null){
                sendMessage(new SuccessMessage(msg, "OP Success"));
            } else {
                sendMessage(new CommandResultMessage(msg, "", resp));
            }
        } else {
            sendMessage(resp);
        }
    }
    
    // Execute the message
    private Message processMessage(Message msg) throws ResponceFailureException, Exception {
        Message resp = null; // null on silent success.
        
        if(auth.isAuthenticated()){
            if(msg instanceof AuthenticationMessage){
                resp = procAuthenticationMessage(adminAuth, (AuthenticationMessage)msg);
            } else if(msg instanceof ModuleMessage){
                resp = processModuleMessage(msg);
            } else if(msg instanceof AddDatabaseMessage){
                resp = procAddDatabaseMessage((AddDatabaseMessage)msg);
            } else if(msg instanceof AddStreamMessage){
                resp = procAddStreamMessage((AddStreamMessage)msg);
            } else if(msg instanceof CollectDatabaseMessage){
                resp = procCollectDatabaseMessage((CollectDatabaseMessage)msg);
            } else if(msg instanceof ConnectDatabaseMessage){
                resp = procConnectDatabaseMessage((ConnectDatabaseMessage)msg);
            } else if(msg instanceof RemoveDatabaseMessage){
                resp = procRemoveDatabaseMessage((RemoveDatabaseMessage)msg);
            } else if(msg instanceof RemoveStreamMessage){
                resp = procRemoveStreamMessage((RemoveStreamMessage)msg);
            } else if(msg instanceof StreamConnectionMessage){
                resp = procStreamConnectionMessage((StreamConnectionMessage)msg);
            } else if(msg instanceof GetModuleHandlesMessage){
                resp = procGetModuleHandlesMessage((GetModuleHandlesMessage)msg);
            } else if(msg instanceof SignalMessage){
                resp = processSignalMessage((SignalMessage)msg);
            } else if(msg instanceof InstantiateModuleMessage){
                resp = procInstantiateModuleMessage((InstantiateModuleMessage)msg);
            } else if(msg instanceof TerminateModuleMessage){
                resp = procTerminateModuleMessage((TerminateModuleMessage)msg);
            } else if(msg instanceof DatabaseQueryMessage){
                resp = procDatabaseQueryMessage((DatabaseQueryMessage)msg);
            } else if(msg instanceof GetModuleListeningListMessage){
                resp = procGetModuleListeningListMessage((GetModuleListeningListMessage)msg);
            } else if(msg instanceof AdminMessage){
                resp = procAdminMessage((AdminMessage)msg);
            } else if(msg instanceof InstantiateAnalysisModuleMessage){
                resp = procStartDatabaseAnalysisMessage((InstantiateAnalysisModuleMessage)msg);
            } else if(msg instanceof ConnectModuleMessage){
                resp = procConnectModuleMessage((ConnectModuleMessage)msg);
            } else if(msg instanceof ConnectModuleDefaultMessage){
                resp = procConnectModuleDefaultMessage((ConnectModuleDefaultMessage)msg);
            } else if(msg instanceof GetModuleJarMessage) {
                resp = procGetModuleJarMessage((GetModuleJarMessage)msg);
            }
        } else if(msg instanceof AuthenticationMessage){
            resp = procAuthenticationMessage(auth, (AuthenticationMessage)msg);
        } else {
            throw new ResponceFailureException("Not Authenticated");
        }
        
        if(resp == null){
            return new SuccessMessage(msg, "OP Success");
        }
        
        return resp;
    }
    
    private Message procAdminMessage(AdminMessage am) throws ResponceFailureException, IOException {
        if(adminAuth.isAuthenticated() && adminAuth.getUsername().equals("root")){
            return adminComm.processAdminMessage(am);
        }
        
        throw new ResponceFailureException("root not authenticated");
    }
    
    private Message procAuthenticationMessage(Authenticator au, AuthenticationMessage msg) throws AuthenticationException, GeneralSecurityException, ResponceDelayException{
        Message rsp;
        if(au.isInProcess()){
            rsp = au.updateState(msg);
        } else {
            rsp = au.startAuthentication(msg);
        }
        
        if(au.isAuthenticated()){
            if(au == auth){
                mesgInStream.setAllowUnknowns(true);
                state = StateManager.v().getState(auth.getUsername());
            }
            
            if(au == adminAuth){
                Main.addEventer(adminComm);
            }
        }
        
        if(rsp == null){
            throw new ResponceDelayException("");
        }
        
        return rsp;
    }
    
    private Message procTerminateModuleMessage(TerminateModuleMessage msg) throws ManagerException{
        state.terminateModule(msg.getModuleHandle());
        return null;
    }
    
    private Message procInstantiateModuleMessage(InstantiateModuleMessage msg) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        ModuleHandle handle = state.startProcModule(msg.getModName());
        ReceptorManager.v().addFlowProcessor(ModuleManager.v().getFlowProcessorConnectTuple(handle));
        
        return handle;
    }
    
    private Message processSignalMessage(SignalMessage msg) throws ResponceFailureException {
        switch(msg.getSignal()){
            case SignalMessage.SIG_STATUS: {
                return processSignalMessage_SIG_STATUS();
            }
            
            case SignalMessage.SIG_GET_DB_SUMMARY: {
                return processSignalMessage_SIG_GET_DB_SUMMARY();
            }
            
            default: {
                throw new ResponceFailureException("Unknown signal: " + msg.getSignal());
            }
        }
    }
    
    private Message processSignalMessage_SIG_STATUS() {
        StatusMessage sm = new StatusMessage();
        FlowServer[] servs = ReceptorManager.v().getFlowServers();
        sm.setServersCount(servs.length);
        for(int i = 0; i < servs.length; i++){
            FlowServer serv = servs[i];
            String modName = ModuleManager.v().getFlowModuleName(serv.getFlowModule());
            sm.setServer(i, serv.getProtocol().getProtocolDescriptor(), serv.getName(), modName, serv.getProtocol().isActive());
        }
        
        List modules = ModuleManager.v().getLinker().getModuleList(-1);
        sm.setModuleCount(modules.size());
        for (int i = 0; i < modules.size(); i++) {
            ModuleFile mod = (ModuleFile)modules.get(i);
            sm.setModule(i, mod.getName(), mod.getType(), mod.getHash());
        }
        
        List dbList = DatabaseManager.v().getDatabaseList();
        sm.setDatabaseCount(dbList.size());
        for(int i = 0; i < dbList.size(); i++){
            Database db = (Database)dbList.get(i);
            sm.setDatabase(i, db.getDescriptor(false));
        }
        
        sm.setJdbcList((String[])DatabaseManager.v().getJDBCList().toArray(new String[]{}));
        
        return sm;
    }
    
    private Message processSignalMessage_SIG_GET_DB_SUMMARY() {
        Iterator it = state.getHandles().iterator();
        AnalysisSummaryMessage asm = new AnalysisSummaryMessage();
        
        asm.setTimestamp(System.currentTimeMillis());
        
        /*while (it.hasNext()) {
            ModuleHandle handle = (ModuleHandle) it.next();
            if(handle.getModuleType() == ModuleHandle.TYPE_ANLZ) {
                AnlzMiddlewareModule mod = state.getAnlzModule(handle);
                AnalysisSummaryMessage.SUM sum = new AnalysisSummaryMessage.SUM(handle, mod.getProgress());
                asm.addSummary(sum);
            }
        }*/
        
        return asm;
    }
    
    private Message processModuleMessage(Message msg) throws IOException, ResponceFailureException, ResponceDelayException {
        Message resp = null; // null on silent success
        
        if(msg instanceof ModuleStatusMessage){
            resp = procGetModuleStatusMessage((ModuleStatusMessage)msg);
        } else if(msg instanceof ModuleControlDataMessage){
            resp = procSetModuleControlDataMessage((ModuleControlDataMessage)msg);
        } else if(msg instanceof GetModuleControlDataMessage){
            resp = procGetModuleControlDataMessage((GetModuleControlDataMessage)msg);
        } else if(msg instanceof ActionMessage){
            resp = procActionMessage((ActionMessage)msg);
        } else if(msg instanceof ChangeFilterMessage){
            resp = procChangeFilterMessage((ChangeFilterMessage)msg);
        } else if(msg instanceof GetFilterListMessage){
            resp = procGetFilterListMessage((GetFilterListMessage)msg);
        } else if(msg instanceof ModuleInterCommMessage) {
            resp = procModuleInterCommMessage((ModuleInterCommMessage)msg);
        }
        
        return resp;
    }
    
    public String toString(){
        return cSock.getRemoteSocketAddress().toString();
    }
    
    private Message procGetFilterListMessage(GetFilterListMessage gflm){
        ProcMiddlewareModule mod = (ProcMiddlewareModule)state.getReportingModule(gflm.getModuleHandle());
        
        Filter filter = mod.getFlowProcessor().getFilter();
        if(filter == null){
            filter = new Filter();
        }
        
        ChangeFilterMessage cfm = filter.getChangeFilterMessage();
        cfm.setModuleHandle(gflm.getModuleHandle());
        return cfm;
    }
    
    private Message procChangeFilterMessage(ChangeFilterMessage cfm){
        ProcMiddlewareModule mod = (ProcMiddlewareModule)state.getReportingModule(cfm.getModuleHandle());
        
        Filter filter = EunomiaUtils.makeReceptorFilter(cfm, ModuleManager.v());
        
        mod.getFlowProcessor().setFilter(filter);
        cfm.resetForReturn();
        
        return null;
    }
    
    private Message procModuleInterCommMessage(ModuleInterCommMessage msg) throws IOException, ResponceDelayException {
        ReportingModule mod = state.getReportingModule(msg.getModuleHandle());
        final ModuleInterCommMessage ret = new ModuleInterCommMessage();
        
        ret.setModuleHandle(msg.getModuleHandle());
        
        DynamicOutputStream dynout = new DynamicOutputStream((ByteArrayOutputStream)ret.getOutputStream(), NabObjectOutput.max_buf - 4096) {
            public OutputStream getSecondary() throws IOException {
                LargeTransfer trans = new LargeTransfer();
                StreamPipe pipe = new StreamPipe();
                
                trans.setInputStream(pipe);
                ret.setLargeTransfer(trans);
                
                delayedResponse(null, ret, true);
                
                return pipe.getOutput();
            }
        };
        
        mod.processMessage(new DataInputStream(msg.getInputStream()), new DataOutputStream(dynout));
        
        if(ret.getLargeTransfer() != null) {
            dynout.close();
            throw new ResponceDelayException("Already responded with large transfers.");
        } else {
            if(((ByteArrayOutputStream)ret.getOutputStream()).size() == 0){
                throw new ResponceDelayException("No response");
            } else {
                return ret;
            }
        }
    }
    
    private Message procActionMessage(ActionMessage msg){
        ProcMiddlewareModule mod = (ProcMiddlewareModule)state.getReportingModule(msg.getModuleHandle());
        
        switch(msg.getAction()){
            case ActionMessage.RESET:
                mod.reset();
                break;
            case ActionMessage.STOP:
                mod.stop();
                break;
            case ActionMessage.START:
                mod.start();
                break;
        }
        
        return null;
    }
    
    private Message procGetModuleControlDataMessage(GetModuleControlDataMessage msg) throws IOException, ResponceFailureException {
        ReportingModule mod = (ReportingModule)state.getReportingModule(msg.getModuleHandle());
        if(mod != null){
            ModuleControlDataMessage mcdm = new ModuleControlDataMessage();
            mcdm.setModuleHandle(msg.getModuleHandle());
            
            mod.getControlData(mcdm.getOutputStream());
            
            return mcdm;
        } else {
            logger.warn("Module instance " + msg.getModuleHandle() + " not found");
        }
        
        throw new ResponceFailureException("Module instance " + msg.getModuleHandle() + " not found");
    }
    
    private Message procSetModuleControlDataMessage(ModuleControlDataMessage msg) throws IOException {
        ReportingModule mod = (ReportingModule)state.getReportingModule(msg.getModuleHandle());
        mod.setControlData(msg.getInputStream());
        
        return null;
    }
    
    private Message procGetModuleStatusMessage(final ModuleStatusMessage msg) throws IOException, ResponceDelayException {
        ReportingModule mod = state.getReportingModule(msg.getModuleHandle());
        DynamicOutputStream dout = new DynamicOutputStream((ByteArrayOutputStream)msg.getOutputStream(), NabObjectOutput.max_buf - 4096) {
            public OutputStream getSecondary() throws IOException {
                LargeTransfer trans = new LargeTransfer();
                StreamPipe pipe = new StreamPipe();
                
                trans.setInputStream(pipe);
                msg.setLargeTransfer(trans);
                
                delayedResponse(null, msg, true);
                
                return pipe.getOutput();
            }
        };
        mod.updateStatus(dout);
        
        if(msg.getLargeTransfer() != null) {
            dout.close();
            throw new ResponceDelayException("Already responded with large transfers.");
        }
        
        return msg;
    }
    
    private Message procGetModuleHandlesMessage(GetModuleHandlesMessage msg){
        ModuleHandleListMessage mhlm = new ModuleHandleListMessage();
        
        Iterator it = state.getHandles().iterator();
        while(it.hasNext()){
            ModuleHandle handle = (ModuleHandle)it.next();
            mhlm.addHandle(handle);
        }
        
        return mhlm;
    }
    
    private Message procAddDatabaseMessage(AddDatabaseMessage adm) throws Exception {
        DatabaseDescriptor dbDesc = adm.getDbDescriptor();
        Database db = DatabaseManager.v().getDatabaseByName(dbDesc.getName());
        
        if(db == null){
            db = DatabaseManager.v().createDefaultDatabase(dbDesc.getDbType(), dbDesc.getName());
        }
        
        db.setJdbcType(dbDesc.getDbType());
        db.setAddress(dbDesc.getAddress());
        db.setCredentials(dbDesc.getUsername(), dbDesc.getPassword());
        db.setDatabaseName(dbDesc.getDbName());
        db.setMainTable(dbDesc.getTableName());
        db.setPort(dbDesc.getPort());
        
        DatabaseManager.v().saveDatabases();
        
        return null;
    }
    
    private Message procRemoveDatabaseMessage(RemoveDatabaseMessage msg) throws Exception {
        Database db = DatabaseManager.v().getDatabaseByName(msg.getName());
        
        String[] collectors = db.getCollectors();
        for (int i = 0; i < collectors.length; i++) {
            ConnectTuple tuple = new ConnectTuple();
            tuple.setFlowProcessor(db.getCollector(collectors[i]));
            ReceptorManager.v().removeFlowProcessor(tuple);
        }
        
        db.disconnect();
        DatabaseManager.v().removeDatabase(db);
        DatabaseManager.v().saveDatabases();
        
        return null;
    }
    
    private Message procCollectDatabaseMessage(CollectDatabaseMessage cdm) throws Exception {
        Database db = DatabaseManager.v().getDatabaseByName(cdm.getDbName());
        String collector = cdm.getCollector();
        
        if(cdm.isCollect()){
            if(!db.isCollecting(collector)){
                FlowProcessor proc = db.getCollector(collector);
                ConnectTuple tuple = new ConnectTuple();
                tuple.setFlowProcessor(proc);
                ReceptorManager.v().addFlowProcessor(tuple);
            }
        } else {
            if(db.isCollecting(collector)){
                FlowProcessor proc = db.getCollector(collector);
                ConnectTuple tuple = new ConnectTuple();
                tuple.setFlowProcessor(proc);
                ReceptorManager.v().removeFlowProcessor(tuple);
                db.removeCollector(collector);
            }
        }
        return null;
    }
    
    private Message procDatabaseQueryMessage(final DatabaseQueryMessage msg) throws IOException, ResponceDelayException, SQLException {
        final Database db = DatabaseManager.v().getDatabaseByName(msg.getDbName());
        
        if(db != null && db.isConnected()) {
            final DatabaseTerminal dt = new DatabaseTerminal();
            
            dt.setErrorMessenger(this);
            
            new Thread(new Runnable() {
                public void run() {
                    DatabaseQueryResultSetMessage resp = dt.sendQueryResult(db, msg.getQuery());
                    if(resp != null) {
                        resp.setDb(db.getName());
                        try {
                            delayedResponse(null, resp, true);
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    } else {
                        try {
                            delayedResponse(null, new FailureMessage(msg, ""), true);
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            }).start();
            
            throw new ResponceDelayException("");
        }
        
        return new FailureMessage(msg, "Database " + msg.getDbName() + " not connected");
    }
    
    private Message procConnectDatabaseMessage(final ConnectDatabaseMessage cdm) throws ResponceDelayException {
        final Database db = DatabaseManager.v().getDatabaseByName(cdm.getDbName());
        if(db != null){
            new Thread(new Runnable(){
                public void run(){
                    try {
                        if(cdm.isConnect()){
                            db.connect();
                        } else {
                            db.disconnect();
                        }
                        delayedResponse(cdm, null, false);
                    } catch(Exception e){
                        try {
                            delayedResponse(cdm, new FailureMessage(cdm, "Unable to connect to database: " + cdm.getDbName() + " cause: " + e.getMessage()), true);
                        } catch(Exception ex){
                            ex.printStackTrace();
                        }
                        return;
                    }
                }
            }).start();
            throw new ResponceDelayException("");
        }
        
        return new FailureMessage(cdm, "Database " + cdm.getDbName() + " not found");
    }
    
    private Message procAddStreamMessage(AddStreamMessage msg) throws IOException, ResponceDelayException {
        FlowServer fServ = ReceptorManager.v().getServerByName(msg.getName());
        FlowModule mod = ModuleManager.v().getFlowModuleInstance(msg.getModName());
        
        if(mod == null){
            return new FailureMessage(msg, "Flow module (" + msg.getModName() + ") not defined on the Middleware");
        }
        
        if(fServ == null){
            fServ = ReceptorManager.v().addServer(msg.getModName(), msg.getProtocol(), msg.getName());
        } else {
            if(fServ.getProtocol().isActive()){
                return new FailureMessage(msg, "Server (" + fServ.getName() + ") must be disconnected before modification");
            }
            
            fServ.changeFlowModule(mod);
            fServ.setProtocol(msg.getProtocol());
            ReceptorManager.v().save();
        }
        
        logger.info("Added Stream " + msg.getName() + " by " + this);
        return null;
    }
    
    private Message procRemoveStreamMessage(RemoveStreamMessage msg) throws IOException {
        FlowServer fServ = ReceptorManager.v().getServerByName(msg.getName());
        
        if(fServ != null){
            try {
                ReceptorManager.v().disconnectServer(fServ);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            ReceptorManager.v().removeServer(fServ);
        } else {
            return new FailureMessage(msg, "Server not found");
        }
        
        return null;
    }
    
    private Message procStreamConnectionMessage(StreamConnectionMessage msg) throws IOException, ResponceFailureException, ResponceDelayException, ReceptorManagerConnectInProcess {
        FlowServer fServ = ReceptorManager.v().getServerByName(msg.getName());
        
        if(fServ == null){
            throw new ResponceFailureException("Unable to find server by name: " + msg.getName());
        }
        
        if(msg.isConnect()){
            ReceptorManager.v().connectServer(fServ);
        } else {
            ReceptorManager.v().disconnectServer(fServ);
        }
        
        throw new ResponceDelayException("");
    }
    
    private Message procStartDatabaseAnalysisMessage(InstantiateAnalysisModuleMessage msg) throws IllegalAccessException, ClassNotFoundException, InstantiationException, ResponceFailureException{
        return state.startAnlzModule(msg.getModule());
    }
    

    private Message procGetModuleListeningListMessage(GetModuleListeningListMessage msg) throws ResponceDelayException, IOException{
        ModuleHandle handle = msg.getHandle();
        ReceptorProcessorModule mod = (ReceptorProcessorModule)state.getReportingModule(handle);
        
        ModuleListeningListMessage mllm = new ModuleListeningListMessage();
        mllm.setServers(ModuleManager.v().getModuleFlowServerList(mod));
        mllm.setHandle(handle);
        
        return mllm;
    }

    private Message procConnectModuleDefaultMessage(ConnectModuleDefaultMessage msg) {
        ProcMiddlewareModule module = (ProcMiddlewareModule) ModuleManager.v().getProcessorModule(msg.getModuleHandle());
        if(msg.isDoAdd()) {
            ReceptorManager.v().addDefaultConnect(module.getConnectTuple());
        } else {
            ReceptorManager.v().removeDefaultConnect(module.getConnectTuple());
        }
        
        return null;
    }
    
    private Message procConnectModuleMessage(ConnectModuleMessage msg) {
        FlowServer serv = ReceptorManager.v().getServerByName(msg.getFlowServer());
        ConnectTuple tuple = ModuleManager.v().getFlowProcessorConnectTuple(msg.getHandle());
        
        if(msg.isConnect()) {
            serv.addFlowProcessor(tuple);
        } else {
            serv.removeFlowProcessor(tuple);
        }
        
        ModuleConnectionStatusMessage resp = new ModuleConnectionStatusMessage();
        resp.setConnect(msg.isConnect());
        resp.setFlowServer(serv.getName());
        resp.setHandle(msg.getHandle());
        
        return resp;
    }
    
    private Message procGetModuleJarMessage(GetModuleJarMessage msg) throws FileNotFoundException {
        ModuleFile mFile = ModuleManager.v().getLinker().getModuleFile(msg.getModule(), msg.getType());
        File file = mFile.getPath();
        ModuleJarMessage resp = null;
        
        if(file != null) {
            resp = new ModuleJarMessage();
            LargeTransfer lt = new LargeTransfer();
            lt.setInputStream(new FileInputStream(file));
            resp.setModule(msg.getModule());
            resp.setFile(lt);
            
            Descriptor desc = mFile.getDescriptor();
            if(desc instanceof LibraryDescriptor) {
                LibraryDescriptor ld = (LibraryDescriptor)desc;
                resp.setModuleVersion(ld.version());
            }
        }
        
        return resp;
    }
    
    public void error(Object source, String msg) {
        ErrorMessage em = new ErrorMessage();
        em.setError(msg);
        try {
            sendMessage(em);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    
    private class Flusher implements Runnable {
        public void run(){
            while(doFlush){
                Util.threadSleep(300);
                flushMessages();
            }
        }
    }
}
