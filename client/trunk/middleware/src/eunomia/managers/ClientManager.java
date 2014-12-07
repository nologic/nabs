/*
 * ClientManager.java
 *
 * Created on September 8, 2005, 5:38 PM
 *
 */

package eunomia.managers;

import eunomia.EunomiaConfiguration;
import eunomia.comm.*;
import eunomia.messages.Message;
import eunomia.messages.receptor.ncm.ServerConnectionStatusMessage;
import eunomia.receptor.FlowServer;
import eunomia.receptor.listeners.ConnectionListener;
import java.util.logging.Level;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.*;
import java.io.*;
import java.net.*;
import javax.net.*;
import javax.net.ssl.*;
import java.security.KeyStore;
import java.util.LinkedList;
import java.util.List;
import org.apache.log4j.Logger;


/**
 *
 * @author Mikhail Sosonkin
 */
public class ClientManager implements Runnable, ConnectionListener {
    private static ClientManager instance;
    private static Logger logger;
    private static Thread cmThread;
    
    static {
        logger = Logger.getLogger(ClientManager.class);
    }
    
    private int listenPort;
    private List clientList;
    private ServerSocket serv;
    private ClientComm[] ccArr;
    
    public ClientManager() {
        clientList = new LinkedList();
    }
    
    public void shutdown() {
        logger.info("Closing client listener");
        try {
            serv.close();
        } catch(IOException ex) {
            ex.printStackTrace();
        }
        
        logger.info("Disconnecting Clients");
        if(ccArr != null) {
            for(int i = 0; i < ccArr.length; i++) {
                ClientComm client = ccArr[i];
                client.error(null, "Sieve Shutting down, bye bye");
                client.disconnect();
            }
        }
    }

    private boolean checkSSL() {
        try {
            Class.forName("com.sun.net.ssl.internal.ssl.Provider");
            return true;
        } catch(Exception e){
            logger.info("JSSE is not installed, reverting to unsecure socket.");
            return false;
        }
    }
    
    public void run(){
        try {
            Socket client;
            if(checkSSL()){
                SSLContext sslc = SSLContext.getInstance("TLSv1");
                KeyStore keystore = KeyStore.getInstance("JKS");
                keystore.load(ClassLoader.getSystemResourceAsStream("cert/KeyStore"), "zaq12wsx".toCharArray());
                
                KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
                kmf.init(keystore, "zaq12wsx".toCharArray());
                sslc.init(kmf.getKeyManagers(), null, null);
                
                SSLServerSocketFactory sslserversocketfactory = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
                SSLServerSocket sslserv = (SSLServerSocket) sslserversocketfactory.createServerSocket(listenPort);
                sslserv.setNeedClientAuth(false);
                sslserv.setEnabledCipherSuites(sslserv.getSupportedCipherSuites());
                serv = sslserv;
            } else {
                serv = new ServerSocket(listenPort);
            }
            
            while( (client = serv.accept()) != null){
                logger.info("Client Connection Established: " + client);
                ClientComm cc = new ClientComm(client);
                synchronized(clientList){
                    clientList.add(cc);
                    ccArr = (ClientComm[])clientList.toArray(new ClientComm[]{});
                }
            }
        } catch(Exception e){
            if(!serv.isClosed()) {
                e.printStackTrace();
            }
        }
    }
    
    public void clientDisconnected(ClientComm cc) {
        synchronized(clientList){
            clientList.remove(cc);
            ccArr = (ClientComm[])clientList.toArray(new ClientComm[]{});
        }
    }
    
    public void broadCastSend(Message msg) {
        if(ccArr != null) {
            ClientComm[] ccArrL = ccArr;
            for (int i = 0; i < ccArrL.length; i++) {
                try {
                    ccArrL[i].broadCaseRecv(msg);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }
    
    private void sendServerConnectionStatusMessage(FlowServer server, int status){
        ServerConnectionStatusMessage scsm = new ServerConnectionStatusMessage();
        scsm.setServer(server.getName());
        scsm.setStatus(status);
        
        broadCastSend(scsm);
    }
    
    public void connectionSuccessful(FlowServer server) {
        sendServerConnectionStatusMessage(server, ServerConnectionStatusMessage.CONNECTED);
    }
    
    public void connectionFailure(FlowServer server) {
        sendServerConnectionStatusMessage(server, ServerConnectionStatusMessage.CON_FAILURE);
    }
    
    public void connectionDropped(FlowServer server) {
        sendServerConnectionStatusMessage(server, ServerConnectionStatusMessage.DROPPED);
    }
    
    public void connectionClosed(FlowServer server) {
        sendServerConnectionStatusMessage(server, ServerConnectionStatusMessage.CLOSED);
    }
    
    public static boolean init(int port) {
        ClientManager man = v();
        
        if(cmThread == null) {
            man.listenPort = port;
            cmThread = new Thread(man, "Client Manager");
            cmThread.start();
            
            return true;
        }

        return false;
    }
    
    public static ClientManager v(){
        if(instance == null){
            instance = new ClientManager();
        }
        
        return instance;
    }
}