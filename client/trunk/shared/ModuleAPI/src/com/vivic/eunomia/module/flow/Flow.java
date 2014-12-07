/*
 * Flow.java
 *
 * Created on June 19, 2006, 9:43 PM
 *
 */

package com.vivic.eunomia.module.flow;

import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * <p>Represents a flow. An instance of this is passed to every FlowProcessor 
 * interested (serially). Once a flow is received from a flow source the same
 * instance will be passed to all registered Flow processors. The flow processor 
 * must be able to determine the type of flow on its own. It is up to the flow 
 * processor to know how to process a particular type of flow.
 * 
 * <p>This version of Flow allows only IPv4 addresses. To support other protocols
 * or Flow formats in general, the developer of the Flow module will need to
 * implement the specific methods to access the data. The protocol specific
 * are optional, their value can simply be set to zero:
 *
 * <pre>
 *     getSourceIP();
 *     getDestinationIP();
 *     getSourcePort();
 *     getDestinationPort();
 * </pre>
 * @author Mikhail Sosonkin
 */
public interface Flow {
    /**
     * 
     * @return Unix time, as read by the flow. It it is generally the time on the server.
     */
    public long getTime();
    public long getSourceIP();
    public long getDestinationIP();
    public int getSourcePort();
    public int getDestinationPort();
    
    public int getSize();
    
    /**
     * Generic way for retrieving flow specific data. It depends on implementation but 
     * generally not efficient.
     */
    public Object getSpecificInfo(Object format);
    
    /**
     * Internal network representation of the flow. This does not have to follow any 
     * standard. As long as the FlowModule maintains consistency on both sides.
     */
    public void writeToDataStream(DataOutputStream dout) throws IOException;
    public void readFromByteBuffer(ByteBuffer buffer);
}
