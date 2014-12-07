/*
 * FlowProc.java
 *
 * Created on August 1, 2005, 2:55 PM
 *
 */

package eunomia.plugin.rec.lossyHistogram;

import com.vivic.eunomia.filter.Filter;
import com.vivic.eunomia.module.flow.Flow;
import com.vivic.eunomia.module.receptor.FlowProcessor;
import eunomia.plugin.alg.LossyCounter;
import eunomia.receptor.module.NABFlow.NABFlow;
import eunomia.receptor.module.NABFlowV2.NABFlowV2;
import com.vivic.eunomia.module.flow.FlowModule;
import java.util.Arrays;

/**
 *
 * @author Mikhail Sosonkin
 */
public class FlowProc implements FlowProcessor {
    private LossyCounter lc;
    private Filter filter;
    private boolean doProc;
    private int[] types;
    
    // For processing top host count.
    private boolean doubleSource;
    private NABFlow secondSource;
    
    public FlowProc(LossyCounter lc) {
        types = new int[NABFlow.NUM_TYPES];
        filter = new Filter();
        this.lc = lc;
        doProc = true;
        doubleSource = false;
        secondSource = new NABFlow();
    }

    public Filter getFilter() {
        return filter;
    }

    public void newFlow(Flow flow) {
        if(doProc){
            if(filter != null && !filter.allow(flow)){
                return;
            }

            if(flow instanceof NABFlow) {
                NABFlow nabFlow = (NABFlow)flow;
                Arrays.fill(types, 0);
                types[nabFlow.getType()] = 1;
            } else if(flow instanceof NABFlowV2) {
                System.arraycopy(((NABFlowV2)flow).getTypeCount(), 0, types, 0, types.length);
            } else {
                return;
            }
            
            lc.newFlow(flow, types);
            if(doubleSource){
                //secondSource.takeFrom(nabFlow, false);
                secondSource.setSrcIp(flow.getDestinationIP());
                lc.newFlow(secondSource, types);
            }
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
        Flow flow = module.getNewFlowInstance();
        return flow instanceof NABFlow || flow instanceof NABFlowV2;
    }

    public boolean isDoubleSource() {
        return doubleSource;
    }

    public void setDoubleSource(boolean doubleSource) {
        this.doubleSource = doubleSource;
    }
}