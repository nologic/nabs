/*
 * RoleInterface.java
 *
 * Created on March 27, 2007, 5:07 PM
 *
 */

package eunomia.plugin.com.atas;

import eunomia.receptor.module.NABFlowV2.NABFlowV2;
import java.util.Collection;
import javax.swing.JComponent;

/**
 *
 * @author kulesh
 */
public interface RoleInterface {   
    /**
     * 
     * @return The number of hosts in this Role
     */
   public int hostCount();
   
    /**
     * 
     * @return A unique role number assigned to this role
     */
   public int getRoleNumber();
   public void setRoleNumber(int n);

    /**
     * 
     * @return A descriptive string for this role
     */
   public String getRoleName();
   
    /**
     * 
     * @return A collection which provides iterators for HostInfos in this Role
     */
   public Collection getCollection();
   
    /**
     * 
     * @param ip An IP address
     * @return The corresponding HostInfo
     */
   public HostInfo getHostInfo(long ip);
   
    /**
     * 
     * @param flowRecord Takes a flow record and processes it (identifies the role)
     */
   public void processFlowRecord(NABFlowV2 flowRecord);
   
   public ClassifierConfigurationMessage getConfigurationMessage();
   public void setConfigurationMessage(ClassifierConfigurationMessage msg);
}