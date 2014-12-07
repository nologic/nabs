/*
 * ReceptorClassLocator.java
 *
 * Created on August 5, 2007, 4:03 PM
 *
 */

package eunomia.comm;

import eunomia.util.oo.*;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.log4j.Logger;

/**
 *
 * @author Mikhail Sosonkin
 */
public class ReceptorClassLocator implements NabClassLocator {
    private static HashMap classMap;
    private static Logger logger;
    private static AtomicBoolean mapLock;
    private static LookUpString lookup;
    
    public ReceptorClassLocator() {
    }

    public Class getClass(int hash, byte[] bytes, int len) throws IllegalAccessException, ClassNotFoundException {
        Class klass = mapClass(hash);

        if(klass == null){
            logger.info("Class not registered: " + new String(bytes, 0, len));
            String cname = new String(bytes, 0, len);
            
            klass = Class.forName(cname);
        }
        
        return klass;
    }

    public static void addClass(Class klass) {
        lock();
        classMap.put(klass.getName(), klass);
        unlock();
    }
    
    private static Class mapClass(int hash) {
        lock();
        lookup.hash = hash;
        Class klass = (Class)classMap.get(lookup);
        unlock();
        
        return klass;
    }
    
    private static void lock() {
        while(mapLock.compareAndSet(true, false));
    }
    
    private static void unlock() {
        mapLock.set(false);
    }
    
    static {
        mapLock = new AtomicBoolean(true);
        lookup = new LookUpString();
        logger = Logger.getLogger(ReceptorClassLocator.class);
        classMap = new HashMap();
        try {
            addClass(java.lang.String.class);
            
            addClass(eunomia.messages.DatabaseDescriptor.class);
            addClass(eunomia.messages.FilterEntryMessage.class);
            addClass(eunomia.messages.ByteArrayMessage.class);
            
            addClass(eunomia.messages.module.ModuleMessage.class);
            
            addClass(eunomia.messages.module.msg.ActionMessage.class);
            addClass(eunomia.messages.module.msg.AnalysisParametersMessage.class);
            addClass(eunomia.messages.module.msg.ChangeFilterMessage.class);
            addClass(eunomia.messages.module.msg.GenericModuleMessage.class);
            addClass(eunomia.messages.module.msg.GetAnalysisParametersMessage.class);
            addClass(eunomia.messages.module.msg.GetFilterListMessage.class);
            addClass(eunomia.messages.module.msg.GetModuleControlDataMessage.class);
            addClass(eunomia.messages.module.msg.ChangeFilterMessage.class);
            addClass(eunomia.messages.module.msg.InitialModuleStatusMessage.class);
            addClass(eunomia.messages.module.msg.ModuleControlDataMessage.class);
            addClass(eunomia.messages.module.msg.ModuleInterCommMessage.class);
            addClass(eunomia.messages.module.msg.ModuleStatusMessage.class);
            addClass(eunomia.messages.module.msg.StartAnalysisMessage.class);
            
            addClass(eunomia.messages.receptor.ModuleHandle.class);
            
            addClass(eunomia.messages.receptor.auth.zero.ChallangeCheckStatusMessage.class);
            addClass(eunomia.messages.receptor.auth.zero.ChallangeMessage.class);
            addClass(eunomia.messages.receptor.auth.zero.ChallangeResponseMessage.class);
            addClass(eunomia.messages.receptor.auth.zero.RequestLoginMessage.class);
            
            addClass(eunomia.messages.receptor.msg.cmd.AddDatabaseMessage.class);
            addClass(eunomia.messages.receptor.msg.cmd.AddStreamMessage.class);
            addClass(eunomia.messages.receptor.msg.cmd.CollectDatabaseMessage.class);
            addClass(eunomia.messages.receptor.msg.cmd.ConnectDatabaseMessage.class);
            addClass(eunomia.messages.receptor.msg.cmd.ConnectModuleDefaultMessage.class);
            addClass(eunomia.messages.receptor.msg.cmd.ConnectModuleMessage.class);
            addClass(eunomia.messages.receptor.msg.cmd.DatabaseQueryMessage.class);
            addClass(eunomia.messages.receptor.msg.cmd.GetAnalysisReportMessage.class);
            addClass(eunomia.messages.receptor.msg.cmd.GetModuleHandlesMessage.class);
            addClass(eunomia.messages.receptor.msg.cmd.GetModuleJarMessage.class);
            addClass(eunomia.messages.receptor.msg.cmd.GetModuleListeningListMessage.class);
            addClass(eunomia.messages.receptor.msg.cmd.GoAdminMessage.class);
            addClass(eunomia.messages.receptor.msg.cmd.InstantiateAnalysisModuleMessage.class);
            addClass(eunomia.messages.receptor.msg.cmd.InstantiateModuleMessage.class);
            addClass(eunomia.messages.receptor.msg.cmd.RemoveDatabaseMessage.class);
            addClass(eunomia.messages.receptor.msg.cmd.RemoveStreamMessage.class);
            addClass(eunomia.messages.receptor.msg.cmd.SignalMessage.class);
            addClass(eunomia.messages.receptor.msg.cmd.StreamConnectionMessage.class);
            addClass(eunomia.messages.receptor.msg.cmd.TerminateModuleMessage.class);
            
            addClass(eunomia.messages.receptor.msg.cmd.admin.AdminSignalMessage.class);
            addClass(eunomia.messages.receptor.msg.cmd.admin.ExecuteCommandMessage.class);
            addClass(eunomia.messages.receptor.msg.cmd.admin.RemoveUserMessage.class);
            addClass(eunomia.messages.receptor.msg.cmd.admin.SetUserMessage.class);
            
            addClass(eunomia.messages.receptor.msg.rsp.CommandResultMessage.class);
            addClass(eunomia.messages.receptor.msg.rsp.DatabaseQueryResultSetMessage.class);
            addClass(eunomia.messages.receptor.msg.rsp.FailureMessage.class);
            addClass(eunomia.messages.receptor.msg.rsp.ModuleHandleListMessage.class);
            addClass(eunomia.messages.receptor.msg.rsp.ModuleJarMessage.class);
            addClass(eunomia.messages.receptor.msg.rsp.ModuleListeningListMessage.class);
            addClass(eunomia.messages.receptor.msg.rsp.StatusMessage.class);
            addClass(eunomia.messages.receptor.msg.rsp.SuccessMessage.class);
            addClass(eunomia.messages.receptor.msg.rsp.UnknownMessage.class);
            
            addClass(eunomia.messages.receptor.msg.rsp.admin.AdminStatusMessage.class);
            
            addClass(eunomia.messages.receptor.ncm.AnalysisReportMessage.class);
            addClass(eunomia.messages.receptor.ncm.AnalysisSummaryMessage.class);
            addClass(eunomia.messages.receptor.ncm.ErrorMessage.class);
            addClass(eunomia.messages.receptor.ncm.LogMessage.class);
            addClass(eunomia.messages.receptor.ncm.ModuleConnectionStatusMessage.class);
            addClass(eunomia.messages.receptor.ncm.ServerConnectionStatusMessage.class);
            addClass(eunomia.messages.receptor.ncm.ShellLineMessage.class);
            
            addClass(eunomia.messages.receptor.protocol.impl.TCPProtocol.class);
            addClass(eunomia.messages.receptor.protocol.impl.UDPProtocol.class);
        } catch (Exception e){
            e.printStackTrace();
        }
    }
    
    private static class LookUpString {
        public int hash;

        public int hashCode(){
            return hash;
        }

        public boolean equals(Object o){
            return o.hashCode() == hash;
        }
    }
}