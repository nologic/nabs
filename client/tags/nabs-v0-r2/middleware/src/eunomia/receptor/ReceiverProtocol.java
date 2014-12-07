/*
 * ReceiverProtocol.java
 *
 * Created on August 26, 2006, 10:33 PM
 *
 */

package eunomia.receptor;

import eunomia.messages.receptor.protocol.ProtocolDescriptor;
import eunomia.messages.receptor.protocol.impl.TCPProtocol;
import eunomia.messages.receptor.protocol.impl.UDPProtocol;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.AlreadyConnectedException;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SocketChannel;

/**
 *
 * @author Mikhail Sosonkin
 */
public class ReceiverProtocol {
    private ProtocolDescriptor descriptor;
    private SelectableChannel sChannel;
    private SocketAddress address;
    
    public ReceiverProtocol(ProtocolDescriptor desc) {
        descriptor = desc;
    }
    
    public ProtocolDescriptor getProtocolDescriptor(){
        return descriptor;
    }
    
    public SelectableChannel getSelectableChannel(){
        return sChannel;
    }
    
    public SelectableChannel openChannel() throws IOException, UnsupportedOperationException, AlreadyConnectedException {
        if(isActive()){
            throw new AlreadyConnectedException();
        }
        
        if(descriptor instanceof TCPProtocol){
            TCPProtocol tcp = (TCPProtocol)descriptor;
            SocketChannel sock = SocketChannel.open();
            address = new InetSocketAddress(tcp.getIp(), tcp.getPort());
            sock.configureBlocking(false);
            sChannel = sock;
        } else if(descriptor instanceof UDPProtocol){
            UDPProtocol udp = (UDPProtocol)descriptor;
            DatagramChannel sock = DatagramChannel.open();
            address = new InetSocketAddress(udp.getListenPort());
            sock.configureBlocking(false);
            sock.socket().bind(address);
            //sock.connect(address);
            System.out.println("add: " + address + " sock: " + sock.isConnected());
            sChannel = sock;
        } else {
            throw new UnsupportedOperationException("Protocol Not Supported: " + descriptor.protoString());
        }
        
        return sChannel;
    }
    
    public void activateChannel() throws IOException, UnsupportedOperationException {
        if(descriptor instanceof TCPProtocol){
            ((SocketChannel)sChannel).connect(address);
        } else if(descriptor instanceof UDPProtocol){
        } else {
            throw new UnsupportedOperationException("Protocol Not Supported: " + descriptor.protoString());
        }
    }
    
    public void closeChannel() throws IOException {
        if(sChannel != null){
            sChannel.close();
        }
        sChannel = null;
    }
    
    public boolean isActive(){
        return sChannel != null && sChannel.isOpen();
    }
    
    public String toString(){
        return descriptor.toString();
    }
    
    public static String getDescriptorString(ProtocolDescriptor desc){
        if(desc instanceof TCPProtocol){
            TCPProtocol tcp = (TCPProtocol)desc;
            return tcp.getIp() + ":" + tcp.getPort();
        } else if(desc instanceof UDPProtocol){
            UDPProtocol udp = (UDPProtocol)desc;
            return "" + udp.getListenPort();
        }
        
        return null;
    }
    
    public static ProtocolDescriptor getDescriptorObject(String proto, String str){
        ProtocolDescriptor desc = null;
        
        if(proto == null || str == null){
            return null;
        }
        
        
        if(proto.equals("TCP")){
            String[] split = str.split(":");
            TCPProtocol tcp = new TCPProtocol();
            tcp.setIp(split[0]);
            tcp.setPort(Integer.parseInt(split[1]));
            desc = tcp;
        } else if(proto.equals("UDP")){
            UDPProtocol udp = new UDPProtocol();
            udp.setListenPort(Integer.parseInt(str));
            desc = udp;
        }
        
        return desc;
    }
}