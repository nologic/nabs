/*
 * Receptor.java
 *
 * Created on September 2, 2005, 5:34 PM
 *
 */

package eunomia.receptor;

import com.vivic.eunomia.module.Flow;
import eunomia.flow.*;
import com.vivic.eunomia.module.receptor.FlowCreator;
import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;
import org.apache.log4j.Logger;

/**
 *
 * @author Mikhail Sosonkin
 */
public class Receptor implements Runnable {
    private boolean isActive;
    private boolean isConnected;
    private Selector selector;
    private Selector conSel;
    private LoadBalancer balancer;
    
    // stats
    private long bytes;
    private int connections;
    
    private static Logger logger;
    
    static {
        logger = Logger.getLogger(Receptor.class);
    }
    
    public Receptor() throws IOException {
        bytes = 0;
        connections = 0;
        balancer = new LoadBalancer();
        selector = Selector.open();
        conSel = Selector.open();
        
        Thread thread = new Thread(this);
        thread.setName("Receptor");
        balancer.setReceptorThread(thread);
        thread.start();
    }
    
    public String getStats(){
        return "Connections: " + connections + " Received: " + bytes;
    }
    
    public void connect(FlowServer serv) throws IOException {
        SelectableChannel sock = serv.getProtocol().openChannel();
        if((sock.validOps() & SelectionKey.OP_CONNECT) != 0){
            sock.register(conSel, SelectionKey.OP_CONNECT, serv);
        } else {
            sock.register(selector, SelectionKey.OP_READ, serv);
            serv.fireConnectionSuccess();
        }
        serv.getProtocol().activateChannel();
        ++connections;
    }
    
    public void disconnect(FlowServer serv) throws IOException {
        ReceiverProtocol recv = serv.getProtocol();
        if(recv.isActive()){
            recv.closeChannel();
            serv.fireConnectionClosed();
        }
        --connections;
    }
    
    private void processConnection() throws IOException {
        int sel = conSel.selectNow();
        if(sel != 0){
            Iterator it = conSel.selectedKeys().iterator();
            while(it.hasNext()){
                SelectionKey sKey = (SelectionKey)it.next();
                it.remove();
                
                if(sKey.isValid()){
                    SocketChannel sock = (SocketChannel)sKey.channel();
                    FlowServer serv = (FlowServer)sKey.attachment();
                    if(sKey.isConnectable()){
                        try {
                            if(sock.finishConnect()){
                                serv.fireConnectionSuccess();
                                sock.register(selector, SelectionKey.OP_READ, serv);
                            }
                        } catch(Exception e){
                            ReceiverProtocol recv = serv.getProtocol();
                            recv.closeChannel();
                            serv.fireConnectionFailed();
                            --connections;
                            logger.error(e.getMessage());
                            e.printStackTrace();
                        }
                        
                        sKey.cancel();
                    }
                }
            }
        }
    }
    
    public void run() {
        Flow flow = null;
        
        while(true){
            try {
                processConnection();
                
                int sel = selector.select(1000);
                if(sel == 0){
                    continue;
                }
                
                Set keySet = selector.selectedKeys();
                Iterator it = keySet.iterator();
                while(it.hasNext()){
                    SelectionKey sKey = (SelectionKey)it.next();
                    FlowServer serv = (FlowServer)sKey.attachment();
                    ReadableByteChannel sock = (ReadableByteChannel)sKey.channel();
                    
                    if(sKey.isValid()){
                        if(sKey.isReadable()){
                            FlowCreator mod = serv.getFlowCreator();
                            ByteBuffer buffer = serv.getByteBuffer();
                            
                            int read = sock.read(buffer);
                            if(read > 0){
                                bytes += read;
                                buffer.flip();
                                while(buffer.remaining() >= mod.getNextFlowMinSize()){
                                    flow = mod.processBuffer(buffer);
                                    if(flow != null)
                                        serv.notifyProcessors(balancer, flow);
                                }
                                
                                //Not too great, but good. Max move of (Flow.FLOW_BYTE_SIZE - 1)
                                // Need a circular buffer.
                                buffer.compact();
                            } else {
                                // Collection lost;
                                logger.info("Connection dropped: " + serv);
                                
                                ReceiverProtocol recv = serv.getProtocol();
                                recv.closeChannel();
                                serv.fireConnectionDropped();
                                --connections;
                                sKey.cancel();
                                sock.close();
                            }
                        }
                    } else {
                        ReceiverProtocol recv = serv.getProtocol();
                        recv.closeChannel();
                        serv.fireConnectionDropped();
                        --connections;
                        sKey.cancel();
                        sock.close();
                    }
                    it.remove();
                }
            } catch(Exception e){
                logger.error(e.getMessage());
                e.printStackTrace();
            }
        }
    }
}