/*
 * WebServerRole.java
 *
 * Created on March 29, 2007, 4:06 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package eunomia.plugin.rec.atas.classifiers;

import eunomia.plugin.com.atas.ClassifierConfigurationMessage;
import eunomia.plugin.com.atas.HostInfo;
import eunomia.plugin.com.atas.RoleInterface;
import eunomia.receptor.module.NABFlowV2.NABFlowV2;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author kulesh
 */
public class WebClientRole implements RoleInterface{
    private ConcurrentHashMap<Long, HostInfo> roleMap;
    private int roleNumber;
    private static final String roleName = "Web Client";
    
    /** Creates a new instance of WebServerRole */
    public WebClientRole() {
        roleMap = new ConcurrentHashMap<Long, HostInfo>();
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
        
        if((flowRecord.getSourcePort() == 80) || 
                (flowRecord.getSourcePort() == 8080) ||
                (flowRecord.getSourcePort() == 443)){
            updateRoleMap(flowRecord);
        }
    }
    
    private void updateRoleMap(NABFlowV2 flowRecord){
        long destIp= flowRecord.getDestinationIP();
        HostInfo tmp= roleMap.get(destIp);
        
        if(tmp == null){
            tmp= new HostInfo(destIp);
            tmp.setLastSeen(flowRecord.getTime());
            tmp.incrementBytes(flowRecord.getSize());
            tmp.incrementPackets(flowRecord.getPackets());
            roleMap.put(destIp, tmp);
        }else{
            tmp.setLastSeen(flowRecord.getTime());
            tmp.incrementBytes(flowRecord.getSize());
            tmp.incrementPackets(flowRecord.getPackets());
        }
    }
    
    public ClassifierConfigurationMessage getConfigurationMessage() {
        return null;
    }

    public void setConfigurationMessage(ClassifierConfigurationMessage msg) {
    }

}
