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
    
    private int listenPort;
    private List clientList;
    private ClientComm[] ccArr;
    
    public ClientManager(int port) {
        listenPort = port;
        clientList = new LinkedList();
        
        new Thread(this, "Client Manager").start();
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
            ServerSocket serv = null;
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
                ClientComm cc = new ClientComm(client);
                synchronized(clientList){
                    clientList.add(cc);
                    ccArr = (ClientComm[])clientList.toArray(new ClientComm[]{});
                }
            }
        } catch(Exception e){
            e.printStackTrace();
        }
    }
    
    private void load() throws IOException {
    }
    
    public void clientDisconnected(ClientComm cc) {
        synchronized(clientList){
            clientList.remove(cc);
            ccArr = (ClientComm[])clientList.toArray(new ClientComm[]{});
        }
    }
    
    public void broadCastSend(Message msg) {
        ClientComm[] ccArrL = ccArr;
        for (int i = 0; i < ccArrL.length; i++) {
            try {
                ccArrL[i].broadCaseRecv(msg);
            } catch (IOException ex) {
                ex.printStackTrace();
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
    
    public static ClientManager v(){
        if(instance == null){
            logger = Logger.getLogger(ClientManager.class);
            instance = new ClientManager(EunomiaConfiguration.getListenPort());
        }
        
        return instance;
    }
    
    public static void initialize() throws Exception {
        v().load();
    }
}