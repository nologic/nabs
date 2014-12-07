/*
 * NABStream.java
 *
 * Created on June 10, 2006, 12:22 PM
 *
 */

package eunomia.receptor.module.NABFlow;

import com.vivic.eunomia.module.receptor.FlowCreator;
import java.nio.ByteBuffer;

/**
 * A simple flow creator. The parsing is done by the flow object. It is recommended
 * that the same flow object is reused. This is because there are many flows going
 * through the system and it will be a performance hazard to create a new object
 * each time. The middleware works under the assumption that this object is being
 * reused.
 * @author Mikhail Sosonkin
 */
public class NABStream implements FlowCreator {
    /**
     * A flow object used by the creator.
     */
    private NABFlow flow;
    
    /**
     * Instantiates the creator and creates a reusable flow object.
     */
    public NABStream() {
        flow = new NABFlow();
    }

    /**
     * Indicates to the middleware how many are needed before notifying the flow 
     * creator.
     * @return the ammount of bytes needed to create a flow.
     */
    public int getNextFlowMinSize() {
        // NABFlow has a fixed length (21 bytes), however this value can changed based on
        // what's already been read.
        return NABFlow.FLOW_BYTE_SIZE;
    }

    /**
     * This method generates the flow. In this case it parses the network data and
     * return an updated flow object.
     * @param buff will contain at least the amount of bytes specified by the
     * <I>getNextFlowMinSize()</I> method.
     * @return Parsed flow object with all the module specific data.
     */
    public NABFlow processBuffer(ByteBuffer buff) {
        // mostly due to historic reason, the flow class has the actual parsing
        // functionality.
        flow.readFromByteBuffer(buff);
        
        // we reuse the same object to save GC and allocation type.
        return flow;
    }

    /**
     * Determines the size of the buffer that is to be maintained by the middleware.
     * This is usefull for processing many flows at the same time, it faster to read
     * bigger chunks. Also useful in cases when a partial flow cannot be processed.
     * @return number of bytes to buffer.
     */
    public int getBufferSize() {
        // We want the middleware to buffer maximum of 1024 flows.
        return 1024 * NABFlow.FLOW_BYTE_SIZE;
    }
}
