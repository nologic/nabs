/*
 * Receptor.java
 *
 * Created on October 20, 2005, 8:14 PM
 *
 */

package eunomia.core.receptor;

import eunomia.config.Config;
import eunomia.core.managers.*;
import eunomia.core.receptor.comm.ReceptorComm;
import eunomia.core.receptor.comm.ReceptorOutComm;
import eunomia.core.receptor.comm.UserPassAuth;
import eunomia.core.receptor.comm.listeners.UserPassAuthListener;
import eunomia.core.receptor.comm.q.ReceiveQueue;
import eunomia.core.receptor.comm.q.SendQueue;
import eunomia.core.receptor.comm.q.listeners.ReceiveQueueListener;
import eunomia.core.receptor.comm.q.listeners.SendQueueListener;
import eunomia.core.receptor.exception.*;
import eunomia.core.receptor.listeners.*;
import eunomia.messages.*;
import eunomia.util.Util;
import java.net.*;
import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import eunomia.util.oo.NabObjectInput;
import eunomia.util.oo.NabObjectOutput;
import org.apache.log4j.Logger;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

/**
 *
 * @author Mikhail Sosonkin
 */
public class Receptor implements UserPassAuthListener, ReceiveQueueListener, SendQueueListener {
    private String name;
    private String ip;
    private int port;
    private int refreshRate;

    private ModuleManager manager;
    private ReceptorState state;
    private Socket sock;
    private List listeners;

    private SendQueue sendQueue;
    private ReceiveQueue recvQueue;
    private ReceptorComm recComm;
    private ReceptorOutComm recOut;
    private UserPassAuth userAuth;
    private UserPassAuth adminAuth;

    private static Logger logger;

    static {
        logger = Logger.getLogger(Receptor.class);
    }

    public Receptor(String name) throws IOException {
        this(name, false);
    }

    public Receptor(String name, boolean load) throws IOException {
        this.name = name;

        recOut = new ReceptorOutComm(this);
        manager = new ModuleManager(this);
        state = new ReceptorState(this);
        listeners = new LinkedList();

        if(load){
            load();
        }
    }

    public ReceptorOutComm getOutComm(){
        return recOut;
    }

    public boolean isConnected(){
        if(sock == null){
            return false;
        }

        return !sock.isClosed();
    }

    public void addReceptorListener(ReceptorListener l){
        listeners.add(l);
    }

    public void removeReceptorListener(ReceptorListener l){
        listeners.remove(l);
    }

    private void fireReceptorDisconnected(){
        Iterator it = listeners.iterator();
        while(it.hasNext()){
            ((ReceptorListener)it.next()).receptorDisconnected(this);
        }
    }

    private void fireReceptorConnected(){
        Iterator it = listeners.iterator();
        while(it.hasNext()){
            ((ReceptorListener)it.next()).receptorConnected(this);
        }
    }

    public ReceptorState getState(){
        return state;
    }

    public String getIP(){
        return ip;
    }

    public int getPort(){
        return port;
    }

    public String getName(){
        return name;
    }

    public int getRefreshRate() {
        return refreshRate;
    }

    public void setRefreshRate(int refreshRate) {
        this.refreshRate = refreshRate;
        manager.wakeRefreshThread();
    }

    public void setIPPort(String ip, int port){
        this.ip = ip;
        this.port = port;
    }

    public void setCredentials(String uname, String pass) throws Exception {
        userAuth = new UserPassAuth(uname, Util.md5(pass.getBytes()));
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
    
    public void connect() throws UnknownHostException, IOException, AuthenticationFailureException {
        if(isConnected()) {
            logger.error("Receptor '" + this + "' is already connected");
            return;
        }
        
        if(checkSSL()){
            SSLSocketFactory sslsocketfactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
            SSLSocket sslsock = (SSLSocket) sslsocketfactory.createSocket(ip, port);
            sslsock.setEnabledCipherSuites(sslsock.getSupportedCipherSuites());
            sock = sslsock;
        } else {
            sock = new Socket(ip, port);
        }

        sendQueue = new SendQueue(new NabObjectOutput(sock.getOutputStream()));
        recvQueue = new ReceiveQueue(new NabObjectInput(sock.getInputStream()));
        recComm = new ReceptorComm(sendQueue, recvQueue, this);
        manager.reset();

        recvQueue.addReceptorListener(this);
        sendQueue.addReceptorListener(this);
        
        userAuth.addUserPassAuthListener(this);
        recComm.setAuth(userAuth);
        sendMessage(userAuth.startAuthentication());
    }

    public void authenticationFailed(UserPassAuth a) {
        logger.error(this + " Authentication failed, disconnecting.");
        try {
            disconnect();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void autheticateRoot(String pass) throws NoSuchAlgorithmException {
        adminAuth = new UserPassAuth("root", Util.md5(pass.getBytes()));
        adminAuth.addUserPassAuthListener(this);
        recComm.setAuth(adminAuth);
        sendMessage(adminAuth.startAuthentication());
    }

    public void authenticationSucceeded(UserPassAuth a) {
        if(a == userAuth){
            recvQueue.setAllowUnknowns(true);
            fireReceptorConnected();
        } else if(a == adminAuth){
            logger.info(this + " root authenticated");
        }
    }

    public void disconnect() throws IOException {
        if(sock != null){
            userAuth = null;
            sendQueue.terminate();
            sendQueue = null;

            recvQueue.terminate();
            recvQueue = null;

            recComm.terminate();
            recComm = null;

            sock.close();
            fireReceptorDisconnected();
        }
    }

    public ModuleManager getManager(){
        return manager;
    }

    public String toString(){
        return name;
    }

    public void sendMessage(Message msg, MessageReceiver recv){
        if(recComm != null){
            if(recv != null){
                recComm.sendSpecial(msg, recv);
            } else {
                recComm.sendMessage(msg);
            }
        }
    }

    public void sendMessage(Message msg){
        sendMessage(msg, null);
    }

    public void load() throws IOException {
        Config config = Config.getConfiguration("receptors." + name);

        ip = config.getString("IP", null);
        port = config.getInt("Port", -1);
        refreshRate = config.getInt("Rate", 1500);
    }

    public void save() throws IOException {
        Config config = Config.getConfiguration("receptors." + name);

        config.setString("IP", ip);
        config.setInt("Port", port);
        config.setInt("Rate", refreshRate);

        config.save();
    }

    public void caughtException(Exception e, ReceiveQueue queue) {
        try {
            disconnect();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void caughtException(Exception e, SendQueue queue) {
        try {
            disconnect();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}