/*
 * ProcComponent.java
 *
 * Created on February 11, 2008, 8:50 PM
 *
 */

package eunomia.module.receptor.proc.exProcAnlz;

import com.vivic.eunomia.filter.Filter;
import com.vivic.eunomia.module.flow.Flow;
import com.vivic.eunomia.module.flow.FlowModule;
import com.vivic.eunomia.module.receptor.FlowProcessor;
import eunomia.module.receptor.libb.imsCore.VeyronProcessingComponent;
import eunomia.module.receptor.libb.imsCore.net.Network;
import eunomia.receptor.module.NABFlowV2.NABFlowV2;

/**
 *
 * @author Mikhail Sosonkin
 */
public class ProcComponent implements FlowProcessor, VeyronProcessingComponent {
    private Network net;
    
    public ProcComponent() {
    }

    public void setFilter(Filter filter) {
    }

    public Filter getFilter() {
        return null;
    }

    public void newFlow(Flow flow) {
    }

    public boolean accept(FlowModule module) {
        return module.getNewFlowInstance() instanceof NABFlowV2;
    }

    public void initialize(Network net) {
        this.net = net;
    }
    
}
