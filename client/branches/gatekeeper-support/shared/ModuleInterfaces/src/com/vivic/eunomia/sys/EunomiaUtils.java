/*
 * EunomiaUtils.java
 *
 * Created on September 30, 2007, 3:15 PM
 *
 */

package com.vivic.eunomia.sys;

import com.vivic.eunomia.module.receptor.FlowModule;
import com.vivic.eunomia.sys.frontend.ConsoleModuleManager;
import com.vivic.eunomia.sys.receptor.SieveModuleManager;
import eunomia.flow.Filter;
import eunomia.messages.FilterEntryMessage;
import eunomia.messages.module.msg.ChangeFilterMessage;

/**
 *
 * @author Mikhail Sosonkin
 */
public class EunomiaUtils {
    private static Filter makeFilter(ChangeFilterMessage cfm, FlowModuleRetriever man) {
        Filter filter = new Filter();

        FilterEntryMessage[] fems = cfm.getWhiteList();
        if(fems != null){
            for(int i = 0; i < fems.length; i++){
                FlowModule fmod = man.getModule(fems[i].getFlowModule());
                if(fmod != null){
                    filter.addFilterWhite(fmod.getNewFilterEntry(fems[i]));
                }
            }
        }

        fems = cfm.getBlackList();
        if(fems != null){
            for(int i = 0; i < fems.length; i++){
                FlowModule fmod = man.getModule(fems[i].getFlowModule());
                if(fmod != null){
                    filter.addFilterBlack(fmod.getNewFilterEntry(fems[i]));
                }
            }
        }
        
        return filter;
    }
    
    public static Filter makeReceptorFilter(ChangeFilterMessage cfm, SieveModuleManager man) {
        return makeFilter(cfm, new FlowModuleRetriever(man));
    }
    
    public static Filter makeFrontendFilter(ChangeFilterMessage cfm, ConsoleModuleManager man) {
        return makeFilter(cfm, new FlowModuleRetriever(man));
    }
    
    private static class FlowModuleRetriever {
        private SieveModuleManager sm;
        private ConsoleModuleManager cm;
        
        public FlowModuleRetriever(SieveModuleManager man) {
            sm = man;
        }
        
        public FlowModuleRetriever(ConsoleModuleManager man) {
            cm = man;
        }
        
        public FlowModule getModule(String mod) {
            if(sm == null) {
                return cm.getFlowModule(mod);
            }
            
            return sm.getFlowModuleInstance(mod);
        }
    }
}