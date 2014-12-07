/*
 * AdminComm.java
 *
 * Created on September 27, 2006, 10:40 PM
 */

package eunomia.comm;

import eunomia.EunomiaEventer;
import eunomia.exception.ManagerException;
import eunomia.exception.ResponceFailureException;
import eunomia.managers.UserManager;
import eunomia.messages.Message;
import eunomia.messages.receptor.msg.cmd.admin.AddUserMessage;
import eunomia.messages.receptor.msg.cmd.admin.AdminMessage;
import eunomia.messages.receptor.msg.cmd.admin.ExecuteCommandMessage;
import eunomia.messages.receptor.msg.cmd.admin.RemoveUserMessage;
import eunomia.messages.receptor.msg.rsp.FailureMessage;
import eunomia.messages.receptor.ncm.LogMessage;
import java.io.IOException;
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
        } else if(msg instanceof AddUserMessage){
            resp = procAddUserMessage((AddUserMessage)msg);
        } else if(msg instanceof RemoveUserMessage) {
            resp = procRemoveUserMessage((RemoveUserMessage)msg);
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

    private Message procAddUserMessage(AddUserMessage msg) throws IOException {
        if(!UserManager.isValidName(msg.getUser())){
            return new FailureMessage(msg, "User name contains illegal charachters.");
        }
        
        try {
            UserManager.v().addUser(msg.getUser(), msg.getPass());
        } catch (ManagerException ex) {
            return new FailureMessage(msg, ex.getMessage());
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

}
