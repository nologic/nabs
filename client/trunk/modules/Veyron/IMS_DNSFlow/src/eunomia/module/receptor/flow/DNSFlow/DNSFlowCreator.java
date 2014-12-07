package eunomia.module.receptor.flow.DNSFlow;

import com.vivic.eunomia.module.receptor.FlowCreator;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

/**
 *
 * @author justin
 */
public class DNSFlowCreator implements FlowCreator {
    
    private DNSFlow flow;
    private boolean waitingForHeader = true;
    
    public DNSFlowCreator() {
        flow = new DNSFlow();
    }
    
    private int nextFlowMinSize = 32; // initialized to the header size
    
    public int getNextFlowMinSize() {
        return nextFlowMinSize;
    }
    
    /**
     * Returns the size of the recieve buffer.
     *
     * The is allowed to specify how large the receive buffer should be. This value is
     * unrestricted, so the developer should be mindful of perform requirements and
     * resource availability.
     *
     * <p> For example, the module may want to wait until 1024 flow records have arrived
     * before processing them and each record is 21 bytes. The buffer size will be set to
     * 21 * 1024 bytes. Next flow min size could return 21, so that as soon as any flows
     * are available (upto 1024) the module will be notified.
     *
     * @return Buffer size in Bytes.
     */
    
    // Large enough to hold the largest possible dns payload
    public int getBufferSize() {
        return 0x0000FFFF;
    }

    /**
     * Parses network data and returns a flow object.
     *
     * This is the performance piece for the module. This function will be provided a
     * byte buffer containing data from a sensor. The function should parse it and
     * generate an appropriate Flow object. Note that the buffer may contain more data
     * than requested, but not less. The module can choose to get all bytes, none, or
     * some. The Sieve will maintain those bytes in a queue, so any left over bytes will
     * be passed in on the next call. It is advised, for performance reasons, that the
     * module reuses the same Flow object for every flow. The processing modules work on
     * the assumption that this is the case.
     *
     * @param buff Byte buffer.
     * @return Flow object produced from the buff buffer.
     */
    public DNSFlow processBuffer(ByteBuffer buff) {
        try {
            if (waitingForHeader) {

                flow.readHeaderFromBuffer(buff);

                // set nextFlowMinSize to be the size of the DNS payload
                nextFlowMinSize = (int) (flow.getBodyLength() & 0x0000FFFF);
                waitingForHeader = false;
                
                return null;
            }

            // fetch body section from the byte buffer and store it in the flow
            flow.readBodyFromBuffer(buff);

            // set nextFlowMinSize to be the size of the header
            nextFlowMinSize = 32;
            waitingForHeader = true;

            return flow;
        } catch (UnsupportedEncodingException ex) {
            ex.printStackTrace();
            
            nextFlowMinSize = 32;
            return null;
        }
    }
}