/*
 * FlowProc.java
 *
 * Created on August 1, 2005, 2:55 PM
 *
 */

package eunomia.plugin.lossyHistogram;

import eunomia.core.data.flow.*;
import eunomia.plugin.alg.LossyCounter;
import eunomia.plugin.interfaces.ModularFlowProcessor;

/**
 *
 * @author Mikhail Sosonkin
 */
public class FlowProc implements ModularFlowProcessor {
    private LossyCounter lc;
    private Filter filter;
    private boolean doProc;
    
    public FlowProc(LossyCounter lc) {
        this.lc = lc;
        doProc = true;
    }

    public Filter getFilter() {
        return filter;
    }

    public void newFlow(Flow flow) {
        if(doProc){
            if(filter != null && !filter.allow(flow)){
                return;
            }

            lc.newFlow(flow);
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
}