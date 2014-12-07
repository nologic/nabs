/*
 * FlowModule.java
 *
 * Created on June 10, 2006, 12:51 AM
 *
 */

package com.vivic.eunomia.module.receptor;

import com.vivic.eunomia.module.flow.Flow;
import java.nio.ByteBuffer;

/**
 * <p> The FlowCreator interface is expected to be implemented by <b>Flow Producer</b>
 * module. This class is the entry point of data from the sensors. The Sieve will
 * perform the actual data management and retrieval, that data will be forwarded on
 * to the module, unmodified. The module can expect that the data will be contigous
 * as it is received from a Sensor.
 *
 * <p> The data will be sent to the module through <code>processBuffer()</code> method.
 * It will be in the for of Java NIO's ByteBuffer.
 *
 * <p> It is important to note that the class implementing this interface must focus
 * on time performance. The methods of this class will be invoked very often triggered
 * by the network events. So the time spent on processing a flow is added to the total
 * time spent on analyzing the flow.
 *
 * @author Mikhail Sosonkin
 */
public interface FlowCreator {
    /**
     * Returns next minimum value required for the next complete flow record.
     *
     * The return value represents the amount of bytes that should be available in the
     * receive buffer before the module is notified of a new event. The Sieve will
     * collect data until this value is reached or the buffer is full. The value can be
     * dynamic, which allows the developer to build a state machine. So, depending on
     * what sort of data was already received the module can choose to wait for more or
     * less data.
     *
     * @return Byte number.
     */
    public int getNextFlowMinSize();

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
    public int getBufferSize();

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
    public Flow processBuffer(ByteBuffer buff);
}