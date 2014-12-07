/*
 * FlowModule.java
 *
 * Created on June 10, 2006, 12:51 AM
 *
 */

package com.vivic.eunomia.module.receptor;

import com.vivic.eunomia.module.Flow;
import java.nio.ByteBuffer;

/**
 *
 * @author Mikhail Sosonkin
 */
public interface FlowCreator {
    public int getNextFlowMinSize();
    public int getBufferSize();
    public Flow processBuffer(ByteBuffer buff);
}