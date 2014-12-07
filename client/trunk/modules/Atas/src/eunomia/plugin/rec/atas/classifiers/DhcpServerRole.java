/*
 * WebServerRole.java
 *
 * Created on March 29, 2007, 4:06 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package eunomia.plugin.rec.atas.classifiers;

import com.vivic.eunomia.module.flow.Flow;
import eunomia.plugin.com.atas.ClassifierConfigurationMessage;
import eunomia.plugin.com.atas.HostInfo;
import eunomia.plugin.com.atas.RoleInterface;
import eunomia.plugin.com.atas.RoleName;
import eunomia.receptor.module.NABFlowV2.NABFlowV2;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author kulesh
 */
public class DhcpServerRole implements RoleInterface{
    private ConcurrentHashMap<Long, HostInfo> roleMap;
    private int roleNumber;
    private static final String roleName = "DHCP Server";
    
    /** Creates a new instance of WebServerRole */
    public DhcpServerRole() {
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
        byte proto= flowRecord.getProtocol();
        
        if(((proto == NABFlowV2.PROTOCOL_UDP) || (proto == NABFlowV2.PROTOCOL_TCP))){
            int sourcePort = flowRecord.getSourcePort();
            int destPort = flowRecord.getDestinationPort();
            
            if((sourcePort == 67 && destPort == 68) || (sourcePort == 547 && destPort == 546)) {
                updateRoleMap(flowRecord);
            }
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
        return null;
    }

    public void setConfigurationMessage(ClassifierConfigurationMessage msg) {
    }
}
