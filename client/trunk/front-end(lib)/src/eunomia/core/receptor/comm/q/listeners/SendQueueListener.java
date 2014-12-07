/*
 * SendQueueListener.java
 *
 * Created on October 11, 2006, 8:56 PM
 *
 */

package eunomia.core.receptor.comm.q.listeners;

import eunomia.core.receptor.comm.q.SendQueue;

/**
 *
 * @author Mikhail Sosonkin
 */
public interface SendQueueListener {
    public void caughtException(Exception e, SendQueue queue);
}
