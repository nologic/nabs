/*
 * AssociationMap.java
 *
 * Created on April 2, 2007, 1:40 PM
 *
 */

package eunomia.plugin.gui.atas;

import eunomia.plugin.com.atas.HostInfo;
import eunomia.plugin.com.atas.RoleChangeListener;
import eunomia.plugin.com.atas.RoleName;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author kulesh
 */
public class AssociationMap {
    private RoleChangeListener roleListener;
    //For each role, keep track of the HostInfos
    private ConcurrentHashMap<Integer, ConcurrentHashMap<Long, HostInfo>> roleMap;
    
    //For each role, keep track of the relation to other roles
    private HashSet<ArrayList<String>>[] relationSet;
    
    //For each role, keep track of the IP addresses belonging to the set
    private HashSet<Long>[] workingSet;
    private String[] names = new String[RoleName.MAX_ROLETYPE];
    
    /** Creates a new instance of AssociationMap */
    public AssociationMap(RoleChangeListener roleListener) {
        names[RoleName.MAX_ROLETYPE - 1] = "Unknown";
        this.roleListener = roleListener;
        roleMap= new ConcurrentHashMap<Integer, ConcurrentHashMap<Long, HostInfo>>();
        relationSet = new HashSet[RoleName.MAX_ROLETYPE];
        workingSet  = new HashSet[RoleName.MAX_ROLETYPE];
        
        for(int i=0; i < workingSet.length; ++i){
            relationSet[i] = new HashSet<ArrayList<String>>();
            workingSet[i] = new HashSet<Long>();
        }
    }
    
    public ConcurrentHashMap<Long, HostInfo> getRoleSet(int roleNumber){
        return roleMap.get(roleNumber);
    }
    
    public HostInfo getHostInfo(int roleNumber, long ip){
        ConcurrentHashMap<Long, HostInfo> tmp = roleMap.get(roleNumber);
        if(tmp == null)
            return null;
        
        return tmp.get(ip);
    }
    
    private void createRoleSet(int roleNumber){
        ConcurrentHashMap<Long, HostInfo> tmp= new ConcurrentHashMap<Long, HostInfo>();
        roleMap.put(roleNumber, tmp);
    }
    
    private void updateNamesList(int n, String str) {
        names[n] = str;
    }
    
    public void updateWorkingSet(int roleNumber, String roleName, HostInfo info){
        updateNamesList(roleNumber, roleName);
        workingSet[roleNumber].add(info.getIp());
        
        ConcurrentHashMap<Long, HostInfo> tmp= roleMap.get(roleNumber);
        if(tmp == null){
            createRoleSet(roleNumber);
            tmp= roleMap.get(roleNumber);
        }
        
        HostInfo currentInfo = null;
        currentInfo= tmp.get(info.getIp());
        if(currentInfo == null){
            tmp.put(info.getIp(), info);
        }else{
            currentInfo.updateStats(info);
        }
        
        if(relationSet[roleNumber].size() == 0){
            //This set not involved in relations just update the GUI
            if(currentInfo == null){
                roleListener.insertHost(roleName, info);
            }else{
                roleListener.insertHost(roleName, currentInfo);
            }
        }
    }
    
    public void updateRoleDisplay(int roleNumber, String roleName){
        //No need to update if the role is not in a relation
        //Update was armortized in updateWorkingSet()
        updateNamesList(roleNumber, roleName);
        if(relationSet[roleNumber].size() == 0)
            return;
        
        ArrayList<String> tmp;
        Iterator<ArrayList<String>> itr = relationSet[roleNumber].iterator();
        while(itr.hasNext()){
            tmp= itr.next();
            if(tmp.size() == 1){
                updateIntersection2(roleNumber, roleName, tmp);
            }else{
                updateIntersection3(roleNumber, roleName, tmp);
            }
        }
    }
    
    private Set<Long> findIntersection(int roleA, int roleB){
        Set<Long> intersection= new HashSet<Long>(workingSet[roleA]);
        
        intersection.retainAll(workingSet[roleB]);
        return intersection;
    }
    
    private void updateIntersection2(int roleNumber, String roleName, ArrayList<String> l){
        int otherRole= getRoleNameIndex(l.get(0));
        
        Set<Long> intersection= findIntersection(roleNumber, otherRole);
        
        if(intersection.size() == 0)
            return;
        
        ArrayList<String> roleNames = new ArrayList<String>();
        roleNames.add(roleName);
        roleNames.add(l.get(0));
        
        HostInfo tmp;
        Iterator<Long> itr= intersection.iterator();
        ConcurrentHashMap<Long, HostInfo> hosts= roleMap.get(roleNumber);
        ArrayList<HostInfo> hostList = new ArrayList<HostInfo>();
        while(itr.hasNext()){
            tmp= hosts.get(itr.next());
            hostList.add(tmp);
        }
        
        roleListener.insertHosts(roleNames, hostList);
    }
    
    private void updateIntersection3(int roleNumber, String roleName, ArrayList<String> l){
        ArrayList<String> otherSet = new ArrayList<String>();
        
        otherSet.add(l.get(0));
        updateIntersection2(roleNumber, roleName, otherSet);
        
        otherSet.clear();
        
        otherSet.add(l.get(1));
        updateIntersection2(roleNumber, roleName, otherSet);
        
        //Now update the intersection of all three sets!
        int otherRole = getRoleNameIndex(l.get(0));
        Set<Long> intersection1= findIntersection(roleNumber, otherRole);
        
        otherRole= getRoleNameIndex(l.get(1));
        Set<Long> intersection2= findIntersection(roleNumber, otherRole);
        
        intersection1.retainAll(intersection2);
        
        if(intersection1.size() == 0)
            return;
        
        ArrayList<String> roleNames = new ArrayList<String>();
        roleNames.add(roleName);
        roleNames.add(l.get(0));
        roleNames.add(l.get(1));
        
        HostInfo tmp;
        Iterator<Long> itr= intersection1.iterator();
        ConcurrentHashMap<Long, HostInfo> hosts= roleMap.get(roleNumber);
        ArrayList<HostInfo> hostList = new ArrayList<HostInfo>();
        while(itr.hasNext()){
            tmp= hosts.get(itr.next());
            hostList.add(tmp);
        }
        
        roleListener.insertHosts(roleNames, hostList);
    }
    
    public void addRelationSet(ArrayList<String> relations){
        int intersections = relations.size();
        ArrayList<String> tmp= new ArrayList<String>();
        
        switch(intersections){
            case 2:
                //Insert the second roleName into first as its intersection
                tmp.add(relations.get(1));
                relationSet[getRoleNameIndex(relations.get(0))].add(tmp);
                
                tmp.clear();
                
                //Insert the first roleName into the second as its intersection
                tmp.add(relations.get(0));
                relationSet[getRoleNameIndex(relations.get(1))].add(tmp);
                break;
                
            case 3:
                //Insert the second, third roleName into first as its intersection
                tmp.add(relations.get(1));
                tmp.add(relations.get(2));
                relationSet[getRoleNameIndex(relations.get(0))].add(tmp);
                
                tmp.clear();
                
                //Insert the first, third roleName into second as its intersection
                tmp.add(relations.get(0));
                tmp.add(relations.get(2));
                relationSet[getRoleNameIndex(relations.get(1))].add(tmp);
                
                tmp.clear();
                
                //Insert the first, second roleName into third as its intersection
                tmp.add(relations.get(0));
                tmp.add(relations.get(1));
                relationSet[getRoleNameIndex(relations.get(2))].add(tmp);
                break;
                
            default:
                System.err.println("Invalid number of intersections (" + intersections + " . Maximum allowed is 3!");
                break;
        }
    }
    
    public void removeRelationSet(ArrayList<String> relations){
        int intersections = relations.size();
        Iterator<ArrayList<String>> itr;
        ArrayList<String> tmp;
        
        switch(intersections){
            case 2:
                //Go through the relations of the first set and remove the second set
                itr= relationSet[getRoleNameIndex(relations.get(0))].iterator();
                while(itr.hasNext()){
                    tmp = itr.next();
                    if((tmp.size() == 1) && (tmp.get(0) == relations.get(1))){
                        itr.remove();
                    }
                }
                
                //Go through the relations of the second set and remove the first set
                itr= relationSet[getRoleNameIndex(relations.get(1))].iterator();
                while(itr.hasNext()){
                    tmp = itr.next();
                    if((tmp.size() == 1) && (tmp.get(0) == relations.get(0))){
                        itr.remove();
                    }
                }
                break;
                
            case 3:
                //Go through the relations of the first set and remove the (second and third) set
                itr= relationSet[getRoleNameIndex(relations.get(0))].iterator();
                while(itr.hasNext()){
                    tmp = itr.next();
                    if((tmp.size() == 2) &&
                            ((tmp.get(0) == relations.get(1)) && (tmp.get(1) == relations.get(2)) ||
                            (tmp.get(1) == relations.get(1)) && (tmp.get(0) == relations.get(2)))
                            ){
                        itr.remove();
                    }
                }
                
                //Go through the relations of the second set and remove the (first and third) set
                itr= relationSet[getRoleNameIndex(relations.get(1))].iterator();
                while(itr.hasNext()){
                    tmp = itr.next();
                    if((tmp.size() == 2) &&
                            ((tmp.get(0) == relations.get(0)) && (tmp.get(1) == relations.get(2)) ||
                            (tmp.get(1) == relations.get(0)) && (tmp.get(0) == relations.get(2)))
                            ){
                        itr.remove();
                    }
                }
                
                //Go through the relations of the third set and remove the (first and second) set
                itr= relationSet[getRoleNameIndex(relations.get(2))].iterator();
                while(itr.hasNext()){
                    tmp = itr.next();
                    if((tmp.size() == 2) &&
                            ((tmp.get(0) == relations.get(0)) && (tmp.get(1) == relations.get(1)) ||
                            (tmp.get(1) == relations.get(0)) && (tmp.get(0) == relations.get(1)))
                            ){
                        itr.remove();
                    }
                }
                
                break;
                
            default:
                System.err.println("Invalid number of intersections (" + intersections + " . Maximum allowed is 3!");
                break;
        }
    }
    
    private int getRelationsCount(int roleNumber){
        return relationSet[roleNumber].size();
    }
    
    private int getRoleNameIndex(String roleName){
        for(int i=0; i < names.length; ++i){
            if(roleName.equals(names[i])){
                return i;
            }
        }
        
        return (RoleName.MAX_ROLETYPE - 1);
    }
}
