package eunomia.plugin.com.atas;
import eunomia.plugin.gui.atas.*;
import java.util.ArrayList;
/*
 * RoleChangeListener.java
 *
 * Created on January 17, 2007, 11:41 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

/**
 *
 * @author kulesh
 *
 * RoleChangeListener will be the middleman between the GUI and the backend.
 * GUI and backend both implements this listener. GUI should also implement a
 * setRoleChangeListener(RoleChangeListener rcl) so that the backend when it
 * instantiate the GUI can tell GUI about backend's instance of RoleChangeListener.
 */
public interface RoleChangeListener {
    
    //The following methods implement updating roles and hosts on the GUI
    
    //Insert a new role into the GUI. Or GUI can call this function to inform
    //the backend that the user has created a new role (e.g. by merging existing roles)
    public void insertRole(String roleName, ArrayList<HostInfo> hostList);
    
    //Remove a role from the GUI. Or GUI can call this function to inform the
    //backend that the user has removed this role from the GUI
    public void removeRole(String roleName);
    
    //Get a list of hosts that are part of role roleName.
    public ArrayList<HostInfo> getHostsOfRole(String roleName);
    
    //Insert a host into role roleName. This function can be used by the backend
    //to inform GUI of adding a single host to a role or by the GUI to inform backend
    //that the user has added a host to a role.
    public void insertHost(String roleName, HostInfo host);
    public void insertHosts(String roleName, ArrayList<HostInfo> hostList);
    
    //Insert a host into role intersection between role names specified by roleNames.
    //This function can be used by the backend to inform GUI of adding a single 
    //host to an intersection. To indicate an intersection between A & B, ArrayList
    //will have A, B; for (A & B & C) it will have three elements, A, B, C
    public void insertHost(ArrayList<String> roleNames, HostInfo host);
    public void insertHosts(ArrayList<String> roleNames, ArrayList<HostInfo> hostList);
    
    //remove a host from role roleName. This method can be used by the backend to
    //inform the GUI that host has been removed from role roleName.
    public void removeHost(String roleName, HostInfo host);
    public void removeHosts(String roleName, ArrayList<HostInfo> hostList);

    //Remove a host from intersection between role names specified by roleNames.
    //This function can be used by the backend to inform GUI of adding a single 
    //host to an intersection.
    public void removeHost(ArrayList<String> roleNames, HostInfo host);
    public void removeHosts(ArrayList<String> roleNames, ArrayList<HostInfo> hostList);

    //Whenever a user creates an intersection in the GUI, call this function with the name of roles that //are involved in the intersection. 
    public void setIntersection(ArrayList<String> roleNames);

    //Whenever a user removes a intersection call this function with the names of the Intersections. 
    //Note: that when a 3-set intersection is removed (even when one set is moved out) you need to 
    //call this function with all three sets, and then call setIntersection() with the two sets that remains. 
    public void removeIntersection(ArrayList<String> rolenames); 
}
