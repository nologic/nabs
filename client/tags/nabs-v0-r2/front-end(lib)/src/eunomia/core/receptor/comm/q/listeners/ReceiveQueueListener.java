/*
 * ReceieveQueueListener.java
 *
 * Created on October 11, 2006, 9:03 PM
 *
 */

package eunomia.core.receptor.comm.q.listeners;

import eunomia.core.receptor.comm.q.ReceiveQueue;

/**
 *
 * @author Mikhail Sosonkin
 */
public interface ReceiveQueueListener {
    public void caughtException(Exception e, ReceiveQueue queue);
}
