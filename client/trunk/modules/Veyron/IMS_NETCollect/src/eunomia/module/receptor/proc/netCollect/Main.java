/*
 * Main.java
 *
 * Created on January 14, 2008, 8:23 PM
 *
 */

package eunomia.module.receptor.proc.netCollect;

import com.vivic.eunomia.sys.util.Util;
import com.vivic.eunomia.filter.Filter;
import com.vivic.eunomia.module.flow.Flow;
import com.vivic.eunomia.module.flow.FlowModule;
import com.vivic.eunomia.module.receptor.FlowProcessor;
import com.vivic.eunomia.module.receptor.ReceptorProcessorModule;
import eunomia.module.receptor.libb.imsCore.NetworkSymbols;
import eunomia.module.receptor.libb.imsCore.NetworkTopology;
import eunomia.module.receptor.libb.imsCore.Reporter;
import eunomia.module.receptor.libb.imsCore.VeyronProcessingComponent;
import eunomia.module.receptor.libb.imsCore.db.NetworkInserter;
import eunomia.module.receptor.libb.imsCore.net.DarkAccess;
import eunomia.module.receptor.libb.imsCore.net.Network;
import eunomia.module.receptor.libb.imsCore.net.NetworkChannel;
import eunomia.module.receptor.libb.imsCore.net.NetworkChannel.ChnContent;
import eunomia.module.receptor.libb.imsCore.net.NetworkChannelFlowID;
import eunomia.module.receptor.libb.imsCore.net.NetworkDefinition;
import eunomia.module.receptor.libb.imsCore.net.NetworkEntity;
import eunomia.module.receptor.libb.imsCore.net.NetworkEntityHostKey;
import eunomia.module.receptor.libb.imsCore.util.ComponentRegistry;
import eunomia.module.receptor.libb.imsCore.util.MicroTime;
import eunomia.receptor.module.NABFlowV2.NABFlowV2;
import eunomia.receptor.module.NEOFlow.NEOFlow;
import eunomia.receptor.module.NEOFlow.TimeStamp;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 *
 * @author Mikhail Sosonkin
 */
public class Main implements ReceptorProcessorModule, FlowProcessor, VeyronProcessingComponent, Runnable {
    private volatile long sensorTime;
    private volatile boolean doProc;
    private NetworkInserter nins;
    private NetworkDefinition ndef;
    private NetworkChannelFlowID flowId;
    
    private NetworkEntityHostKey bSrcEnt;
    private NetworkEntityHostKey bDstEnt;
    
    private DarkAccess daTmp;
    
    private RTBuffer rtBuff;
    private long received;
    
    /* buffed Required flow data objects */
    private long[] typeCopy;
    private int[] tcpFlags;
    private int[] histogram;
    private MicroTime minInterArrivalTime;
    private MicroTime maxInterArrivalTime;
    private MicroTime firstSYNpackTime;
    private MicroTime firstSYNACKpackTime;
    private MicroTime firstACKpackTime;
    private MicroTime startTime;
    private MicroTime endTime;

    public Main() {
        doProc = false;
        
        rtBuff = new RTBuffer();
        
        typeCopy = new long[NetworkChannel.NUM_TYPES];
        tcpFlags = new int[NetworkChannel.NUM_TCP_FLAGS];
        histogram = new int[NetworkChannel.NUM_HISTOGRAM];
        minInterArrivalTime = new MicroTime(false);
        maxInterArrivalTime = new MicroTime(true);
        firstSYNpackTime = new MicroTime(false);
        firstSYNACKpackTime = new MicroTime(false);
        firstACKpackTime = new MicroTime(false);
        startTime = new MicroTime(false);
        endTime = new MicroTime(true);
        
        bSrcEnt = new NetworkEntityHostKey();
        bDstEnt = new NetworkEntityHostKey();
        flowId = new NetworkChannelFlowID();
        flowId.setKey(new NetworkEntityHostKey(), new NetworkEntityHostKey(), 0, 0, 0);
        
        daTmp = new DarkAccess(null);
        
        ComponentRegistry.getInstance().registerProcessingComponent(this);
        
        new Thread(this).start();
    }

    public boolean accept(FlowModule module) {
        Flow flow = module.getNewFlowInstance();
        return flow instanceof NABFlowV2 || flow instanceof NEOFlow;
    }

    public void initialize(NetworkTopology net, NetworkSymbols syms) {
        ndef = net.getNetworkDefinition();
    }
    
    public void setNetworkInserter(NetworkInserter nins) {
        this.nins = nins;
        
        doProc = true;
    }
    
    public void run() {
        while(true) {
            boolean doWait = false;
            long largestTime;
            
            if(rtBuff.isArchReady()) {
                NetworkChannel[] chans = rtBuff.getChannelArchArray();
                nins.flow_addChannels(chans, 0, chans.length);
                largestTime = nins.flow_getLargestTime();
                
                NetworkEntity[] ents = rtBuff.getEntityArchArray();
                nins.flow_addEntities(ents, 0, ents.length);
                largestTime = Math.max(largestTime, nins.flow_getLargestTime());
                
                nins.flow_setLastActivity(largestTime);
                
                System.out.println("Commited: " + chans.length + " + " + ents.length + " Back log: " + this.getBackLog() + " size: " + rtBuff.getSize());
                System.out.println("Time: " + Util.getTimeStamp(sensorTime*1000L));
                doWait = false;
            } else {
                doWait = true;
            }
            
            if(doWait) {
                Util.threadSleep(100);
            }
        }
    }

    public void newFlow(Flow flow) {
        // Flow Specific data.
        long[] typeCopy = this.typeCopy;
        int[] tcpFlags = this.tcpFlags;
        int[] histogram = this.histogram;
        MicroTime minInterArrivalTime = this.minInterArrivalTime;
        MicroTime maxInterArrivalTime = this.maxInterArrivalTime;
        MicroTime firstSYNpackTime = this.firstSYNpackTime;
        MicroTime firstSYNACKpackTime = this.firstSYNACKpackTime;
        MicroTime firstACKpackTime = this.firstACKpackTime;
        MicroTime startTime = this.startTime;
        MicroTime endTime = this.endTime;
    
        NetworkChannelFlowID flowId = this.flowId;

        byte proto, tos;
        int packetCount;
        int pMinSize;
        int pMaxSize;
        int frags;
        short minTTL = 0;
        short maxTTL = 0;
        
        if(!doProc) {
            return;
        }
        
        // Flow general data.
        long sIp = flow.getSourceIP();
        long dIp = flow.getDestinationIP();
        int sPort = flow.getSourcePort();
        int dPort = flow.getDestinationPort();
        int size = flow.getSize();

        // we only care about flows in network.
        if(!ndef.isInNetowrk(sIp) && !ndef.isInNetowrk(dIp)) {
            return;
        }

        if(flow instanceof NABFlowV2) {
            NABFlowV2 f = (NABFlowV2)flow;
        
            int[] types = f.getTypeCount();
            for (int i = 0; i < types.length; i++) {
                typeCopy[i] = (long)types[i] * (long)NABFlowV2.MAX_PAYLOAD;
            }
            
            tos = 0;
            frags = 0;
            proto = f.getProtocol();
            endTime.set(f.getEndTimeSeconds(), 0);
            startTime.set(f.getStartTimeSeconds(), 0);
            packetCount = f.getPackets();
            pMinSize = f.getMin_packet_size();
            pMaxSize = f.getMax_packet_size();
            
            tcpFlags[NetworkChannel.TCP_SYN] = f.getTcpFlag(NABFlowV2.TCP_SYN);
            tcpFlags[NetworkChannel.TCP_ACK] = f.getTcpFlag(NABFlowV2.TCP_ACK);
            tcpFlags[NetworkChannel.TCP_FIN] = f.getTcpFlag(NABFlowV2.TCP_FIN);
            tcpFlags[NetworkChannel.TCP_RST] = f.getTcpFlag(NABFlowV2.TCP_RST);
            tcpFlags[NetworkChannel.TCP_URG] = f.getTcpFlag(NABFlowV2.TCP_URG);
            tcpFlags[NetworkChannel.TCP_PSH] = f.getTcpFlag(NABFlowV2.TCP_PSH);
            
            histogram[NetworkChannel.HIST_0_256] = f.getHistogram(0) + f.getHistogram(1) + f.getHistogram(2) + f.getHistogram(3);
            histogram[NetworkChannel.HIST_257_512] = f.getHistogram(4) + f.getHistogram(5);
            histogram[NetworkChannel.HIST_513_768] = f.getHistogram(6);
            histogram[NetworkChannel.HIST_769_1024] = f.getHistogram(7);
            histogram[NetworkChannel.HIST_1025_1280] = f.getHistogram(8);
        } else if(flow instanceof NEOFlow) {
            NEOFlow f = (NEOFlow)flow;
        
            int[] types = f.getTypeCount();
            for (int i = 0; i < types.length; i++) {
                typeCopy[i] = (long)types[i] * (long)NEOFlow.MAX_PAYLOAD;
            }
            
            TimeStamp t = f.getEndTime();
            endTime.set(t.getSeconds(), t.getMicroSeconds());
            
            t = f.getStartTime();
            startTime.set(t.getSeconds(), t.getMicroSeconds());
            
            tos = f.getTos();
            proto = f.getProtocol();
            packetCount = f.getPackets();
            pMinSize = f.getMin_packet_size();
            pMaxSize = f.getMax_packet_size();
            frags = f.getFragCount();
            maxTTL = f.getMax_ttl();
            minTTL = f.getMin_ttl();
            
            tcpFlags[NetworkChannel.TCP_SYN] = f.getTcpFlag(NABFlowV2.TCP_SYN);
            tcpFlags[NetworkChannel.TCP_ACK] = f.getTcpFlag(NABFlowV2.TCP_ACK);
            tcpFlags[NetworkChannel.TCP_FIN] = f.getTcpFlag(NABFlowV2.TCP_FIN);
            tcpFlags[NetworkChannel.TCP_RST] = f.getTcpFlag(NABFlowV2.TCP_RST);
            tcpFlags[NetworkChannel.TCP_URG] = f.getTcpFlag(NABFlowV2.TCP_URG);
            tcpFlags[NetworkChannel.TCP_PSH] = f.getTcpFlag(NABFlowV2.TCP_PSH);
            
            System.arraycopy(f.getHistogram(), 0, histogram, 0, NetworkChannel.NUM_HISTOGRAM);
        } else {
            return;
        }
        
        long eTimeMillis = endTime.getSeconds();
        if(sensorTime < eTimeMillis) {
            sensorTime = eTimeMillis;
        }
        
        flowId.getSourceEntity().setIPv4(sIp);
        flowId.getDestinationEntity().setIPv4(dIp);
        flowId.setPortsProto(sPort, dPort, proto);
        
        if(size <= 0) {
            DarkAccess da = daTmp.clone();
            
            nins.configureDarkAccessKey(da);
            
            da.setFlowID(flowId);
            da.flow_setData(packetCount, pMaxSize, pMinSize, minTTL, maxTTL, frags, tos, tcpFlags);
            da.flow_setTimes(startTime, endTime, minInterArrivalTime, maxInterArrivalTime, firstSYNpackTime, firstSYNACKpackTime, firstACKpackTime);
            
            nins.flow_addDarkAccess(new DarkAccess[]{da}, 0, 1);
        } else {
            // process channels.
            boolean isNew = false;
            boolean isFlipped = false;
            
            NetworkChannel ch = rtBuff.lookup(flowId);
            if(ch == null) {
                isNew = true;
                ch = rtBuff.getCleanChannel(flowId);
                nins.configureChannelKey(ch);
            }
            
            ChnContent cont = ch.src();
            if(isNew && !ch.getChannelFlowID().getSourceEntity().equals(flowId.getSourceEntity())) {
                // check if the original direction is actually wrong.
                MicroTime oldStartTime = ch.getStartTime();
                if(oldStartTime.compareTo(startTime) == 1) {
                    // new flow is earlier, therefore it represents the correct direction.
                    ch.flip();
                    isFlipped = true;
                } else {
                    cont = ch.dst();
                }
            }
            
            ch.flow_addOccurrences(1);
            ch.flow_setTimes(startTime, endTime);
            ch.setTos(tos);
            
            cont.flow_addData(size, packetCount, typeCopy, tcpFlags, histogram, pMaxSize, pMinSize, frags);
            cont.flow_setTimes(minInterArrivalTime, maxInterArrivalTime, firstSYNpackTime, firstSYNACKpackTime, firstACKpackTime);
            cont.flow_setTtls(minTTL, maxTTL);
            
            if(tcpFlags[NetworkChannel.TCP_RST] > 0 || tcpFlags[NetworkChannel.TCP_FIN] >= 2) {
                rtBuff.flagForArchiving(ch, isFlipped);
            } else {
                rtBuff.put(ch, isNew, isFlipped);
            }
            
            // process hosts
            NetworkEntityHostKey srcHost = bSrcEnt;
            NetworkEntityHostKey dstHost = bDstEnt;
            NetworkEntity sHost;
            NetworkEntity dHost;
            
            srcHost.setIPv4(sIp);
            dstHost.setIPv4(dIp);
            
            sHost = rtBuff.lookup(srcHost);
            dHost = rtBuff.lookup(dstHost);
            
            if(sHost == null) {
                sHost = rtBuff.getCleanEntity(srcHost);
                nins.configureEntityKey(sHost);
                rtBuff.put(sHost, true);
            }
            
            if(dHost == null) {
                dHost = rtBuff.getCleanEntity(dstHost);
                nins.configureEntityKey(dHost);
                rtBuff.put(dHost, true);
            }
            
            sHost.setTimes(startTime, endTime);
            dHost.setTimes(startTime, endTime);
            
            sHost.src().flow_addData((long)size, typeCopy);
            sHost.dst().flow_addData((long)size, typeCopy);
        }
        
        rtBuff.scanTimeOuts(sensorTime);
        ++received;
    }
    
    private boolean isDarkAccess(int size, int synCount) {
        return size == 0 && synCount > 0;
    }

    public void setReporter(Reporter report) {
    }
    
    public double getCommitRate() {
        return 0.0;//bProc.getCommitRate();
    }
    
    public long getBackLog() {
        return 0;//bProc.getReceived() - bProc.getCommited();
    }
    
    public void destroy() {
    }

    public FlowProcessor getFlowProcessor() {
        return this;
    }

    public void updateStatus(OutputStream out) throws IOException {
    }

    public void setControlData(InputStream in) throws IOException {
    }

    public void getControlData(OutputStream out) throws IOException {
    }

    public void processMessage(DataInputStream in, DataOutputStream out) throws IOException {
    }

    public void start() {
        doProc = true;
    }

    public void stop() {
        doProc = false;
    }

    public void reset() {
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

    public void setFilter(Filter filter) {
    }

    public Filter getFilter() {
        return null;
    }
}