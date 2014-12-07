/*
 * StreamManagerListener.java
 *
 * Created on June 16, 2005, 5:41 PM
 */

package eunomia.core.managers.listeners;

import eunomia.core.data.streamData.*;

/**
 *
 * @author  Mikhail Sosonkin
 */
public interface StreamManagerListener {
    public void streamAdded(StreamDataSource sds);
    public void streamRemoved(StreamDataSource sds);
}