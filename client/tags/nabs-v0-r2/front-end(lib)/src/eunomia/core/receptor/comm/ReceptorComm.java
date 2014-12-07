/*
 * ReceptorComm.java
 *
 * Created on October 10, 2006, 11:53 PM
 *
 */

package eunomia.core.receptor.comm;

import eunomia.core.managers.ModuleManager;
import eunomia.core.receptor.Receptor;
import eunomia.core.receptor.ReceptorState;
import eunomia.core.receptor.comm.q.ReceiveQueue;
import eunomia.core.receptor.comm.q.SendQueue;
import eunomia.core.receptor.listeners.MessageReceiver;
import eunomia.messages.DatabaseDescriptor;
import eunomia.messages.Message;
import eunomia.messages.module.ModuleMessage;
import eunomia.messages.receptor.ModuleHandle;
import eunomia.messages.receptor.auth.AuthenticationMessage;
import eunomia.messages.receptor.msg.cmd.AddDatabaseMessage;
import eunomia.messages.receptor.msg.cmd.AddStreamMessage;
import eunomia.messages.receptor.msg.rsp.ModuleListeningListMessage;
import eunomia.messages.receptor.ncm.AnalysisSummaryMessage;
import eunomia.messages.receptor.msg.cmd.CollectDatabaseMessage;
import eunomia.messages.receptor.msg.cmd.ConnectDatabaseMessage;
import eunomia.messages.receptor.msg.cmd.RemoveStreamMessage;
import eunomia.messages.receptor.msg.cmd.TerminateModuleMessage;
import eunomia.messages.receptor.msg.rsp.CommandResultMessage;
import eunomia.messages.receptor.msg.rsp.FailureMessage;
import eunomia.messages.receptor.msg.rsp.ModuleHandleListMessage;
import eunomia.messages.receptor.msg.rsp.StatusMessage;
import eunomia.messages.receptor.msg.rsp.SuccessMessage;
import eunomia.messages.receptor.ncm.AnalysisReportMessage;
import eunomia.messages.receptor.ncm.ErrorMessage;
import eunomia.messages.receptor.ncm.LogMessage;
import eunomia.messages.receptor.ncm.ModuleConnectionStatusMessage;
import eunomia.messages.receptor.ncm.ServerConnectionStatusMessage;
import eunomia.messages.receptor.ncm.ShellLineMessage;
import eunomia.plugin.GUIPlugin;
import eunomia.util.number.ModInteger;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;
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
    
    // request mapping
    private Map sentMessageMap;
    private ModInteger hasher;
    
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
        
        sentMessageMap = new HashMap();
        hasher = new ModInteger();
        thread = new Thread(this);
        thread.start();
    }
    
    private MessageReceiver getRecieverForHash(int hash){
        Object o = null;
        synchronized(sentMessageMap){
            hasher.setInt(hash);
            o = sentMessageMap.remove(hasher);
        }
        
        return (MessageReceiver)o;
    }
    
    private void putRecieverForHash(int hash, MessageReceiver rcv){
        synchronized(sentMessageMap){
            ModInteger mint = new ModInteger();
            mint.setInt(hash);
            sentMessageMap.put(mint, rcv);
        }
    }
    
    public void setAuth(UserPassAuth a){
        auth = a;
    }
    
    public void run() {
        while(tRun){
            Message msg = recvQueue.get();
            if(msg == null){
                continue;
            }
            
            Message resp = processMessage(msg);
            
            if(resp != null){
                sendMessage(resp);
            }
        }
    }
    
    public void terminate(){
        tRun = false;
    }
    
    public void sendSpecial(Message msg, MessageReceiver recv){
        putRecieverForHash(msg.hashCode(), recv);
        sendMessage(msg);
    }
    
    public void sendMessage(Message msg){
        sendQueue.put(msg);
    }
    
    private Message processMessage(Message msg){
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
        } else if(msg instanceof AnalysisSummaryMessage){
            procAnalysisSummaryMessage((AnalysisSummaryMessage)msg);
        } else if(msg instanceof AnalysisReportMessage){
            procAnalysisReportMessage((AnalysisReportMessage)msg);
        } else if(msg instanceof ModuleListeningListMessage){
            procModuleListeningListMessage((ModuleListeningListMessage)msg);
        } else if(msg instanceof ModuleConnectionStatusMessage) {
            procModuleConnectionStatusMessage((ModuleConnectionStatusMessage)msg);
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
    
    private Message procCommandResultMessage(CommandResultMessage msg){
        Message cause = msg.getCause();
        MessageReceiver receiver = getRecieverForHash(cause.hashCode());
        Message result = msg.getResult();
        
        if(receiver != null){
            receiver.messageResponse(result);
        } else {
            return processMessage(result);
        }
        
        return null;
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
        }
        
        return resp;
    }

    private Message procFailureMessage(FailureMessage fm){
        Message resp = null;
        Message msg = fm.getCause();
        
        if(msg instanceof AuthenticationMessage){
            resp = procAuthenticationMessageF((AuthenticationMessage)msg);
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
    
    private void procModuleMessage(ModuleMessage msg) {
        try {
            modManager.processModuleMessage(msg);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    private void procTerminateModuleMessageS(TerminateModuleMessage tmm){
        modManager.moduleTerminated(tmm.getModuleHandle());
    }
    
    private void procAddStreamMessageS(AddStreamMessage asm){
        state.addStreamServer(asm.getName(), asm.getModName(), asm.getProtocol(), false);
        state.fireReceptorStateChanged();
    }
    
    private void procRemoveStreamMessageS(RemoveStreamMessage asm){
        state.removeStreamServer(asm.getName());
        state.fireReceptorStateChanged();
    }
    
    private void procAddDatabaseMessageS(AddDatabaseMessage asm){
        state.addDatabase(asm.getDbDescriptor());
        state.fireReceptorStateChanged();
    }
    
    private void procConnectDatabaseMessageS(ConnectDatabaseMessage asm){
        DatabaseDescriptor db = state.getDatabaseDescriptor(asm.getDbName());
        if(db != null){
            logger.info("Database " + db + " connection status: " + asm.isConnect());
            db.setConnected(asm.isConnect());
        }
        state.fireReceptorStateChanged();
    }
    
    private void procCollectDatabaseMessageS(CollectDatabaseMessage asm){
        DatabaseDescriptor db = state.getDatabaseDescriptor(asm.getDbName());
        if(db != null){
            db.setCollecting(asm.isCollect());
        }
        state.fireReceptorStateChanged();
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
        String[] mods = sm.getModuleNames();
        for(int i = 0; i < mods.length; i++){
            state.addModule(mods[i]);
        }
        mods = sm.getFlowModuleNames();
        for(int i = 0; i < mods.length; i++){
            state.addFlowModule(mods[i]);
        }
        
        mods = sm.getAnalysisModuleNames();
        for(int i = 0; i < mods.length; i++){
            state.addAnalysisModule(mods[i]);
        }
        
        mods = sm.getJdbcList();
        for (int i = 0; i < mods.length; i++) {
            state.addDatabaseType(mods[i]);
        }
        
        DatabaseDescriptor[] descs = sm.getDatabases();
        for(int i = 0; i < descs.length; i++){
            state.addDatabase(descs[i]);
        }
        state.fireReceptorStateChanged();
    }

    private void procAnalysisSummaryMessage(AnalysisSummaryMessage msg) {
        receptor.getManager().addDatabaseReport(msg);
    }

    private void procAnalysisReportMessage(AnalysisReportMessage msg) {
        receptor.getManager().analysisModuleReport(msg);
    }

    private void procModuleListeningListMessage(ModuleListeningListMessage msg) {
        GUIPlugin mod = (GUIPlugin) receptor.getManager().getModuleByHandle(msg.getHandle());
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
        GUIPlugin mod = (GUIPlugin) receptor.getManager().getModuleByHandle(msg.getHandle());
        if(mod != null){
            if(msg.isConnect()){
                mod.addStream(state.getStreamServer(msg.getFlowServer()));
            } else {
                mod.removeStream(state.getStreamServer(msg.getFlowServer()));
            }
        }
        mod.fireStreamListUpdated();
    }
}