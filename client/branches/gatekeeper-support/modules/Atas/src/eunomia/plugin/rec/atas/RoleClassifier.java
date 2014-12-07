/*
 * RoleClassifier.java
 *
 * Created on March 6, 2007, 3:20 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package eunomia.plugin.rec.atas;

import eunomia.messages.Message;
import eunomia.plugin.com.atas.RoleInterface;
import eunomia.plugin.com.atas.RoleName;
import eunomia.plugin.rec.atas.classifiers.DarkSpaceRole;
import eunomia.receptor.module.NABFlowV2.NABFlowV2;
import eunomia.util.Util;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author kulesh
 */
public class RoleClassifier {
    /* default params for darkspace access */
    private static String lowestIp = "0.0.238.128";
    private static String highestIp = "255.255.238.128";
    
    private static String darkSpaceClass = "DarkSpaceRole";
    private static String[] classifiers = new String[] {
        "WebServerRole",
        "InteractiveSessionParticipants",
        "WebClientRole",
        "MailServerRole",
        "DnsServerRole",
        "DhcpServerRole"
    };
    
    private RoleInterface roleClassifiers[];
    
    public RoleClassifier() {
        roleClassifiers = new RoleInterface[RoleName.MAX_ROLETYPE - 1];
        
        loadDefaultClassifiers();
    }
    
    private void loadDefaultClassifiers() {
        try {
            DarkSpaceRole dr = (DarkSpaceRole)loadNewRole(darkSpaceClass);
            dr.setIpRange(Util.getLongIp(lowestIp), Util.getLongIp(highestIp));
            
            for (int i = 0; i < classifiers.length; i++) {
                loadNewRole(classifiers[i]);
            }
        } catch (InstantiationException ex) {
            ex.printStackTrace();
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        } catch (IllegalAccessException ex) {
            ex.printStackTrace();
        }
    }
    
    public RoleInterface loadNewRole(String name) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        RoleInterface role = loadClassifier(name);
        addClassifier(role);
        
        return role;
    }
    
    public void addClassifier(RoleInterface ri) {
        for (int i = 0; i < roleClassifiers.length; i++) {
            if(roleClassifiers[i] == null) {
                roleClassifiers[i] = ri;
                ri.setRoleNumber(i);
                return;
            }
        }
        
        throw new RuntimeException("Need to expand classifier count");
    }
    
    public List getConfigurationList() {
        List list = new LinkedList();
        for (int i = 0; i < roleClassifiers.length; i++) {
            RoleInterface role = roleClassifiers[i];
            if(role != null) {
                Message msg = role.getConfigurationMessage();
                if(msg != null) {
                    list.add(msg);
                }
            }
        }
        
        return list;
    }
    
    public void identifyRole(NABFlowV2 flowRecord){
        for(int i = 0; i < roleClassifiers.length; ++i){
            roleClassifiers[i].processFlowRecord(flowRecord);
        }
    }
    
    public int getRoleClassifierCount(){
        return roleClassifiers.length;
    }
    
    public RoleInterface getRoleClassifierByIndex(int n){
        if(n >= roleClassifiers.length)
            return null;
        
        return roleClassifiers[n];
    }
    
    public RoleInterface getRoleClassifierByRoleNumber(int n){
        for(int i = 0; i < roleClassifiers.length; ++i){
            if(roleClassifiers[i].getRoleNumber() == n) {
                return roleClassifiers[i];
            }
        }
        
        return null;
    }
    
    public int getActiveRoles(){
        int activeRoles= 0;
        
        for(int i = 0; i < roleClassifiers.length; ++i) {
            if(roleClassifiers[i].hostCount() > 0) {
                ++activeRoles;
            }
        }
        
        return activeRoles;
    }
    
    private static RoleInterface loadClassifier(String name) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        String className = "eunomia.plugin.rec.atas.classifiers." + name;
        Class klass = Class.forName(className);
        
        return (RoleInterface)klass.newInstance();
    }
}