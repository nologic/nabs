/*
 * DarkSpaceRole.java
 *
 * Created on March 29, 2007, 4:30 PM
 *
 */

package eunomia.plugin.rec.atas.classifiers;

import eunomia.plugin.com.atas.ClassifierConfigurationMessage;
import eunomia.plugin.com.atas.HostInfo;
import eunomia.plugin.com.atas.RoleInterface;
import eunomia.plugin.rec.atas.classifiers.msg.DarkSpaceConfigMessage;
import eunomia.receptor.module.NABFlowV2.NABFlowV2;
import eunomia.util.Util;
import java.awt.GridLayout;
import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;
import javax.swing.JPanel;

/**
 *
 * @author kulesh
 */
public class DarkSpaceRole implements RoleInterface{
    private ConcurrentHashMap<Long, HostInfo> roleMap;
    private int roleNumber;
    private static final String roleName = "Darkspace Accessor";
    
    private long ipRangeBegin, ipRangeEnd; //inclusive
    private HashSet<Long> activeHosts; //set to maintain active hosts in local network
       
    /** Creates a new instance of DarkSpaceRole */
    public DarkSpaceRole() {
        roleMap = new ConcurrentHashMap<Long, HostInfo>();
        activeHosts = new HashSet<Long>();
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

    public void setIpRange(long beginIp, long endIp){
        ipRangeBegin = beginIp;
        ipRangeEnd  = endIp;
    }
    
    public long getIpRangeBegin(){
        return ipRangeBegin;
    }
    
    public long getIpRangeEnd(){
        return ipRangeEnd;
    }
    
    public void processFlowRecord(NABFlowV2 flowRecord) {
        long sourceIp = flowRecord.getSourceIP();
        
        //Check whether the flow originator is local
        if((sourceIp <= ipRangeEnd) && (sourceIp >= ipRangeBegin)){
            activeHosts.add(sourceIp);
            //you might want to remoce this guy from roles
        }

        if (activeHosts.contains(flowRecord.getDestinationIP()) == true)
            return;

        //currently scanning our known darkspace!
        updateRoleMap(flowRecord);
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
        DarkSpaceConfigMessage msg = new DarkSpaceConfigMessage();
        
        msg.setRoleName(this.getRoleName());
        msg.setRoleNumber(this.getRoleNumber());
        msg.setIpRangeBegin(this.getIpRangeBegin());
        msg.setIpRangeEnd(this.getIpRangeEnd());
        
        return msg;
    }

    public void setConfigurationMessage(ClassifierConfigurationMessage msg) {
        DarkSpaceConfigMessage dmg = (DarkSpaceConfigMessage)msg;
        
        this.setIpRange(dmg.getIpRangeBegin(), dmg.getIpRangeEnd());
    }
}