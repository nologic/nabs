/*
 * Authenticator.java
 *
 * Created on February 21, 2006, 4:40 PM
 *
 */

package eunomia.comm.auth;

import eunomia.comm.auth.exceptions.AuthenticationException;
import eunomia.managers.*;
import eunomia.messages.Message;
import eunomia.messages.receptor.auth.AuthenticationMessage;
import eunomia.messages.receptor.auth.zero.*;
import java.io.*;
import java.security.*;
import java.util.*;
import org.apache.log4j.Logger;

/**
 *
 * @author Mikhail Sosonkin
 */
public class Authenticator {
    //private ObjectInput mesgInStream;
    //private ObjectOutput mesgOutStream;
    private String username;
    private byte[] randNumber;
    private int state;
    private boolean authenticated;
    private boolean allowRoot;
    
    private static Logger logger;
    public static final int
            NO_STATE = 0,
            CL_CHALLANGE_SENT = 1,
            SR_AUTH_SERVER = 2,
            DONE_STATE = 3;
    
    
    static {
        logger = Logger.getLogger(Authenticator.class);
    }
    
    public Authenticator(){
        randNumber = new byte[16];
        state = NO_STATE;
        authenticated = false;
        allowRoot = false;
    }
    
/*    public Authenticator(ObjectInput ois, ObjectOutput oos) {
        mesgInStream = ois;
        mesgOutStream = oos;
    }*/
    
    public boolean isAuthenticated(){
        return authenticated;
    }
    
    public boolean isInProcess(){
        return state != NO_STATE;
    }
    
    public Message startAuthentication(AuthenticationMessage msg) throws AuthenticationException, GeneralSecurityException {
        if(msg instanceof RequestLoginMessage){
            RequestLoginMessage rlm = (RequestLoginMessage)msg;
            username = rlm.getLogin();
            
            if(!allowRoot && username.equals("root")){
                throw new AuthenticationException("Must authenticate as a regular user first.");
            }
            
            // generate challange
            byte[] passhash = UserManager.v().getPassHash(username);
            if(passhash == null){
                throw new AuthenticationException("User has no password");
            }
            
            ChallangeMessage cm = new ChallangeMessage();
            SecureRandom rand = new SecureRandom();
            rand.nextBytes(randNumber);
            cm.setChallange(randNumber, passhash);
            state = CL_CHALLANGE_SENT;
            
            //send the challange
            return cm;
        } else if(msg instanceof ChallangeCheckStatusMessage){
            // After authentication is complete the client sends this message
            //  It can be safely ignored. We don't care that the client has
            //  actually authenticated us.
            return null;
        }
        
        throw new AuthenticationException("State Error");
    }
    
    public Message updateState(AuthenticationMessage msg) throws AuthenticationException, GeneralSecurityException {
        switch(state){
            case CL_CHALLANGE_SENT: {
                ChallangeResponseMessage crm = null;
                byte[] resp;
                if(msg instanceof ChallangeResponseMessage){
                    crm = (ChallangeResponseMessage)msg;
                    resp = crm.getResponse();
                } else {
                    throw new AuthenticationException("State Error");
                }
                
                if(!Arrays.equals(randNumber, resp)){
                    throw new AuthenticationException("Authentication Failed");
                }
                state = SR_AUTH_SERVER;
                // wait for challenge
                ChallangeCheckStatusMessage check = new ChallangeCheckStatusMessage();
                check.setOk(true);
                return check;
            }
            
            case SR_AUTH_SERVER: {
                ChallangeMessage cm = null;
                if(msg instanceof ChallangeMessage){
                    cm = (ChallangeMessage)msg;
                } else {
                    throw new AuthenticationException("State Error");
                }
                
                //send reponse
                ChallangeResponseMessage crm = new ChallangeResponseMessage();
                byte[] passhash = UserManager.v().getPassHash(username);
                if(passhash == null){
                    throw new AuthenticationException("User has no password");
                }
                crm.produceResponse(cm.getChallange(), passhash);
                
                authenticated = true;
                logger.info("User " + username + " logged in");
                state = DONE_STATE;
                // complete the challenege-response
                return crm;
            }
            
            default: {
                if(msg instanceof ChallangeCheckStatusMessage){
                    return null;
                }
            }
        }
        
        throw new AuthenticationException("State Incomplete");
    }
    
    // if the connection is alive then authentication is successfull
    /*private boolean authenticateClient() throws IOException {
        Message msg;
        // authenticating client;
        try {
            msg = (Message)mesgInStream.readObject();
     
            RequestLoginMessage rlm = null;
            if(msg instanceof RequestLoginMessage){
                rlm = (RequestLoginMessage)msg;
                username = rlm.getLogin();
            } else {
                return false;
            }
     
            // generate challange
            byte[] passhash = UserManager.v().getPassHash(username);
            if(passhash == null){
                return false;
            }
     
            ChallangeMessage cm = new ChallangeMessage();
            SecureRandom rand = new SecureRandom();
            rand.nextBytes(randNumber);
            cm.setChallange(randNumber, passhash);
     
            //send the challange
            mesgOutStream.writeObject(cm);
            mesgOutStream.flush();
            msg = (Message)mesgInStream.readObject();
     
            //check response
            ChallangeResponseMessage crm = null;
            byte[] resp;
            if(msg instanceof ChallangeResponseMessage){
                crm = (ChallangeResponseMessage)msg;
                resp = crm.getResponse();
            } else {
                return false;
            }
     
            if(!Arrays.equals(randNumber, resp)){
                return false;
            }
        } catch (ClassNotFoundException cnfe){
            cnfe.printStackTrace();
            return false;
        } catch (GeneralSecurityException gse){
            gse.printStackTrace();
            return false;
        }
     
        return true;
    }
     
    private boolean authenticateServer() throws IOException {
        Message msg;
     
        //authenticate server
        try {
            msg = (Message)mesgInStream.readObject();
     
            ChallangeMessage cm = null;
            if(msg instanceof ChallangeMessage){
                cm = (ChallangeMessage)msg;
            } else {
                return false;
            }
     
            //send reponse
            ChallangeResponseMessage crm = new ChallangeResponseMessage();
            byte[] passhash = UserManager.v().getPassHash(username);
            if(passhash == null){
                return false;
            }
            crm.produceResponse(cm.getChallange(), passhash);
     
            mesgOutStream.writeObject(crm);
            mesgOutStream.flush();
        } catch (ClassNotFoundException cnfe){
            cnfe.printStackTrace();
            return false;
        } catch (GeneralSecurityException gse){
            gse.printStackTrace();
            return false;
        }
     
        return true;
    }*/
    
    public String getUsername(){
        return username;
    }
    
    /*public boolean authenticate() throws IOException {
        if(authenticateClient()){
            return authenticateServer();
        }
     
        return false;
    }*/

    public boolean isAllowRoot() {
        return allowRoot;
    }

    public void setAllowRoot(boolean allowRoot) {
        this.allowRoot = allowRoot;
    }
}