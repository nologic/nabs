/*
 * NabsClient.java
 *
 * Created on June 8, 2005, 4:41 PM
 */

package eunomia.core.data.streamData.client;

import java.net.*;
import java.io.*;
import eunomia.core.data.streamData.client.listeners.*;

import eunomia.core.data.flow.*;
import org.apache.log4j.Logger;

/**
 *
 * @author  Mikhail Sosonkin
 */
public class RawNabsClient implements Runnable, NabsClient {
    private Socket socket;
    private BufferedReader reader;
    private DataInputStream in;
    
    private int port;
    private String ip;
    private boolean isActive;
    private boolean isConnected;
    private FlowProcessor[] procs;
    private Filter filter;
    private int procCount;
    
    private static Logger logger;
    
    static {
        logger = Logger.getLogger(RawNabsClient.class);
    }
    
    public RawNabsClient(String ip, int port, int flowProcessors) {
        this.ip = ip;
        this.port = port;
        procs = new FlowProcessor[flowProcessors];
        procCount = 0;
        
        Thread thread = new Thread(this);
        thread.setName("RawClent: " + ip + ":" + port);
        thread.setPriority(Thread.MIN_PRIORITY);
        thread.start();
    }
    
    public RawNabsClient(String ip, int port){
        this(ip, port, 3);
    }
    
    public RawNabsClient(){
        this("127.0.0.1", 1986, 3);
    }
    
    public String getIP() {
        return ip;
    }
    
    public int getPort() {
        return port;
    }
    
    public void setServer(String ip, int port) {
        if(!isActive){
            this.ip = ip;
            this.port = port;
        }
    }
    
    public void connect() throws IOException {
        socket = new Socket(ip, port);
        in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
        isConnected = true;
    }
    
    public void disconnect() throws IOException {
        isConnected = false;
        in.close();
    }
    
    public void activate() throws IOException {
        isActive = true;
    }
    
    public void deactivate() throws IOException {
        isActive = false;
    }
    
    public boolean isActive(){
        return isActive;
    }
    
    // return value is no longer useful, but left here for future use.
    public boolean registerProcessor(FlowProcessor fp){
        boolean retVal = false;
        
        synchronized(procs){
            if(procCount == procs.length){
                FlowProcessor[] newProc = new FlowProcessor[2 * procs.length];
                System.arraycopy(procs, 0, newProc, 0, procs.length);
                procs = newProc;
            }

            FlowProcessor[] tmp = procs;
            for(int i = tmp.length - 1; i != -1; --i){
                if(tmp[i] == null){
                    ++procCount;
                    tmp[i] = fp;
                    retVal = true;
                    break;
                }
            }
        }

        return retVal;
    }
    
    public boolean deregisterProcessor(FlowProcessor fp){
        boolean retVal = false;
        
        synchronized(procs){
            for(int i = procs.length - 1; i != -1; --i){
                if(procs[i] == fp){
                    --procCount;
                    procs[i] = null;
                    retVal = true;
                    break;
                }
            }
        }
        
        return retVal;
    }
    
    private void notifyProcessors(Flow flow){
        Filter f = filter;
        
        if(f != null && !f.allow(flow)){
            return;
        }

        FlowProcessor[] tmp = procs;
        for(int i = tmp.length - 1; i != -1; --i){
            if(procs[i] != null){
                tmp[i].newFlow(flow);
            }
        }
    }
    
    public void run() {
        Flow flow = new Flow(this);
        
        while(true){
            if(isActive && isConnected){
                try{
                    flow.readFromDataStream(in);
                    notifyProcessors(flow);
                } catch(Exception e){
                    try {
                        disconnect();
                    } catch (Exception ex){
                        ex.printStackTrace();
                    }
                    e.printStackTrace();
                }
            } else {
                try {
                    Thread.sleep(200);
                } catch(Exception e){
                    e.printStackTrace();
                }
            }
        }
    }
    public void newFlow(Flow flow){
        // This function on the same stack for all receptors.
        if(isActive && flow.getNabsClient() != this && !flow.isPresent(this)){
            flow.addPresence(this);
            notifyProcessors(flow);
            flow.removePresence(this);
        }
    }

    public Filter getFilter() {
        return filter;
    }

    public void setFilter(Filter filter) {
        this.filter = filter;
    }
}