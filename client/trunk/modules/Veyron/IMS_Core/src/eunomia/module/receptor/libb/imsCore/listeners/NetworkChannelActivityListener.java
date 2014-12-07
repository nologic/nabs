/*
 * NetworkChannelActivityListener.java
 *
 * Created on March 16, 2008, 9:52 PM
 *
 */

package eunomia.module.receptor.libb.imsCore.listeners;

import eunomia.module.receptor.libb.imsCore.net.NetworkChannel;
import eunomia.module.receptor.libb.imsCore.net.NetworkChannelFlowID;

/**
 *
 * @author Mikhail Sosonkin
 */
public interface NetworkChannelActivityListener {
    public boolean isStillInterested(NetworkChannelFlowID id, long notifiedAgo);
    public boolean channelActivity(NetworkChannel chan);
}