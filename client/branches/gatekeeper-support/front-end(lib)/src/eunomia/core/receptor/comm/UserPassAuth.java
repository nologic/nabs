/*
 * UserPassAuth.java
 *
 * Created on February 25, 2006, 7:05 PM
 */

package eunomia.core.receptor.comm;

import eunomia.core.receptor.comm.listeners.UserPassAuthListener;
import eunomia.core.receptor.listeners.ReceptorListener;
import eunomia.messages.Message;
import eunomia.messages.receptor.auth.AuthenticationMessage;
import eunomia.messages.receptor.auth.zero.*;
import java.io.*;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import org.apache.log4j.Logger;

/**
 *
 * @author Mikhail Sosonkin
 */
public class UserPassAuth {
    private String username;
    private byte[] randNumber;
    private byte[] passhash;
    private int state;
    private boolean isAuthenticated;
    private LinkedList listeners;
    
    private static Logger logger;
    private static final int
            NO_STATE = 0,
            REQUEST_SENT = 1,
            AUTH_CLIENT = 2,
            CL_CHALLAGE_SENT = 3,
            COMPLETE = 4;
    
    static {
        logger = Logger.getLogger(UserPassAuth.class);
    }
    
    public UserPassAuth(String uname, byte[] phash) {
        randNumber = new byte[16];
        username = uname;
        passhash = phash;
        state = NO_STATE;
        isAuthenticated = false;
        listeners = new LinkedList();
    }
    
    public void addUserPassAuthListener(UserPassAuthListener l){
        listeners.add(l);
    }
    
    public void removeUserPassAuthListener(UserPassAuthListener l){
        listeners.remove(l);
    }
    
    public Message startAuthentication() {
        RequestLoginMessage rlm = new RequestLoginMessage();
        rlm.setLogin(username);
        state = REQUEST_SENT;
        
        return rlm;
    }
    
    public Message updateState(AuthenticationMessage msg) throws GeneralSecurityException {
        switch(state){
            case REQUEST_SENT: {
                if(msg instanceof ChallangeMessage){
                    ChallangeMessage cm = (ChallangeMessage)msg;
                    ChallangeResponseMessage crm = new ChallangeResponseMessage();
                    crm.produceResponse(cm.getChallange(), passhash);
                    
                    state = AUTH_CLIENT;
                    return crm;
                }
            }
            
            case AUTH_CLIENT: {
                if(msg instanceof ChallangeCheckStatusMessage){
                    ChallangeCheckStatusMessage check = (ChallangeCheckStatusMessage)msg;
                    if(check.isOk()){
                        ChallangeMessage cm = new ChallangeMessage();
                        SecureRandom rand = new SecureRandom();
                        rand.nextBytes(randNumber);
                        cm.setChallange(randNumber, passhash);

                        state = CL_CHALLAGE_SENT;
                        return cm;
                    } else {
                        fireAuthenticationFailed();
                    }
                }
            }
            
            case CL_CHALLAGE_SENT: {
                state = COMPLETE;
                
                ChallangeResponseMessage crm = null;
                byte[] resp = null;
                if(msg instanceof ChallangeResponseMessage){
                    crm = (ChallangeResponseMessage)msg;
                    resp = crm.getResponse();
                    if(Arrays.equals(randNumber, resp)){
                        isAuthenticated = true;
                        fireAuthenticationSucceeded();
                    } else {
                        isAuthenticated = false;
                        fireAuthenticationFailed();
                    }
                }
                
                ChallangeCheckStatusMessage check  = new ChallangeCheckStatusMessage();
                check.setOk(isAuthenticated);
                return check;
            }
        }
        
        return null;
    }
    
    public Message failureMessage(AuthenticationMessage msg){
        fireAuthenticationFailed();
        return null;
    }
    
    private void fireAuthenticationFailed(){
        Iterator it = listeners.iterator();
        while (it.hasNext()) {
            UserPassAuthListener l = (UserPassAuthListener) it.next();
            l.authenticationFailed(this);
        }
    }
    
    private void fireAuthenticationSucceeded(){
        Iterator it = listeners.iterator();
        while (it.hasNext()) {
            UserPassAuthListener l = (UserPassAuthListener) it.next();
            l.authenticationSucceeded(this);
        }
    }

    public boolean isAuthenticated() {
        return isAuthenticated;
    }
    
    public String getUser() {
        return username;
    }
}