/*
 * ReceptorComm.java
 *
 * Created on October 10, 2006, 11:53 PM
 *
 */

package eunomia.core.receptor.comm;

import com.vivic.eunomia.sys.util.EunomiaUtils;
import eunomia.core.data.staticData.DatabaseTerminal;
import eunomia.core.managers.LibraryDescriptor;
import eunomia.core.managers.ModuleManager;
import eunomia.core.managers.exception.ManagerException;
import eunomia.core.receptor.Receptor;
import eunomia.core.receptor.ReceptorState;
import eunomia.core.receptor.comm.q.ReceiveQueue;
import eunomia.core.receptor.comm.q.SendQueue;
import com.vivic.eunomia.filter.Filter;
import eunomia.messages.DatabaseDescriptor;
import eunomia.messages.Message;
import eunomia.messages.module.ModuleMessage;
import eunomia.messages.module.msg.AnalysisParametersMessage;
import eunomia.messages.module.msg.ChangeFilterMessage;
import eunomia.messages.module.msg.ModuleControlDataMessage;
import eunomia.messages.module.msg.ModuleInterCommMessage;
import eunomia.messages.module.msg.ModuleStatusMessage;
import eunomia.messages.receptor.ModuleHandle;
import eunomia.messages.receptor.auth.AuthenticationMessage;
import eunomia.messages.receptor.msg.cmd.AddDatabaseMessage;
import eunomia.messages.receptor.msg.cmd.AddStreamMessage;
import eunomia.messages.receptor.msg.cmd.admin.SetUserMessage;
import eunomia.messages.receptor.msg.rsp.DatabaseQueryResultSetMessage;
import eunomia.messages.receptor.msg.rsp.ModuleJarMessage;
import eunomia.messages.receptor.msg.rsp.ModuleListeningListMessage;
import eunomia.messages.receptor.ncm.AnalysisSummaryMessage;
import eunomia.messages.receptor.msg.cmd.CollectDatabaseMessage;
import eunomia.messages.receptor.msg.cmd.ConnectDatabaseMessage;
import eunomia.messages.receptor.msg.cmd.DatabaseQueryMessage;
import eunomia.messages.receptor.msg.cmd.RemoveStreamMessage;
import eunomia.messages.receptor.msg.cmd.TerminateModuleMessage;
import eunomia.messages.receptor.msg.rsp.CommandResultMessage;
import eunomia.messages.receptor.msg.rsp.FailureMessage;
import eunomia.messages.receptor.msg.rsp.ModuleHandleListMessage;
import eunomia.messages.receptor.msg.rsp.StatusMessage;
import eunomia.messages.receptor.msg.rsp.SuccessMessage;
import eunomia.messages.receptor.msg.rsp.admin.AdminStatusMessage;
import eunomia.messages.receptor.ncm.AnalysisReportMessage;
import eunomia.messages.receptor.ncm.ErrorMessage;
import eunomia.messages.receptor.ncm.LogMessage;
import eunomia.messages.receptor.ncm.ModuleConnectionStatusMessage;
import eunomia.messages.receptor.ncm.ServerConnectionStatusMessage;
import eunomia.messages.receptor.ncm.ShellLineMessage;
import eunomia.module.AnlzFrontendModule;
import eunomia.module.FrontendModule;
import eunomia.module.ProcFrontendModule;
import eunomia.module.ReportingFrontendModule;
import eunomia.util.oo.LargeTransfer;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import org.apache.log4j.Logger;

/**
 *
 * @author Mikhail Sosonkin
 */
public class ReceptorComm implements Runnable {
    private SendQueue sendQueue;
    private ReceiveQueue recvQueue;
    private Receptor receptor;
    private ReceptorState state;
    private ModuleManager modManager;
    private boolean tRun;
    private Thread thread;
    private UserPassAuth auth;
    
    private static Logger logger;
    
    static {
        logger = Logger.getLogger(ReceptorComm.class);
    }
    
    public ReceptorComm(SendQueue sq, ReceiveQueue rq, Receptor rec) {
        sendQueue = sq;
        recvQueue = rq;
        receptor = rec;
        state = rec.getState();
        modManager = rec.getManager();
        tRun = true;
        
        thread = new Thread(this);
        thread.start();
    }
        
    public void setAuth(UserPassAuth a){
        auth = a;
    }
    
    public void run() {
        while(tRun){
            Message msg = recvQueue.get();

            if(msg != null){
                Message resp = null;
                try {
                    resp = processMessage(msg);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }

                if(resp != null){
                    sendMessage(resp);
                }
            }
        }
    }
    
    public void terminate(){
        tRun = false;
    }
    
    public void sendMessage(Message msg){
        sendQueue.put(msg);
    }
    
    private Message processMessage(Message msg) throws IOException{
        Message resp = null;

        if(msg instanceof CommandResultMessage){
            // stateful messages. For commands that are large
            // and/or we don't want them coming back.
            resp = procCommandResultMessage((CommandResultMessage)msg);
        } else if(msg instanceof ModuleMessage){
            procModuleMessage((ModuleMessage)msg);
        } else if(msg instanceof FailureMessage){
            procFailureMessage((FailureMessage)msg);
        } else if(msg instanceof SuccessMessage){
            resp = procSuccessMessage((SuccessMessage)msg);
        } else if(msg instanceof StatusMessage){
            procStatusMessage((StatusMessage)msg);
        } else if(msg instanceof ServerConnectionStatusMessage){
            procServerConnectionStatusMessage((ServerConnectionStatusMessage)msg);
        } else if(msg instanceof ErrorMessage){
            procErrorMessage((ErrorMessage)msg);
        } else if(msg instanceof LogMessage) {
            procLogMessage((LogMessage)msg);
        } else if(msg instanceof ModuleHandleListMessage){
            procModuleHandleListMessage((ModuleHandleListMessage)msg);
        } else if(msg instanceof ModuleHandle){
            procModuleHandle((ModuleHandle)msg);
        } else if(msg instanceof AuthenticationMessage){
            if(auth != null){
                resp = procAuthenticationMessage((AuthenticationMessage)msg);
            }
        } else if(msg instanceof ShellLineMessage){
            procShellLineMessage((ShellLineMessage)msg);
        } else if(msg instanceof ModuleListeningListMessage){
            procModuleListeningListMessage((ModuleListeningListMessage)msg);
        } else if(msg instanceof ModuleConnectionStatusMessage) {
            procModuleConnectionStatusMessage((ModuleConnectionStatusMessage)msg);
        } else if(msg instanceof DatabaseQueryResultSetMessage) {
            procDatabaseQueryResultSetMessage((DatabaseQueryResultSetMessage)msg);
        } else if(msg instanceof ModuleJarMessage) {
            procModuleJarMessage((ModuleJarMessage)msg);
        } else if(msg instanceof AdminStatusMessage) {
            procAdminStatusMessage((AdminStatusMessage)msg);
        }
        
        return resp;
    }
    
    private void procErrorMessage(ErrorMessage msg) {
    	logger.error("(" + receptor.getName() + ") " + msg.getError());
    }
    
    private void procLogMessage(LogMessage msg) {
    	logger.info("(" + receptor.getName() + ") " + msg.getMessage());
    }
    
    private Message procShellLineMessage(ShellLineMessage slm){
        logger.info("[" + receptor.getName() + "] " + slm.getLine());
        return null;
    }
    
    private Message procCommandResultMessage(CommandResultMessage msg) throws IOException{
        Message result = msg.getResult();
        
        return processMessage(result);
    }
    
    private Message procAuthenticationMessage(AuthenticationMessage am){
        try {
            return auth.updateState(am);
        } catch (GeneralSecurityException ex) {
            logger.error(ex.getMessage());
            ex.printStackTrace();
        }
        
        return null;
    }
    
    private Message procAuthenticationMessageF(AuthenticationMessage am){
        return auth.failureMessage(am);
    }
    
    private Message procSuccessMessage(SuccessMessage sm){
        Message resp = null;
        Message msg = sm.getCause();
        
        if(msg instanceof AddStreamMessage){
            procAddStreamMessageS((AddStreamMessage)msg);
        } else if(msg instanceof RemoveStreamMessage){
            procRemoveStreamMessageS((RemoveStreamMessage)msg);
        } else if(msg instanceof AddDatabaseMessage){
            procAddDatabaseMessageS((AddDatabaseMessage)msg);
        } else if(msg instanceof ConnectDatabaseMessage){
            procConnectDatabaseMessageS((ConnectDatabaseMessage)msg);
        } else if(msg instanceof CollectDatabaseMessage){
            procCollectDatabaseMessageS((CollectDatabaseMessage)msg);
        } else if(msg instanceof TerminateModuleMessage){
            procTerminateModuleMessageS((TerminateModuleMessage)msg);
        } else if(msg instanceof SetUserMessage) {
            procSetUserMessageS((SetUserMessage)msg);
        }
        
        return resp;
    }

    private Message procFailureMessage(FailureMessage fm){
        Message resp = null;
        Message msg = fm.getCause();
        
        if(msg instanceof AuthenticationMessage){
            resp = procAuthenticationMessageF((AuthenticationMessage)msg);
        } else if(msg instanceof DatabaseQueryMessage) {
            procDatabaseQueryMessageF((DatabaseQueryMessage)msg);
        } else {
            logger.warn(fm);
        }
        
        return resp;
    }

    private void procModuleHandleListMessage(ModuleHandleListMessage msg) {
        modManager.loadModuleList(msg.getHandles());
    }
    
    private void procModuleHandle(ModuleHandle msg) {
        modManager.moduleInstantiated(msg);
    }
    
    private void procModuleMessage(ModuleMessage msg) throws IOException {
        if(msg instanceof AnalysisParametersMessage){
            procAnalysisParametersMessage((AnalysisParametersMessage)msg);
        } else if(msg instanceof ModuleStatusMessage) {
            procModuleStatusMessage((ModuleStatusMessage)msg);
        } else if(msg instanceof ModuleControlDataMessage) {
            procModuleControlDataMessage((ModuleControlDataMessage)msg);
        } else if(msg instanceof ChangeFilterMessage) {
            procChangeFilterMessage((ChangeFilterMessage)msg);
        } else if(msg instanceof ModuleInterCommMessage){
            procModuleInterCommMessage((ModuleInterCommMessage)msg);
        }
    }
    
    private void procSetUserMessageS(SetUserMessage msg) {
        logger.info("New password for user '" + msg.getUser() + "' is now in effect.");
    }
    
    private void procTerminateModuleMessageS(TerminateModuleMessage tmm){
        modManager.moduleTerminated(tmm.getModuleHandle());
    }
    
    private void procAddStreamMessageS(AddStreamMessage asm){
        state.addStreamServer(asm.getName(), asm.getModName(), asm.getProtocol(), false);
    }
    
    private void procRemoveStreamMessageS(RemoveStreamMessage asm){
        state.removeStreamServer(asm.getName());
    }
    
    private void procAddDatabaseMessageS(AddDatabaseMessage asm){
        state.addDatabase(asm.getDbDescriptor());
    }
    
    private void procConnectDatabaseMessageS(ConnectDatabaseMessage asm){
        DatabaseDescriptor db = state.getDatabaseDescriptor(asm.getDbName());
        if(db != null){
            logger.info("Database " + db + " connection status: " + asm.isConnect());
            db.setConnected(asm.isConnect());
        }
    }
    
    private void procCollectDatabaseMessageS(CollectDatabaseMessage asm){
        DatabaseDescriptor db = state.getDatabaseDescriptor(asm.getDbName());
        if(db != null){
            if(asm.isCollect()) {
                db.addCollector(asm.getCollector());
            } else {
                db.removeCollector(asm.getCollector());
            }
        }
    }
    
    private void procServerConnectionStatusMessage(ServerConnectionStatusMessage msg) {
        state.setStreamServerStatus(msg.getServer(), msg.getStatus());
    }
        
    private void procStatusMessage(StatusMessage sm){
        int count = sm.getServerCount();
        for(int i = 0; i < count; i++){
            StatusMessage.StreamServer se = sm.getServer(i);
            state.addStreamServer(se.getName(), se.getModUsed(), se.getProtocol(), se.isConnected());
        }
        
        StatusMessage.ModuleDescriptor[] modules = sm.getModules();
        for (int i = 0; i < modules.length; i++) {
            int type = modules[i].getType();
            String name = modules[i].getName();
            switch(type) {
                case ModuleHandle.TYPE_ANLZ:
                    state.addAnalysisModule(name);
                    break;
                case ModuleHandle.TYPE_COLL:
                    state.addCollector(name);
                    break;
                case ModuleHandle.TYPE_FLOW:
                    state.addFlowModule(name);
                    break;
                case ModuleHandle.TYPE_PROC:
                    state.addModule(name);
                    break;
            }
            
            receptor.getManager().checkModuleHash(name, type, modules[i].getHash());
        }
        
        String[] mods = sm.getJdbcList();
        for (int i = 0; i < mods.length; i++) {
            state.addDatabaseType(mods[i]);
        }

        DatabaseDescriptor[] descs = sm.getDatabases();
        for(int i = 0; i < descs.length; i++){
            state.addDatabase(descs[i]);
        }
    }
    
    private void procAdminStatusMessage(AdminStatusMessage msg) {
        state.setUsers(msg.getUsers());
        state.setReceptorModules(msg.getModules());
    }
    
    private void procModuleStatusMessage(ModuleStatusMessage msg) throws IOException {
        if(msg.getLargeTransfer() != null && !msg.getLargeTransfer().isReceived()) {
            return;
        }
        
        ModuleHandle handle = msg.getModuleHandle();
        FrontendModule module = receptor.getManager().getModule(handle);
        try {
            if(module instanceof ReportingFrontendModule) {
                ReportingFrontendModule mod = (ReportingFrontendModule)module;
                mod.updateStatus(msg.getInputStream());
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(handle + " => " + e.getMessage());
        }
        
        receptor.getManager().moduleUpdated(module);
        msg.cleanup();
    }

    private void procModuleControlDataMessage(ModuleControlDataMessage msg) throws IOException {
        ModuleHandle handle = msg.getModuleHandle();
        ReportingFrontendModule mod = (ReportingFrontendModule)receptor.getManager().getModule(handle);

        mod.setControlData(msg.getInputStream());
    }

    private void procModuleListeningListMessage(ModuleListeningListMessage msg) {
        ProcFrontendModule mod = (ProcFrontendModule) receptor.getManager().getModule(msg.getHandle());
        if(mod != null){
            mod.resetStreamList();
            String[] servers = msg.getServers();
            for (int i = 0; i < servers.length; i++) {
                mod.addStream(state.getStreamServer(servers[i]));
            }
        }
        mod.fireStreamListUpdated();
    }

    private void procModuleConnectionStatusMessage(ModuleConnectionStatusMessage msg) {
        ProcFrontendModule mod = (ProcFrontendModule) receptor.getManager().getModule(msg.getHandle());
        if(mod != null){
            if(msg.isConnect()){
                mod.addStream(state.getStreamServer(msg.getFlowServer()));
            } else {
                mod.removeStream(state.getStreamServer(msg.getFlowServer()));
            }
        }
        mod.fireStreamListUpdated();
    }

    @Deprecated
    private void procAnalysisParametersMessage(AnalysisParametersMessage msg) {
        throw new RuntimeException("This method should not be used");
        /*AnlzFrontendModule mod = (AnlzFrontendModule) receptor.getManager().getModule(msg.getModuleHandle());
        
        if(mod != null){
            mod.setArguments(new DataInputStream(msg.getInputStream()));
            String[] dbs = msg.getDatabases();
            for (int i = 0; i < dbs.length; i++) {
                DatabaseDescriptor db = state.getDatabaseDescriptor(dbs[i]);
                if(db != null){
                    mod.addDatabase(db);
                }
            }
            mod.fireDatabaseListUpdated();
        }*/
    }

    private void procChangeFilterMessage(ChangeFilterMessage cfm) {
        ProcFrontendModule mod = (ProcFrontendModule) receptor.getManager().getModule(cfm.getModuleHandle());
        Filter filter = EunomiaUtils.makeFrontendFilter(cfm, receptor.getManager());
        
        receptor.getManager().filterReceived(filter, mod);
    }

    private void procModuleInterCommMessage(ModuleInterCommMessage msg) throws IOException {
        if(msg.getLargeTransfer() != null && !msg.getLargeTransfer().isReceived()) {
            return;
        }
        
        ReportingFrontendModule mod = (ReportingFrontendModule) receptor.getManager().getModule(msg.getModuleHandle());
        mod.processMessage(new DataInputStream(msg.getInputStream()));
        msg.cleanup();
    }

    private void procDatabaseQueryMessageF(DatabaseQueryMessage msg) {
        DatabaseTerminal term = receptor.getState().getTerminal(msg.getDbName());
        term.lastQueryFailed();
    }
    
    private void procDatabaseQueryResultSetMessage(DatabaseQueryResultSetMessage msg) {
        DatabaseTerminal term = receptor.getState().getTerminal(msg.getDb());
        LargeTransfer index = msg.getIndex();
        LargeTransfer result = msg.getResult();
        if(term != null && index.isReceived() && result.isReceived()) {
            term.setDataset(index.getDestinationFile(), result.getDestinationFile());
        }
    }

    private void procModuleJarMessage(ModuleJarMessage msg) throws IOException {
        LargeTransfer lt = msg.getFile();
        
        if(lt.isReceived()) {
            logger.info("Downloaded module '" + msg.getModule() + "'");
            try {
                if(msg.isLibrary()) {
                    receptor.getLinker().loadLibrary(lt.getDestinationFile(), new LibraryDescriptor(msg.getModule(), msg.getModuleVersion()));
                } else {
                    receptor.getLinker().loadModule(lt.getDestinationFile());
                }
            } catch (ManagerException ex) {
                ex.printStackTrace();
            }
        } else {
            File dir = receptor.getLinker().getModulesDir();
            File dest = File.createTempFile(msg.getModule(), ".jar", dir);
            lt.setDestinationFile(dest);
            logger.info("Downloading module '" + msg.getModule() + "' to " + dest);
        }
    }
}