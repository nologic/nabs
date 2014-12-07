/*
 * ModuleRegistry.java
 *
 * Created on September 11, 2008, 11:09 PM
 *
 */

package eunomia.module.receptor.libb.imsCore.util;

import eunomia.module.receptor.libb.imsCore.Reporter;
import eunomia.module.receptor.libb.imsCore.VeyronAnalysisComponent;
import eunomia.module.receptor.libb.imsCore.VeyronProcessingComponent;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Mikhail Sosonkin
 */
public class ComponentRegistry {
    private static ComponentRegistry ins;
    
    private List anlComps;
    private List prcComps;
    private List repComps;
    private List listeners;
    
    private ComponentRegistry() {
        anlComps = new LinkedList();
        prcComps = new LinkedList();
        repComps = new LinkedList();
        listeners = new LinkedList();
    }
    
    public static ComponentRegistry getInstance() {
        if(ins == null) {
            ins = new ComponentRegistry();
        }
        
        return ins;
    }
    
    public List getAnalysisComponents() {
        return anlComps;
    }
    
    public List getProcessingComponents() {
        return prcComps;
    }
    
    public void registerAnalysisComponent(VeyronAnalysisComponent comp, long secInterval, long firstRunInterval) {
        AnlComp ac = new AnlComp(comp, secInterval, firstRunInterval);
        
        anlComps.add(ac);
        
        Iterator it = listeners.iterator();
        while (it.hasNext()) {
            ComponentRegistryListener l = (ComponentRegistryListener) it.next();
            l.analysisComponentAdded(ac);
        }
    }

    public void registerProcessingComponent(VeyronProcessingComponent comp) {
        prcComps.add(comp);

        Iterator it = listeners.iterator();
        while (it.hasNext()) {
            ComponentRegistryListener l = (ComponentRegistryListener) it.next();
            l.processingComponentAdded(comp);
        }
    }
    
    public void registerReportingComponent(Reporter rep) {
        repComps.add(rep);
        
        Iterator it = listeners.iterator();
        while (it.hasNext()) {
            ComponentRegistryListener l = (ComponentRegistryListener) it.next();
            l.reporterComponentAdded(rep);
        }
    }
    
    public void addComponentRegistryListener(ComponentRegistryListener l) {
        listeners.add(l);
    }
    
    public class AnlComp {
        public long secInterval;
        public long firstRunInterval;
        public VeyronAnalysisComponent comp;
        
        public AnlComp(VeyronAnalysisComponent comp, long secInterval, long firstRunInterval) {
            this.secInterval = secInterval;
            this.firstRunInterval = firstRunInterval;
            this.comp = comp;
        }
    }
}