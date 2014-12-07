/*
 * ClientComm.java
 *
 * Created on September 6, 2005, 12:56 PM
 *
 */

package eunomia.comm;

import eunomia.Main;
import eunomia.comm.auth.Authenticator;
import eunomia.comm.auth.exceptions.AuthenticationException;
import eunomia.comm.interfaces.ErrorMessenger;
import eunomia.data.*;
import eunomia.exception.*;
import eunomia.flow.*;
import eunomia.managers.*;
import eunomia.managers.connectable.ConnectTuple;
import eunomia.messages.*;
import eunomia.messages.module.*;
import eunomia.messages.module.msg.*;
import eunomia.messages.receptor.*;
import eunomia.messages.receptor.auth.AuthenticationMessage;
import eunomia.messages.receptor.msg.cmd.*;
import eunomia.messages.receptor.msg.cmd.admin.AdminMessage;
import eunomia.messages.receptor.msg.rsp.*;
import eunomia.messages.receptor.ncm.*;
import eunomia.modules.AnalysisModule;
import eunomia.plugin.interfaces.ReceptorModule;
import eunomia.receptor.FlowServer;
import eunomia.receptor.module.interfaces.FlowModule;
import eunomia.util.Util;
import eunomia.util.oo.*;

import java.io.*;
import java.net.*;
import java.security.GeneralSecurityException;
import javax.net.ssl.SSLException;
import java.util.*;

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

    private static Logger logger;

    static {
        logger = Logger.getLogger(ClientComm.class);
    }

    public ClientComm(Socket clientSocket) {
        cSock = clientSocket;

        logger.info("Client connected: " + cSock.getRemoteSocketAddress());

        new Thread(this).start();
    }

    public void run(){
        try {
            mesgInStream = new NabObjectInput(cSock.getInputStream());
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
                logger.error("Error reading message: " + se.getMessage());
                disconnect();
                return;
            } catch (IOException ioe){
                logger.error("Error reading message: " + ioe.getMessage());
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
                logger.error("Error writting message: " + ioe.getMessage());
                disconnect();
                ioe.printStackTrace();
                return;
            }
        }
    }
    
    private void disconnect() {
        logger.info("Client disconnected: " + cSock.getRemoteSocketAddress());
        Main.removeEventer(adminComm);
        ClientManager.v().clientDisconnected(this);
        doFlush = false;
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
            } else if(msg instanceof StartDatabaseAnalysisMessage){
                resp = procStartDatabaseAnalysisMessage((StartDatabaseAnalysisMessage)msg);
            } else if(msg instanceof GetProcessingSummaryMessage){
                resp = procGetProcessingSummaryMessage((GetProcessingSummaryMessage)msg);
            } else if(msg instanceof GetAnalysisReportMessage){
                resp = procGetAnalysisReportMessage((GetAnalysisReportMessage)msg);
            } else if(msg instanceof ConnectModuleMessage){
                resp = procConnectModuleMessage((ConnectModuleMessage)msg);
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

    private Message procTerminateModuleMessage(TerminateModuleMessage msg){
        state.terminateProcModule(msg.getModuleHandle());
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
                StatusMessage sm = new StatusMessage();
                FlowServer[] servs = ReceptorManager.v().getFlowServers();
                sm.setServersCount(servs.length);
                for(int i = 0; i < servs.length; i++){
                    FlowServer serv = servs[i];
                    String modName = ModuleManager.v().getFlowModuleName(serv.getFlowModule());
                    sm.setServer(i, serv.getProtocol().getProtocolDescriptor(), serv.getName(), modName, serv.getProtocol().isActive());
                }

                sm.setModuleNames((String[])ModuleManager.v().getModuleList().toArray(new String[]{}));
                sm.setAnalysisModuleNames((String[])ModuleManager.v().getAnalysisModuleNamesList().toArray(new String[]{}));
                sm.setJdbcList((String[])DatabaseManager.v().getJDBCList().toArray(new String[]{}));

                List dbList = DatabaseManager.v().getDatabaseList();
                sm.setDatabaseCount(dbList.size());
                Iterator it = dbList.iterator();
                for(int i = 0; it.hasNext(); i++){
                    Database db = (Database)it.next();
                    sm.setDatabase(i, db.getDescriptor(false));
                }

                List fModList = ModuleManager.v().getFlowModuleNamesList();
                String[] arr = (String[])fModList.toArray(new String[]{});
                sm.setFlowModuleNames(arr);

                return sm;
            }
            default: {
                //System.out.println("Unknown signal" + msg.getSignal());
                throw new ResponceFailureException("Unknown signal: " + msg.getSignal());
            }
        }
    }

    private Message processModuleMessage(Message msg) throws IOException, ResponceFailureException {
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
        } else if(msg instanceof GenericModuleMessage) {
            //make sure this is the last case.
            //so more specific messages get processed 1st.
            resp = procGenericModuleMessage((GenericModuleMessage)msg);
        }

        return resp;
    }

    public String toString(){
        return cSock.getRemoteSocketAddress().toString();
    }

    private Message procGetFilterListMessage(GetFilterListMessage gflm){
        ReceptorModule mod = state.getProcModule(gflm.getModuleID());

        Filter filter = mod.getFlowProcessor().getFilter();
        ChangeFilterMessage cfm = new ChangeFilterMessage();
        cfm.setModuleID(gflm.getModuleID());

        if(filter != null){
            FilterEntry[] entries = filter.getWhiteList().getAsArray();
            if(entries != null){
                FilterEntryMessage[] entriesMsg = new FilterEntryMessage[entries.length];
                for(int i = 0; i < entries.length; i++){
                    entriesMsg[i] = entries[i].getFilterEntryMessage();
                }
                cfm.setWhiteList(entriesMsg);
            }

            entries = filter.getBlackList().getAsArray();
            if(entries != null){
                FilterEntryMessage[] entriesMsg = new FilterEntryMessage[entries.length];
                for(int i = 0; i < entries.length; i++){
                    entriesMsg[i] = entries[i].getFilterEntryMessage();
                }
                cfm.setBlackList(entriesMsg);
            }
        }
        return cfm;
    }

    private Message procChangeFilterMessage(ChangeFilterMessage cfm){
        ReceptorModule mod = state.getProcModule(cfm.getModuleID());

        Filter filter = new Filter();

        FilterEntryMessage[] fems = cfm.getWhiteList();
        if(fems != null){
            for(int i = 0; i < fems.length; i++){
                FlowModule fmod = ModuleManager.v().getFlowModuleInstance(fems[i].getFlowModule());
                if(mod != null){
                    filter.addFilterWhite(fmod.getNewFilterEntry(fems[i]));
                }
            }
        }

        fems = cfm.getBlackList();
        if(fems != null){
            for(int i = 0; i < fems.length; i++){
                FlowModule fmod = ModuleManager.v().getFlowModuleInstance(fems[i].getFlowModule());
                if(mod != null){
                    filter.addFilterBlack(fmod.getNewFilterEntry(fems[i]));
                }
            }
        }

        mod.getFlowProcessor().setFilter(filter);
        cfm.resetForReturn();

        return null;
    }

    private Message procGenericModuleMessage(GenericModuleMessage gmm) throws IOException {
        ReceptorModule mod = state.getProcModule(gmm.getModuleID());
        return new CommandResultMessage(gmm, "", mod.processMessage(gmm));
    }

    private Message procActionMessage(ActionMessage msg){
        ReceptorModule mod = state.getProcModule(msg.getModuleID());

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
        ReceptorModule mod = state.getProcModule(msg.getModuleID());
        if(mod != null){
            ModuleControlDataMessage mcdm = new ModuleControlDataMessage();
            mcdm.setModuleID(msg.getModuleID());

            mod.getControlData(mcdm.getOutputStream());

            return mcdm;
        } else {
            logger.warn("Module instance " + msg.getModuleID() + " not found");
        }

        throw new ResponceFailureException("Module instance " + msg.getModuleID() + " not found");
    }

    private Message procSetModuleControlDataMessage(ModuleControlDataMessage msg) throws IOException {
        ReceptorModule mod = state.getProcModule(msg.getModuleID());
        mod.setControlData(msg.getInputStream());

        return null;
    }

    private Message procGetModuleStatusMessage(ModuleStatusMessage msg) throws IOException {
        ReceptorModule mod = state.getProcModule(msg.getModuleID());
        mod.updateStatus(msg.getOutputStream());

        return msg;
    }

    private Message procGetModuleHandlesMessage(GetModuleHandlesMessage msg){
        ModuleHandleListMessage mhlm = new ModuleHandleListMessage();

        Iterator it = state.getProcHandles().iterator();
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

        try {
            ConnectTuple tuple = new ConnectTuple();
            tuple.setFlowProcessor(db.getCollector());
            ReceptorManager.v().removeFlowProcessor(tuple);
        } catch (Exception e){
            // Maybe the collector wasn't active.
        }

        db.disconnect();
        DatabaseManager.v().removeDatabase(db);
        DatabaseManager.v().saveDatabases();

        return null;
    }

    private Message procCollectDatabaseMessage(CollectDatabaseMessage cdm) throws Exception {
        Database db = DatabaseManager.v().getDatabaseByName(cdm.getDbName());

        if(cdm.isCollect()){
            if(!db.isCollecting()){
                FlowProcessor proc = db.getCollector();
                ConnectTuple tuple = new ConnectTuple();
                tuple.setFlowProcessor(proc);
                ReceptorManager.v().addFlowProcessor(tuple);
                db.setCollecting(true);
            }
        } else {
            if(db.isCollecting()){
                FlowProcessor proc = db.getCollector();
                ConnectTuple tuple = new ConnectTuple();
                tuple.setFlowProcessor(proc);
                ReceptorManager.v().removeFlowProcessor(tuple);
                db.setCollecting(false);
            }
        }
        return null;
    }

    private Message procDatabaseQueryMessage(DatabaseQueryMessage msg) throws IOException, Exception{
        Database db = DatabaseManager.v().getDatabaseByName(msg.getDbName());
        DatabaseTerminal dt = DatabaseManager.v().openTerminal(db, cSock.getInetAddress().getHostAddress(), msg.getQuery());
        DatabaseTerminalOpenMessage dtom = new DatabaseTerminalOpenMessage();

        dt.setErrorMessenger(this);

        dtom.setPort1(dt.getPort1());
        dtom.setPort2(dt.getPort2());
        dtom.setRandom1(dt.getRandom1());
        dtom.setRandom2(dt.getRandom2());

        return new CommandResultMessage(msg, "", dtom);
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

    private Message procStartDatabaseAnalysisMessage(StartDatabaseAnalysisMessage msg) throws IllegalAccessException, ClassNotFoundException, InstantiationException, ResponceFailureException{
        Database db = DatabaseManager.v().getDatabaseByName(msg.getDb());
        if(db == null){
            throw new ResponceFailureException("Unable to find database by name: " + msg.getDb());
        }

        AnalysisModule mod = state.startAnlzModule(msg.getModule());
        AnalysisThread newThread = new AnalysisThread(db.getThreadGroup(), mod, msg.getParams(), db);
        newThread.start();

        return null;
    }

    private Message procGetProcessingSummaryMessage(GetProcessingSummaryMessage msg) throws ResponceFailureException {
        Database db = DatabaseManager.v().getDatabaseByName(msg.getDatabaseName());
        if(db == null){
            throw new ResponceFailureException("Unable to find database by name: " + msg.getDatabaseName());
        }
        
        Iterator it = db.getAnalysisThreads().iterator();
        AnalysisSummaryMessage asm = new AnalysisSummaryMessage();
        asm.setDatabase(db.getName());
        
        while (it.hasNext()) {
            AnalysisThread thread = (AnalysisThread) it.next();
            AnalysisModule mod = thread.getModule();
            ModuleHandle handle = mod.getHandle();
            if(state.containsAnlzHandle(handle)) {
                AnalysisSummaryMessage.SUM sum = new AnalysisSummaryMessage.SUM(mod.getModuleName(), handle, mod.getProgress());
                asm.addSummary(sum);
            }
        }
        
        return asm;
    }

    private Message procGetModuleListeningListMessage(GetModuleListeningListMessage msg) throws ResponceDelayException, IOException{
        ModuleHandle handle = msg.getHandle();
        ModuleListeningListMessage resp = getModuleListeningListMessage(handle.getInstanceID());
        resp.setHandle(handle);
        
        return resp;
    }

    private ModuleListeningListMessage getModuleListeningListMessage(int id){
        ReceptorModule mod = state.getProcModule(id);
        ModuleListeningListMessage mllm = new ModuleListeningListMessage();
        mllm.setServers(ModuleManager.v().getModuleFlowServerList(mod));

        return mllm;
    }

    private Message procGetAnalysisReportMessage(GetAnalysisReportMessage msg) {
        AnalysisReportMessage arm = new AnalysisReportMessage();
        AnalysisModule mod = ModuleManager.v().getAnalysisModule(msg.getHandle());
        
        arm.setHandle(msg.getHandle());
        mod.getResult(arm.getReportOutputStream());
        
        return arm;
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
