/*
 * AdminComm.java
 *
 * Created on September 27, 2006, 10:40 PM
 */

package eunomia.comm;

import eunomia.EunomiaEventer;
import eunomia.exception.ManagerException;
import eunomia.exception.ResponceFailureException;
import eunomia.managers.ModuleManager;
import eunomia.managers.UserManager;
import eunomia.managers.module.ModuleFile;
import eunomia.messages.Message;
import eunomia.messages.receptor.msg.cmd.admin.AdminSignalMessage;
import eunomia.messages.receptor.msg.cmd.admin.SetUserMessage;
import eunomia.messages.receptor.msg.cmd.admin.AdminMessage;
import eunomia.messages.receptor.msg.cmd.admin.ExecuteCommandMessage;
import eunomia.messages.receptor.msg.cmd.admin.RemoveUserMessage;
import eunomia.messages.receptor.msg.rsp.FailureMessage;
import eunomia.messages.receptor.msg.rsp.admin.AdminStatusMessage;
import eunomia.messages.receptor.ncm.LogMessage;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import org.apache.log4j.spi.LoggingEvent;

/**
 *
 * @author Mikhail Sosonkin
 */
public class AdminComm implements EunomiaEventer {
    private Shell shell;
    private ClientComm clientComm;
    
    public AdminComm(ClientComm cc) {
        clientComm = cc;
    }
    
    public void setShell(Shell sh){
        shell = sh;
    }
    
    public Message processAdminMessage(AdminMessage msg) throws ResponceFailureException, IOException {
        Message resp = null;
        
        if(msg instanceof ExecuteCommandMessage){
            resp = procExecuteCommandMessage((ExecuteCommandMessage)msg);
        } else if(msg instanceof SetUserMessage){
            resp = procSetUserMessage((SetUserMessage)msg);
        } else if(msg instanceof RemoveUserMessage) {
            resp = procRemoveUserMessage((RemoveUserMessage)msg);
        } else if(msg instanceof AdminSignalMessage) {
            resp = procAdminSignalMessage((AdminSignalMessage)msg);
        }
        
        return resp;
    }
    
    private Message procExecuteCommandMessage(ExecuteCommandMessage ecm) throws ResponceFailureException{
        if(shell == null){
            throw new ResponceFailureException("Shell not set");
        }
        shell.execute(ecm.getCommand());
        
        return null;
    }

    private Message procSetUserMessage(SetUserMessage msg) throws IOException {
        if(!UserManager.isValidName(msg.getUser())){
            return new FailureMessage(msg, "User name contains illegal charachters.");
        }
        
        String user = msg.getUser();
        String pass = msg.getPass();
        String old_pass = msg.getOldPass();
        msg.setPass(null);
        msg.setOldPass(null);
        if(UserManager.v().getPassHash(user) != null) {
            try {
                if(old_pass != null && !UserManager.v().checkPass(user, old_pass)) {
                    return new FailureMessage(msg, "Password incorrect");
                }
                
                UserManager.v().changePassword(user, pass);
            } catch (Exception ex) {
                return new FailureMessage(msg, ex.getMessage());
            }
        } else {
            try {
                UserManager.v().addUser(user, pass);
            } catch (ManagerException ex) {
                return new FailureMessage(msg, ex.getMessage());
            }
        }
        
        return null;
    }

    private Message procRemoveUserMessage(RemoveUserMessage msg) {
        String user = msg.getUsername();
        try {
            UserManager.v().removeUser(user);
        } catch (ManagerException ex) {
            return new FailureMessage(msg, ex.getMessage());
        }
        
        return null;
    }

    public void logEvent(LoggingEvent event) {
    	try {
            LogMessage msg = new LogMessage();
            msg.setMessage(event.getRenderedMessage());

            clientComm.sendMessage(msg);
    	} catch(Exception e) {
            e.printStackTrace();
    	}
    }

    private Message procAdminSignalMessage(AdminSignalMessage msg) throws ResponceFailureException {
        switch(msg.getSignal()) {
            case AdminSignalMessage.SIG_STATUS:
                return procAdminSignalMessage_SIG_STATUS();
                
            default: {
                throw new ResponceFailureException("Unknown signal: " + msg.getSignal());
            }
        }
    }

    private Message procAdminSignalMessage_SIG_STATUS() {
        String[] users = UserManager.v().getUsersList();
        List modules = ModuleManager.v().getLinker().getModuleList(-1);
        
        AdminStatusMessage msg = new AdminStatusMessage();
        msg.setUsers(users);
        
        Iterator it = modules.iterator();
        while (it.hasNext()) {
            ModuleFile mf = (ModuleFile) it.next();
            msg.addModule(mf.getType(), mf.getName(), mf.getDescriptor().shortDescription());
        }
        
        return msg;
    }
}
