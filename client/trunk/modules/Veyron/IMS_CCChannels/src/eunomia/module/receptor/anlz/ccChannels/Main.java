/*
 * Main.java
 *
 * Created on November 3, 2007, 5:01 PM
 *
 */

package eunomia.module.receptor.anlz.ccChannels;

import com.vivic.eunomia.module.Descriptor;
import com.vivic.eunomia.module.receptor.ReceptorAnalysisModule;
import com.vivic.eunomia.filter.Filter;
import com.vivic.eunomia.sys.receptor.SieveContext;
import eunomia.messages.receptor.ModuleHandle;
import eunomia.module.receptor.libb.imsCore.NetworkSymbols;
import eunomia.module.receptor.libb.imsCore.NetworkTopology;
import eunomia.module.receptor.libb.imsCore.Reporter;
import eunomia.module.receptor.libb.imsCore.VeyronProcessingComponent;
import eunomia.module.receptor.libb.imsCore.listeners.NetworkChannelActivityListener;
import eunomia.module.receptor.libb.imsCore.listeners.NetworkChannelNewListener;
import eunomia.module.receptor.libb.imsCore.net.NetworkChannel;
import eunomia.module.receptor.libb.imsCore.net.NetworkChannelFlowID;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import org.apache.log4j.Logger;

/**
 *
 * @author Mikhail Sosonkin
 */
public class Main implements ReceptorAnalysisModule, VeyronProcessingComponent, NetworkChannelNewListener, NetworkChannelActivityListener {
    private double alpha = 0.9;
    private double beta = 0.8;
    private double gamma = 0.9;
    
    private Filter filter;
    private boolean doProc;
    private List listeners;
    private long[] typeCopy;

    private NetworkTopology net;
    private static Logger logger;
    
    private Reporter reporter;
    
    static {
        logger = Logger.getLogger(Main.class);
    }
    
    public Main() {
        filter = new Filter();
        doProc = false;
    }

    public void destroy() {
    }

    public void updateStatus(OutputStream out) throws IOException {
    }

    public void setControlData(InputStream in) throws IOException {
    }

    public void getControlData(OutputStream out) throws IOException {
    }

    public void processMessage(DataInputStream in, DataOutputStream out) throws IOException {
    }

    public void setProperty(String name, Object value) {
    }

    public Object getProperty(String name) {
        return null;
    }

    public Object[] getCommands() {
        return null;
    }

    public Object executeCommand(Object command, Object[] parameters) {
        return null;
    }

    private long getMediaCount(long[] types) {
        return types[NetworkChannel.DT_Audio_MP3] + types[NetworkChannel.DT_Audio_WAV] +
               types[NetworkChannel.DT_Image_BMP] + types[NetworkChannel.DT_Image_JPG] +
               types[NetworkChannel.DT_Video_MPG];
    }

    public void threadMain() {
        List list = SieveContext.getModuleManager().getModuleHandleList("bootIms", Descriptor.TYPE_ANLZ);
        
        if(list.size() > 0) {
            ModuleHandle handle = (ModuleHandle)list.get(0);
            ReceptorAnalysisModule mod = (ReceptorAnalysisModule)SieveContext.getModuleManager().getModule(handle);
            mod.setProperty("reg", this);
        } else {
            logger.error("No bootIms module instances found");
        }
    }

    public void initialize(NetworkTopology net, NetworkSymbols syms) {
        this.net = net;
        
        net.getNetworkListenerManager().addNetworkChannelNewListener(this);
    }
    
    public void setReporter(Reporter report) {
        reporter = report;
    }
    
    private boolean isPotentialChannel(NetworkChannel channel) {
        int[] shist = channel.src().getHistogram();
        int[] dhist = channel.dst().getHistogram();
        int cumPack256 = shist[NetworkChannel.HIST_0_256] + dhist[NetworkChannel.HIST_0_256];
        
        int allFlags = 0;
        int[] sflags = channel.src().getTcp_flags();
        int[] dflags = channel.dst().getTcp_flags();
        for(int i = 0; i < NetworkChannel.NUM_TCP_FLAGS; i++) {
            allFlags += sflags[i] + dflags[i];
        }
        
        int tcp_ack_psh = sflags[NetworkChannel.TCP_ACK] + sflags[NetworkChannel.TCP_PSH] +
                          dflags[NetworkChannel.TCP_ACK] + dflags[NetworkChannel.TCP_PSH];
        
        int packCount = channel.src().getPackets() + channel.dst().getPackets();
        int minPackSize = Math.min(channel.src().getMin_packet_size(), channel.dst().getMin_packet_size());
        int maxPackSize = Math.max(channel.src().getMax_packet_size(), channel.dst().getMax_packet_size());
        
        return (cumPack256 >= packCount * alpha) && 
               (tcp_ack_psh >= allFlags * beta) &&
               (minPackSize != maxPackSize) &&
               channel.getChannelFlowID().getProtocol() == NetworkChannelFlowID.PROTOCOL_TCP;
    }
    
    private long contentTotal(long[] content) {
        long sum = 0;
        
        for (int i = 0; i < content.length; i++) {
            sum += content[i];
        }
        
        return sum;
    }

    public NetworkChannelActivityListener newChannel(NetworkChannel chan) {
        if(isPotentialChannel(chan)) {
            long medContent = getMediaCount(chan.src().getContent()) + getMediaCount(chan.dst().getContent());
            long totContent = contentTotal(chan.src().getContent()) + contentTotal(chan.dst().getContent());
            
            if(medContent < totContent * gamma) {
                addCommandAndControl(chan);
            } else {
                return this;
            }
        }
        
        return null;
    }

    public boolean channelActivity(NetworkChannel chan) {
        long medContent = getMediaCount(chan.src().getContent()) + getMediaCount(chan.dst().getContent());
        
        if(isPotentialChannel(chan) && medContent < (chan.src().getByteSize() + chan.dst().getByteSize()) * gamma) {
            addCommandAndControl(chan);
            return false;
        }
        
        return true;
    }
    
    private void addCommandAndControl(NetworkChannel chan) {
        reporter.commandAndControlChannel(chan);
    }

    public boolean isStillInterested(NetworkChannelFlowID id, long notifyAge) {
        // we don't care about anything idle for more than 15s.
        return notifyAge < 15000;
    }
}