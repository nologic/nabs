/*
 * Flow.java
 *
 * Created on June 19, 2006, 9:43 PM
 *
 */

package eunomia.flow;

import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Represents a flow. An instance of this is passed to every FlowProcessor 
 * interested (serially). Once a flow is received from a flow source the same
 * instance will be passed to Flow processors. The flow processor must be able
 * to determine the type of flow on its on. It is up to the flow processor to
 * know how to process a particular type of flow.
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
