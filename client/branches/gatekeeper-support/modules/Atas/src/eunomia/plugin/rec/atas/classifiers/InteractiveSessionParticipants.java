/*
 * InteractiveSessionParticipants.java
 *
 * Created on March 29, 2007, 9:31 PM
 *
 */

package eunomia.plugin.rec.atas.classifiers;

import eunomia.plugin.com.atas.ClassifierConfigurationMessage;
import eunomia.plugin.com.atas.HostInfo;
import eunomia.plugin.com.atas.RoleInterface;
import eunomia.plugin.rec.atas.classifiers.msg.InteractiveSessionConfigMessage;
import eunomia.receptor.module.NABFlowV2.NABFlowV2;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author kulesh
 */
public class InteractiveSessionParticipants implements RoleInterface{
    /* default params for interactive sessions */
    private double alpha = 0.9;
    private double beta = 0.2;
    private double epsilon = 0.8;
   
    private ConcurrentHashMap<Long, HostInfo> roleMap;
    private int roleNumber;
    private static final String roleName = "Interactive";
    
    /** Creates a new instance of InteractiveSessionParticipants */
    public InteractiveSessionParticipants() {
        roleMap= new ConcurrentHashMap<Long, HostInfo>();
    }

    public int hostCount() {
        return roleMap.size();
    }

    public int getRoleNumber() {
        return roleNumber;
    }

    public void setRoleNumber(int n) {
        roleNumber = n;
    }

    public String getRoleName() {
        return roleName;
    }

    public Collection getCollection() {
        return roleMap.entrySet();
    }

    public HostInfo getHostInfo(long ip){
        return roleMap.get(ip);
    }
    
    public void processFlowRecord(NABFlowV2 flowRecord) {
        if(flowRecord.getProtocol() != NABFlowV2.PROTOCOL_TCP)
            return;
        
        if(flowRecord.getMax_packet_size() == flowRecord.getMin_packet_size())
            return;
        
        int totalPackets = flowRecord.getPackets();
        int totalBytes = flowRecord.getSize();
        int total_below_256 = flowRecord.getHistogram(1) + flowRecord.getHistogram(2) + flowRecord.getHistogram(3);
        int total_between_256_511 = flowRecord.getHistogram(4) + flowRecord.getHistogram(5);
        int total_flags = flowRecord.getTcp_acks() + flowRecord.getTcp_fins() + flowRecord.getTcp_push() +
                flowRecord.getTcp_rsts() + flowRecord.getTcp_syns() + flowRecord.getTcp_urgs();

        
        if((total_below_256 >= (totalPackets * getAlpha())) &&
                (total_between_256_511 >= (totalPackets * getBeta())) &&
                ((flowRecord.getTcp_push()/flowRecord.getTcp_acks()) >= (getEpsilon() * total_flags))){
            
            updateRoleMap(flowRecord);
        }
    }
    
    private void updateRoleMap(NABFlowV2 flowRecord){
        long sourceIp= flowRecord.getSourceIP();
        HostInfo tmp= roleMap.get(sourceIp);
        
        if(tmp == null){
            tmp= new HostInfo(sourceIp);
            tmp.setLastSeen(flowRecord.getTime());
            tmp.incrementBytes(flowRecord.getSize());
            tmp.incrementPackets(flowRecord.getPackets());
            roleMap.put(sourceIp, tmp);
        } else {
            tmp.setLastSeen(flowRecord.getTime());
            tmp.incrementBytes(flowRecord.getSize());
            tmp.incrementPackets(flowRecord.getPackets());
        }
    }
    
    public ClassifierConfigurationMessage getConfigurationMessage() {
        InteractiveSessionConfigMessage msg = new InteractiveSessionConfigMessage();
        
        msg.setRoleName(this.getRoleName());
        msg.setRoleNumber(this.getRoleNumber());
        msg.setAlpha(alpha);
        msg.setBeta(beta);
        msg.setEpsilon(epsilon);
        
        return msg;
    }

    public void setConfigurationMessage(ClassifierConfigurationMessage msg) {
        InteractiveSessionConfigMessage imsg = (InteractiveSessionConfigMessage)msg;
        
        alpha = imsg.getAlpha();
        beta = imsg.getBeta();
        epsilon = imsg.getEpsilon();
    }

    public double getAlpha() {
        return alpha;
    }

    public void setAlpha(double alpha) {
        this.alpha = alpha;
    }

    public double getBeta() {
        return beta;
    }

    public void setBeta(double beta) {
        this.beta = beta;
    }

    public double getEpsilon() {
        return epsilon;
    }

    public void setEpsilon(double epsilon) {
        this.epsilon = epsilon;
    }

}