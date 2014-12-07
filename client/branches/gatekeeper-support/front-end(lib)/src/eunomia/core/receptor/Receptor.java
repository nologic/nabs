/*
 * Receptor.java
 *
 * Created on October 20, 2005, 8:14 PM
 *
 */

package eunomia.core.receptor;

import com.vivic.eunomia.sys.frontend.ConsoleReceptor;
import com.vivic.eunomia.sys.frontend.GlobalSettings;
import eunomia.config.Config;
import eunomia.config.Settings;
import eunomia.core.managers.ModuleLinker;
import eunomia.core.managers.ModuleManager;
import eunomia.core.receptor.comm.ReceptorComm;
import eunomia.core.receptor.comm.ReceptorOutComm;
import eunomia.core.receptor.comm.UserPassAuth;
import eunomia.core.receptor.comm.listeners.UserPassAuthListener;
import eunomia.core.receptor.comm.q.ReceiveQueue;
import eunomia.core.receptor.comm.q.SendQueue;
import eunomia.core.receptor.comm.q.listeners.ReceiveQueueListener;
import eunomia.core.receptor.comm.q.listeners.SendQueueListener;
import eunomia.core.receptor.exception.AuthenticationFailureException;
import eunomia.core.receptor.listeners.ReceptorListener;
import eunomia.messages.Message;
import eunomia.util.Util;
import java.security.NoSuchAlgorithmException;
import eunomia.util.oo.NabObjectInput;
import eunomia.util.oo.NabObjectOutput;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.apache.log4j.Logger;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

/**
 *
 * @author Mikhail Sosonkin
 */
public class Receptor implements UserPassAuthListener, ReceiveQueueListener, SendQueueListener, ConsoleReceptor {
    private int serialNumber;
    
    private String name;
    private String ip;
    private int port;
    private int refreshRate;

    private ModuleManager manager;
    private ReceptorState state;
    private ModuleLinker linker;
    private ReceptorClassLocator cLoc;
    
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

    public Receptor(int serial) {
        serialNumber = serial;
        
        cLoc = new ReceptorClassLocator();
        linker = new ModuleLinker(Integer.toString(serialNumber, 36), this);
        recOut = new ReceptorOutComm(this);
        manager = new ModuleManager(this);
        state = new ReceptorState(this);

        listeners = new LinkedList();
    }
    
    public GlobalSettings getGlobalSettings() {
        return Settings.v();
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
    
    public boolean isAuthenticated() {
        return userAuth != null && userAuth.isAuthenticated();
    }
    
    public boolean isRootAuthenticated() {
        return (isAuthenticated() && userAuth.getUser().equals("root")) ||
               (adminAuth != null && adminAuth.isAuthenticated());
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
    
    public ModuleLinker getLinker() {
        return linker;
    }
    
    public ReceptorClassLocator getClassLocator() {
        return cLoc;
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
            logger.error(" '" + this + "' is already connected");
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
        
        manager.reset();
        state.reset();
        sendQueue = new SendQueue(new NabObjectOutput(sock.getOutputStream()));
        recvQueue = new ReceiveQueue(new NabObjectInput(sock.getInputStream(), cLoc));
        recComm = new ReceptorComm(sendQueue, recvQueue, this);

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
            
            if(sendQueue != null) {
                sendQueue.terminate();
                sendQueue = null;
            }

            if(recvQueue != null) {
                recvQueue.terminate();
                recvQueue = null;
            }

            if(recComm != null) {
                recComm.terminate();
                recComm = null;
            }

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

    public void sendMessage(Message msg){
        if(recComm != null){
            recComm.sendMessage(msg);
        }
    }
    
    public void setProperty(String key, String value) {
        Config config = Config.getConfiguration("receptors." + name + ".property");
        config.setString(key, value);
        
        config.save();
    }
    
    public String getProperty(String key) {
        Config config = Config.getConfiguration("receptors." + name + ".property");
        return config.getString(key, null);
    }

    public void load() {
        Config config = Config.getConfiguration("receptors." + serialNumber);

        name = config.getString("Name", null);
        ip = config.getString("IP", null);
        port = config.getInt("Port", -1);
        refreshRate = config.getInt("Rate", 1500);
    }

    public void save() {
        Config config = Config.getConfiguration("receptors." + serialNumber);

        config.setString("Name", name);
        config.setString("IP", ip);
        config.setInt("Port", port);
        config.setInt("Rate", refreshRate);

        config.save();
    }

    public void caughtException(Exception e, ReceiveQueue queue) {
        try {
            e.printStackTrace();
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

    public int getSerialNumber() {
        return serialNumber;
    }

    public void setName(String name) {
        this.name = name;
    }
}