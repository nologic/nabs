/*
 * FlowRecieved.java
 *
 * Created on June 8, 2005, 4:55 PM
 */

package com.vivic.eunomia.module.receptor;

import com.vivic.eunomia.filter.Filter;
import com.vivic.eunomia.module.flow.Flow;
import com.vivic.eunomia.module.flow.FlowModule;

/**
 * Basic processing unit for flows.
 * @author Mikhail Sosonkin
 */

public interface FlowProcessor {
    /**
     * Defines the filter on the incoming flows.
     * @param filter Filter Object, it is up to the module to actually enforce it. So, filters can
     * be used for anything.
     */
    public void setFilter(Filter filter);
    /**
     * Filter used by the module.
     * @return Filter instance, can be null
     */
    public Filter getFilter();
    /**
     * On new flow this is called by the system. It is up to the module to deal with
     * different flow types. This method needs to be optimizes for time as much as
     * possible. All flow processor modules will specify an object of this time and
     * the Sieve will call <CODE>newFlow</CODE> method serially for each new flow received.
     * @param flow Flow received by system.
     */
    public void newFlow(Flow flow);
    /**
     * Decides which flows will be sent to the module for processing. Decision made at
     * connect time.
     * @param module The flow module for a some flow source
     * @return Flag for whether or not the module will except a flow from this type.
     */
    public boolean accept(FlowModule module);
}