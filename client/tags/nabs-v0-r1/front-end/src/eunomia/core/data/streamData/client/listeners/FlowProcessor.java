/*
 * FlowRecieved.java
 *
 * Created on June 8, 2005, 4:55 PM
 */

package eunomia.core.data.streamData.client.listeners;

import eunomia.core.data.flow.*;

/**
 *
 * @author  Mikhail Sosonkin
 */

public interface FlowProcessor {
    public void setFilter(Filter filter);
    public Filter getFilter();
    public void newFlow(Flow flow);
}