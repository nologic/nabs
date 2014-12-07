/*
 * FlowProc.java
 *
 * Created on August 1, 2005, 2:55 PM
 *
 */

package eunomia.plugin.rec.lossyHistogram;

import eunomia.flow.*;
import eunomia.plugin.alg.*;
import eunomia.plugin.interfaces.*;
import eunomia.flow.Filter;
import eunomia.receptor.module.NABFlow.NABFlow;
import eunomia.receptor.module.interfaces.FlowModule;

/**
 *
 * @author Mikhail Sosonkin
 */
public class FlowProc implements FlowProcessor {
    private LossyCounter lc;
    private Filter filter;
    private boolean doProc;
    
    public FlowProc(LossyCounter lc) {
        filter = new Filter();
        this.lc = lc;
        doProc = true;
    }

    public Filter getFilter() {
        return filter;
    }

    public void newFlow(Flow flow) {
        if(doProc && flow instanceof NABFlow){
            if(filter != null && !filter.allow(flow)){
                return;
            }

            lc.newFlow((NABFlow)flow);
        }
    }

    public void setFilter(Filter filter) {
        this.filter = filter;
    }

    public boolean isDoProc() {
        return doProc;
    }

    public void setDoProc(boolean doProc) {
        this.doProc = doProc;
    }

    public boolean accept(FlowModule module) {
        return true;
    }
}